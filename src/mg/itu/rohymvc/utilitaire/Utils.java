package mg.itu.rohymvc.utilitaire;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.rohymvc.annotation.Controller;
import mg.itu.rohymvc.annotation.UrlMapping;
import mg.itu.rohymvc.url.URLMapping;
import mg.itu.rohymvc.url.URLMethod;
import mg.itu.rohymvc.vue.ModelAndView;

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
  public static  Object invokeMethod(URLMapping mapping) throws Exception {
    Object controller = mapping.getC()
            .getDeclaredConstructor()
            .newInstance();

    return mapping.getMethods().invoke(controller);
}
  public static  void renderView(Object result,String prefix,String suffix, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (result instanceof String) {
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
}
