package mg.itu.rohymvc.controller;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.itu.rohymvc.dto.URLMapping;
import mg.itu.rohymvc.dto.URLMethod;
import mg.itu.rohymvc.utilitaire.Utils;



public class FrontServletController extends HttpServlet {
       HashMap<URLMethod,URLMapping> urlmapped;
       Utils utils;
    public void init() throws ServletException{
        
        String scanPackage = this.getInitParameter("packcontroller");
        utils=new Utils();
        urlmapped=new HashMap<>();
       
    
            try {
               utils.scanClassPath(scanPackage, urlmapped);
                    
        
            } catch (Exception e) {
               e.printStackTrace();
            }
          
        
       
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
      String uri = request.getRequestURI();
      String contextPath = request.getContextPath();
String route = uri.substring(contextPath.length());
       
           PrintWriter out = response.getWriter();
        try {
            out.println("<html><body>");
            out.println("<h1>Welcome to the Home Page</h1>");
            URLMapping unique=urlmapped.get(new URLMethod(route, request.getMethod()));
            out.println("<ol>");
            if (unique==null) {
                for (Map.Entry<URLMethod, URLMapping> entry : urlmapped.entrySet()) {
    URLMethod urlMethod = entry.getKey();
    URLMapping mapping = entry.getValue();
         out.println("<li>URL:"+urlMethod.getUrl()+" ");out.println(mapping+"</li>");
         if(urlMethod.getMethod().equals("POST")){
             out.println("<form method='POST' action='"+urlMethod.getUrl()+"'>");
                out.println("<input type='text' name='param1' placeholder='Enter param1'>");
                out.println("<input type='text' name='param2' placeholder='Enter param2'>");
             out.println("<input type='submit' value='Submit'>");
                out.println("</form>");
         }
}
            }
            else{
              out.println("URL:"+route+" ");out.println(unique);
            }
            out.println("</ol>");
            
         
           
            out.println("</body></html>");
    }      
        finally {
            out.close();
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
            }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
            }
}
