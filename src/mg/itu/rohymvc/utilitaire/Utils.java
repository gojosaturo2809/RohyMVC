package mg.itu.rohymvc.utilitaire;

import java.io.File;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import mg.itu.rohymvc.annotation.Controller;



public class Utils {
   
    
   public ArrayList<Class<?>>  scanClassPath(String packageName) throws Exception {

    String[] packages = packageName.split(";");
     ArrayList<Class<?>>    classes = new ArrayList<>();

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    for (String pkg : packages) {

        String path = pkg.replace('.', '/');

        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {

            URL resource = resources.nextElement();

            File directory = new File(resource.toURI());

            if (!directory.exists()) {
                continue;
            }
            scanDirectory(directory, packageName, classes);
        }
    }
   return classes;

}
private void scanDirectory(
        File directory,
        String packageName,
        ArrayList<Class<?>> classes)
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
                classes
            );

        } else if (file.getName().endsWith(".class")) {

            String className =
                packageName + "."
                + file.getName().replace(".class", "");

            classes.add(Class.forName(className));
        }
    }
}
public ArrayList<Class<?>> findController(String packageName) throws Exception{
      ArrayList<Class<?>> classes=   this.scanClassPath(packageName);
        ArrayList<Class<?>> listcontrollers=new ArrayList<>();
        for (Class<?> class1 : classes) {
            if (class1.isAnnotationPresent(Controller.class)) {
             listcontrollers.add(class1);    
            }
        }
       return listcontrollers;
}
}
