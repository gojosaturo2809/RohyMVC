package mg.itu.rohymvc.utilitaire;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import mg.itu.rohymvc.fichiermap.MappingFile;
import mg.itu.rohymvc.session.MySession;
import mg.itu.rohymvc.session.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mg.itu.rohymvc.annotation.Api;
import mg.itu.rohymvc.annotation.AuthRequired;
import mg.itu.rohymvc.annotation.Controller;
import mg.itu.rohymvc.annotation.Role;
import mg.itu.rohymvc.annotation.UrlMapping;
import mg.itu.rohymvc.url.URLMapping;
import mg.itu.rohymvc.url.URLMethod;
import mg.itu.rohymvc.vue.ModelAndView;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class Utils {

    public void scanClassPath(String packageName, HashMap<URLMethod, URLMapping> urlmap)
            throws RuntimeException, Exception {

        String[] packages = packageName.split(";");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (String pkg : packages) {

            String path = pkg.replace('.', '/');

            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {

                URL resource = resources.nextElement();

                File directory = new File(resource.toURI());

                if (directory.exists()) {
                    scanDirectory(directory, pkg, urlmap);
                }

            }
        }

    }

    private void scanDirectory(
            File directory,
            String packageName,
            HashMap<URLMethod, URLMapping> urlMap)
            throws RuntimeException, ClassNotFoundException {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                scanDirectory(
                        file,
                        packageName + "." + file.getName(),
                        urlMap);

            } else if (file.getName().endsWith(".class")) {

                String className = packageName + "."
                        + file.getName().replace(".class", "");

                Class<?> c = Class.forName(className);
                if (c.isAnnotationPresent(Controller.class)) {
                    for (Method m : c.getDeclaredMethods()) {
                        UrlMapping annotation = m.getAnnotation(UrlMapping.class);
                        if (annotation != null) {
                            URLMapping mapped = new URLMapping();
                            mapped.setC(c);
                            mapped.setMethods(m);
                            URLMethod um = new URLMethod(annotation.url(), annotation.method());

                            if (urlMap.containsKey(um)) {
                                throw new RuntimeException("L'URL: " + annotation.url() + " de methode:"
                                        + annotation.method() + " dans le controller " + c.getSimpleName()
                                        + " est deja associe a un URL de meme methode");
                            }
                            urlMap.put(um, mapped);
                        }
                    }
                }

            }
        }
    }

    public static Object invokeMethod(URLMapping mapping, WebApplicationContext springContext,
            HttpServletRequest request,String session_key,String role_key) throws Exception {
                MySession session = SessionManager.getSession(request);
               
                if(mapping.getMethods().isAnnotationPresent(AuthRequired.class)){
                    if(session.getAttribute(session_key)==null){
                        
                       throw new RuntimeException("Vous devez etre authentifie pour acceder a cette ressource");
                    
                    }
                }
                else if(mapping.getMethods().isAnnotationPresent(Role.class)){
                    if(session.getAttribute(session_key)==null){
                        
                       throw new RuntimeException("Vous devez etre authentifie pour acceder a cette ressource");
                    
                    }
                    else if(session.getAttribute(role_key)==null || !session.getAttribute(role_key).equals(mapping.getMethods().getAnnotation(Role.class).nom())){
                        throw new RuntimeException("Vous n'avez pas le role requis pour acceder a cette ressource");
                    }
                }
        Object controller = mapping.getC()
                .getDeclaredConstructor()
                .newInstance();

        Parameter[] parameters = mapping.getMethods().getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            String paramName = parameters[i].getName();
            Class<?> type = parameters[i].getType();

            if (List.class.isAssignableFrom(type)) {
                args[i] = remplirList(
                        parameters[i],
                        request,
                        paramName);
                continue;
            }
            if (type.equals(ApplicationContext.class)) {
                args[i] = springContext;
                continue;
            }
            if (type.equals(MySession.class)) {
    args[i] = session;
    continue;
}
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();

                String[] paramValues = request.getParameterValues(paramName);
                if (paramValues != null) {

                    Object array = Array.newInstance(componentType, paramValues.length);
                    for (int j = 0; j < paramValues.length; j++) {
                        Array.set(array, j, convert(paramValues[j], componentType));
                    }
                    args[i] = array;
                } else {
                    args[i] = null;
                }
                continue;
            }
            if (!type.isPrimitive()
                    && !type.isInterface()
                    && !type.isEnum()
                    && !type.isArray()
                    && !type.equals(String.class)) {

                Object obj = type.getDeclaredConstructor().newInstance();
                remplirObjet(type, obj, request, paramName);
                args[i] = obj;
                continue;
            }

            System.out.println("paramName: " + paramName);
            System.out.println("paramValue: " + request.getParameter(paramName));
            String paramValue = request.getParameter(paramName);

            if (paramValue == null) {
                args[i] = null;
            } else {
                args[i] = convert(paramValue, parameters[i].getType());
            }
        }

        return mapping.getMethods().invoke(controller, args);

    }
    private static boolean contientParametres(
        HttpServletRequest request,
        String prefix) {

    return request.getParameterMap()
            .keySet()
            .stream()
            .anyMatch(name -> name.startsWith(prefix + "."));
}

    private static void remplirObjet(
            Class<?> type,
            Object obj,
            HttpServletRequest request, String param) {

        for (Method method : type.getMethods()) {

            if (method.getName().startsWith("set")
                    && method.getParameterCount() == 1) {

                String setterName = method.getName();

                String fieldName = setterName.substring(3);

                fieldName = Character.toLowerCase(fieldName.charAt(0))
                        + fieldName.substring(1);

                String value = request.getParameter(param + "." + fieldName);

                Class<?> setterType = method.getParameterTypes()[0];


                if (setterType.equals(MappingFile.class)) {

    try {
        Part part = request.getPart(param + "." + fieldName);

        MappingFile fichier = convertirFichier(part);

        method.invoke(obj, fichier);

    } catch (Exception e) {
        throw new RuntimeException(
            "Erreur lors du traitement du fichier : " + fieldName,
            e
        );
    }

    continue;
}

                if (!setterType.isPrimitive()
                        && !setterType.equals(String.class)) {

                   String nestedPrefix = param + "." + fieldName;

    if (!contientParametres(request, nestedPrefix)) {
        continue;
    }

    try {
        Object nestedObj =
                setterType.getDeclaredConstructor().newInstance();

        remplirObjet(
                setterType,
                nestedObj,
                request,
                nestedPrefix
        );

        method.invoke(obj, nestedObj);

                    } catch (InstantiationException
                            | IllegalAccessException
                            | InvocationTargetException
                            | NoSuchMethodException e) {

                        e.printStackTrace();
                    }

                } else {

                    if (value != null) {

                        Object convertedValue = convert(value, setterType);

                        try {

                            method.invoke(obj, convertedValue);

                        } catch (IllegalAccessException
                                | InvocationTargetException e) {

                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

        
private static MappingFile convertirFichier(Part part)
        throws IOException {

    if (part == null || part.getSize() == 0) {
        return null;
    }

    UploadConfig config = UploadConfig.load();

    // Vérification de la taille
    if (part.getSize() > config.getMaxFileSize()) {
        throw new IllegalArgumentException(
            "Fichier trop volumineux : " + part.getSize()
            + " octets. Maximum autorisé : "
            + config.getMaxFileSize() + " octets."
        );
    }

    String submittedName = part.getSubmittedFileName();

    if (submittedName == null || submittedName.isBlank()) {
        throw new IllegalArgumentException(
            "Nom de fichier manquant."
        );
    }

    String nomOriginal = submittedName.replace('\\', '/');
    nomOriginal = nomOriginal.substring(
        nomOriginal.lastIndexOf('/') + 1
    );

    if (nomOriginal.isBlank()
            || nomOriginal.equals(".")
            || nomOriginal.equals("..")) {
        throw new IllegalArgumentException(
            "Nom de fichier invalide."
        );
    }

    
    int point = nomOriginal.lastIndexOf('.');
    String extension = point > 0
            ? nomOriginal.substring(point + 1)
            : "";


    byte[] contenu;

    try (var input = part.getInputStream()) {
        contenu = input.readAllBytes();
    }

    String nomStockage;

    if (config.isOverwrite()) {
        nomStockage = nomOriginal;
    } else {
        String extensionFichier = extension.isEmpty()
                ? ""
                : "." + extension;

        nomStockage = java.util.UUID.randomUUID()
                + extensionFichier;
    }

    Path destination = config.getDirectory()
            .resolve(nomStockage)
            .normalize();

    // Vérifier que la destination reste dans le répertoire autorisé
    if (!destination.getParent().equals(
            config.getDirectory().toAbsolutePath().normalize())) {
        throw new IllegalArgumentException(
            "Destination de fichier invalide."
        );
    }

  
        java.nio.file.Files.write(
            destination,
            contenu,
            java.nio.file.StandardOpenOption.CREATE_NEW
        );
    

    // Construire l'objet transmis au contrôleur
    MappingFile fichier = new MappingFile();
    fichier.setNomFichier(nomOriginal);
    fichier.setExtension(extension);
    fichier.setContenu(contenu);
    fichier.setCheminStockage(destination.toString());

    return fichier;
}

    

    private static List<?> remplirList(
            Parameter parameter,
            HttpServletRequest request,
            String paramName) throws Exception {

        Type genericType = parameter.getParameterizedType();

        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Le type de la List doit être précisé : " + paramName);
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericType;

        Type elementType = parameterizedType.getActualTypeArguments()[0];

        if (!(elementType instanceof Class<?>)) {
            throw new IllegalArgumentException(
                    "Type d'élément non pris en charge : " + paramName);
        }

        Class<?> elementClass = (Class<?>) elementType;

        List<Object> list = new ArrayList<>();

        // Liste de valeurs simples
        if (isSimpleType(elementClass)) {

            String[] values = request.getParameterValues(paramName);

            if (values != null) {
                for (String value : values) {
                    list.add(convert(value, elementClass));
                }
            }

            return list;
        }

          // Cas 2 : liste d'objets
    int index = 0;

    while (true) {

        String objectPrefix = paramName + "[" + index + "]";

        // Vérifie si cet objet possède au moins un paramètre
        boolean exists = false;

        for (String name : request.getParameterMap().keySet()) {
            if (name.startsWith(objectPrefix + ".")) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            break;
        }

        Object obj = elementClass
                .getDeclaredConstructor()
                .newInstance();

        remplirObjet(elementClass, obj, request, objectPrefix);

        list.add(obj);
        index++;
    }
    return list;
    }
    
    private static boolean isSimpleType(Class<?> type) {

        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Double.class
                || type == Long.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == Boolean.class
                || type == Character.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type.isEnum();
    }

    public static void renderView(Object result, URLMapping mapping, String prefix, String suffix,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (mapping.getMethods().isAnnotationPresent(Api.class)) {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String jsonResponse = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result);
            response.getWriter().write(jsonResponse);
        } else if (result instanceof String) {
            String viewName = prefix + (String) result + suffix;

            request.getRequestDispatcher(viewName).forward(request, response);
        } else if (result instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView) result;
            for (String attribute : modelAndView.getAttributes().keySet()) {
                request.setAttribute(attribute, modelAndView.getAttributes().get(attribute));
            }
            request.getRequestDispatcher(prefix + modelAndView.getView() + suffix).forward(request, response);
        } else {
            throw new IllegalArgumentException("Result must be a String or ModelAndView");
        }

    }

    private static Object convert(String value, Class<?> type) {

        if (type == String.class) {
            return value;
        }

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        }
        if (type == LocalDate.class) {
            return LocalDate.parse(value);
        }

        return value;
    }
}
