package mg.itu.rohymvc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.rohymvc.utilitaire.Utils;



public class FrontServletController extends HttpServlet {
    ArrayList<String> controllerNames;
    public void init() throws ServletException{
        String scanPackage = this.getInitParameter("packcontroller");
        Utils utils=new Utils();
        controllerNames=new ArrayList<>();
        ArrayList<Class<?>> classes;
    
            try {
                classes = utils.findController(scanPackage);
                   for (Class<?> class1 : classes) {
           controllerNames.add(class1.getSimpleName());   
        }
            } catch (Exception e) {
               
            }
          
        
       
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
           PrintWriter out = response.getWriter();
        try {
            out.println("<html><body>");
            out.println("<h1>Welcome to the Home Page</h1>");
         
            for (String string : controllerNames) {
            out.println(string+"\n");
            }
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
