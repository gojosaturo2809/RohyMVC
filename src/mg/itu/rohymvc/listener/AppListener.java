package mg.itu.rohymvc.listener;
import jakarta.servlet.ServletContextListener;

import java.util.HashMap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import mg.itu.rohymvc.dto.URLMapping;
import mg.itu.rohymvc.dto.URLMethod;
import mg.itu.rohymvc.utilitaire.Utils;
@WebListener
public class AppListener implements ServletContextListener {
   
    
    @Override
    public void contextInitialized(ServletContextEvent sce){
         String scanPackage = sce.getServletContext().getInitParameter("packcontroller");
       Utils  utils = new Utils();
         HashMap<URLMethod, URLMapping> urlmapped=new HashMap<>();

        try {
            utils.scanClassPath(scanPackage, urlmapped);
            sce.getServletContext().setAttribute("urlmappeds", urlmapped);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce){
      sce.getServletContext().removeAttribute("urlmappeds");
    }
}
