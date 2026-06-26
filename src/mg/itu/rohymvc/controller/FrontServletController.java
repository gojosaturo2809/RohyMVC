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
    HashMap<URLMethod, URLMapping> urlmapped;
    Utils utils;

    public void init() throws ServletException {

        String scanPackage = this.getInitParameter("packcontroller");
        utils = new Utils();
        urlmapped = new HashMap<>();

        try {
            utils.scanClassPath(scanPackage, urlmapped);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String route = uri.substring(contextPath.length());

        PrintWriter out = response.getWriter();
      try {
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<meta charset='UTF-8'>");
    out.println("<title>Front Controller</title>");
    out.println("</head>");
    out.println("<body>");

    out.println("<h1>Welcome to the Home Page</h1>");
    out.println("<hr>");

    URLMapping unique = urlmapped.get(new URLMethod(route, request.getMethod()));

    if (unique == null) {

        out.println("<h2>Registered Routes</h2>");

        out.println("<table border='1' cellpadding='5'>");
        out.println("<tr>");
        out.println("<th>URL</th>");
        out.println("<th>HTTP Method</th>");
        out.println("<th>Mapping</th>");
        out.println("</tr>");

        for (Map.Entry<URLMethod, URLMapping> entry : urlmapped.entrySet()) {

            URLMethod urlMethod = entry.getKey();
            URLMapping mapping = entry.getValue();

            out.println("<tr>");
            out.println("<td>" + urlMethod.getUrl() + "</td>");
            out.println("<td>" + urlMethod.getMethod() + "</td>");
            out.println("<td>" + mapping + "</td>");
            out.println("</tr>");
        }

        out.println("</table>");

    } else {

        out.println("<h2>Route Found</h2>");

        out.println("<p><b>URL :</b> " + route + "</p>");
        out.println("<p><b>HTTP Method :</b> " + request.getMethod() + "</p>");

        out.println("<h3>Mapping</h3>");
        out.println("<pre>" + unique + "</pre>");

        out.println("<h3>Method Result</h3>");
       Object result = utils.invokeMethod(unique);
        out.println("<h3>Method Return</h3>");
        out.println("<p>" + result.toString() + "</p>");
    }

    out.println("</body>");
    out.println("</html>");

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
