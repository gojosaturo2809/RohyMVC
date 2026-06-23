package mg.itu.rohymvc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.rohymvc.dto.MethodDTO;
import mg.itu.rohymvc.utilitaire.Utils;



public class FrontServletController extends HttpServlet {
       ArrayList<MethodDTO> mtdo;
       Utils utils;
    public void init() throws ServletException{
        
        String scanPackage = this.getInitParameter("packcontroller");
        utils=new Utils();
        mtdo=new ArrayList<>();
       
    
            try {
                mtdo = utils.findAllAnnotedMethods(scanPackage);
                    
        
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
         ArrayList<MethodDTO> mdo=utils.getMethodByUrl(route, mtdo);
         if (mdo.isEmpty()) {
            mdo.addAll(mtdo);
         }
         out.println("<ol>");
         for (MethodDTO  methode : mdo) {
               out.println("<li>"+methode+"</li>");
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
