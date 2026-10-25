package mg.itu.rohymvc.listener;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSessionListener;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import mg.itu.rohymvc.session.MySession;
import mg.itu.rohymvc.session.SessionManager;
import mg.itu.rohymvc.url.URLMapping;
import mg.itu.rohymvc.url.URLMethod;
import mg.itu.rohymvc.utilitaire.Utils;
@WebListener
public class AppListener implements ServletContextListener, HttpSessionListener {
   
    
    @Override
    public void contextInitialized(ServletContextEvent sce){
         String scanPackage = sce.getServletContext().getInitParameter("packcontroller");
          System.out.println("=================================");
    System.out.println("packcontroller = " + scanPackage);
    System.out.println("=================================");
       Utils  utils = new Utils();
         HashMap<URLMethod, URLMapping> urlmapped=new HashMap<>();
        ConcurrentHashMap<String,MySession> sessionMap = new ConcurrentHashMap<>();
        try {
            utils.scanClassPath(scanPackage, urlmapped);
            sce.getServletContext().setAttribute("urlmappeds", urlmapped);
           sce.getServletContext().setAttribute(SessionManager.SESSIONS_KEY, sessionMap
);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    
    @Override
    public void contextDestroyed(ServletContextEvent sce){
      sce.getServletContext().removeAttribute("urlmappeds");
      sce.getServletContext().removeAttribute(SessionManager.SESSIONS_KEY);
    }
    @Override
public void sessionDestroyed(HttpSessionEvent event) {

    ServletContext context = event.getSession().getServletContext();

    @SuppressWarnings("unchecked")
    ConcurrentMap<String, MySession> sessions =
        (ConcurrentMap<String, MySession>)
            context.getAttribute(SessionManager.SESSIONS_KEY);

    if (sessions != null) {
        MySession session = sessions.remove(event.getSession().getId());

        if (session != null) {
            session.clear();
        }
    }
}
    
}
