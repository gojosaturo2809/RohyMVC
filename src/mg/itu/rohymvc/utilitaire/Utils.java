package mg.itu.rohymvc.utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import mg.itu.rohymvc.annotation.Controller;
import mg.itu.rohymvc.annotation.UrlMapping;
import mg.itu.rohymvc.dto.MethodDTO;



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
            scanDirectory(directory, pkg, classes);
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
public ArrayList<MethodDTO> findAnnotedMethod(Class <?> c) throws Exception{
      ArrayList<MethodDTO> methods=new ArrayList<>();
      try {
           Method[] mt=c.getDeclaredMethods();
           for (Method m : mt) {
              if (isAnnotedMethod(m)) {
                  MethodDTO mdto=new MethodDTO();
                  mdto.setController(c.getSimpleName());
                  mdto.setMethodname(m.getName());
                  mdto.setUrl(m.getAnnotation(UrlMapping.class).url());
                  methods.add(mdto);
              }
              else{
                continue;
              }
           } 


      } catch (Exception e) {
       throw e;
      }
      return methods;
     
}
private boolean isAnnotedMethod(Method m){
      return m.isAnnotationPresent(UrlMapping.class);
      
}
public ArrayList<MethodDTO> findAllAnnotedMethods(String packageName) throws Exception{
      ArrayList<MethodDTO> retours=new ArrayList<>();
      ArrayList<Class<?>> classes=findController(packageName);
        for (Class<?> class1 : classes) {
            retours.addAll(findAnnotedMethod(class1));
        }
     return retours;
}
public ArrayList<MethodDTO> getMethodByUrl(String url,ArrayList<MethodDTO> dto)
{
    ArrayList<MethodDTO> mdto=new ArrayList<>();
        for (MethodDTO methodDTO : dto) {
            if(url.equals(methodDTO.getUrl())){
                mdto.add(methodDTO);
            }
        }
    return mdto;
}
}
