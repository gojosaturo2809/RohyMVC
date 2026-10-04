package mg.itu.rohymvc.utilitaire;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.rohymvc.annotation.Api;
import mg.itu.rohymvc.annotation.Controller;
import mg.itu.rohymvc.annotation.UrlMapping;
import mg.itu.rohymvc.url.URLMapping;
import mg.itu.rohymvc.url.URLMethod;
import mg.itu.rohymvc.vue.ModelAndView;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;


public class Utils {

    public void scanClassPath(String packageName,HashMap<URLMethod,URLMapping> urlmap) throws RuntimeException,Exception {

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
            HashMap<URLMethod, URLMapping> urlMap
            )
            throws RuntimeException,ClassNotFoundException {

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

                Class<?> c=Class.forName(className);
                if (c.isAnnotationPresent(Controller.class)) {
                     for (Method m : c.getDeclaredMethods()) {
                        UrlMapping annotation = m.getAnnotation(UrlMapping.class);
                             if (annotation != null) {
                             URLMapping mapped=new URLMapping();
                             mapped.setC(c);
                             mapped.setMethods(m);
                            URLMethod um=new URLMethod(annotation.url(), annotation.method());
                            if (urlMap.containsKey(um)) {
                                throw new RuntimeException("L'URL: "+annotation.url()+" de methode:"+annotation.method()+ " dans le controller "+c.getSimpleName()+" est deja associe a un URL de meme methode");
                            }
                        urlMap.put(um, mapped);
                }  
                     }                    
                }


            }
        }
    }
  public static  Object invokeMethod(URLMapping mapping, WebApplicationContext springContext,HttpServletRequest request) throws Exception {
    Object controller = mapping.getC()
            .getDeclaredConstructor()
            .newInstance();
   
    Parameter[] parameters = mapping.getMethods().getParameters();
    Object[] args = new Object[parameters.length];
    
   for(int i=0;i<parameters.length;i++) {
    Class<?> type = parameters[i].getType();
    if(type.equals(ApplicationContext.class)) {
        args[i] = springContext;
        continue;
    }
  if(type.isArray()) {
      Class<?> componentType = type.getComponentType();
    

    
        String paramName = parameters[i].getName();
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
        remplirObjet(type,obj, request);
        args[i] = obj;
        continue;
}
    String paramName = parameters[i].getName();
    System.out.println("paramName: " + paramName);
    System.out.println("paramValue: " + request.getParameter(paramName));
    String paramValue = request.getParameter(paramName);

    if(paramValue == null) {
        args[i] = null;
    } else {
        args[i] = convert(paramValue, parameters[i].getType());
    }
   }

return mapping.getMethods().invoke(controller, args);
   
}
       private static void remplirObjet(
        Class<?> type,
        Object obj,
        HttpServletRequest request) {

    for (Method method : type.getMethods()) {

        if (method.getName().startsWith("set")
                && method.getParameterCount() == 1) {

            String setterName = method.getName();

            String fieldName = setterName.substring(3);

            fieldName = Character.toLowerCase(fieldName.charAt(0))
                    + fieldName.substring(1);

            String value = request.getParameter(fieldName);

            Class<?> setterType = method.getParameterTypes()[0];

            if (!setterType.isPrimitive()
                    && !setterType.equals(String.class)) {

                Object nestedObj = null;

                try {

                    nestedObj =
                            setterType.getDeclaredConstructor().newInstance();

                    remplirObjet(
                            setterType,
                            nestedObj,
                            request
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

                    Object convertedValue =
                            convert(value, setterType);

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

  public static  void renderView(Object result,URLMapping mapping,String prefix,String suffix, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  if(mapping.getMethods().isAnnotationPresent(Api.class)) {
    
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }
    else  if (result instanceof String) {
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

    return value;
}
}
