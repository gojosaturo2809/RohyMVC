package mg.itu.rohymvc.utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;


import mg.itu.rohymvc.annotation.Controller;
import mg.itu.rohymvc.annotation.UrlMapping;
import mg.itu.rohymvc.dto.URLMapping;

public class Utils {

    public void scanClassPath(String packageName,HashMap<String,URLMapping> urlmap) throws Exception {

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
            HashMap<String,URLMapping> urlMap
            )
            throws Exception {

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
                        urlMap.put(annotation.url(), mapped);
                }  
                     }                    
                }


            }
        }
    }
}
