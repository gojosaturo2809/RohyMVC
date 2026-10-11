package mg.itu.rohymvc.controller;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.rohymvc.url.URLMapping;
import mg.itu.rohymvc.url.URLMethod;
import mg.itu.rohymvc.utilitaire.Utils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class FrontServletController extends HttpServlet {
    HashMap<URLMethod, URLMapping> urlmapped;
    String prefix = "/WEB-INF/views/";
    String suffix = ".jsp";
    private WebApplicationContext springContext;

    public void init() throws ServletException {
        urlmapped = (HashMap<URLMethod, URLMapping>) this.getServletContext().getAttribute("urlmappeds");
        prefix = this.getServletContext().getInitParameter("prefix");
        suffix = this.getServletContext().getInitParameter("suffix");
        ServletContext servletContext = getServletContext();

        springContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType("text/html;charset=UTF-8");

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String route = uri.substring(contextPath.length());

        PrintWriter out = response.getWriter();
        try {
          

            URLMapping unique = urlmapped.get(new URLMethod(route, request.getMethod()));

            if (unique == null) {

                for (Map.Entry<URLMethod, URLMapping> entry : urlmapped.entrySet()) {

                    URLMethod urlMethod = entry.getKey();
                    URLMapping mapping = entry.getValue();

                    out.println("<tr>");
                    out.println("<td>" + urlMethod.getUrl() + "</td>");
                    out.println("<td>" + urlMethod.getMethod() + "</td>");

                    out.println("<td>" + mapping + "</td>");
                    out.println("</tr>");
                }


            } else {

                
                Object result = Utils.invokeMethod(unique, springContext, request);

                Utils.renderView(result,unique, prefix, suffix, request, response);
            }

          

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
