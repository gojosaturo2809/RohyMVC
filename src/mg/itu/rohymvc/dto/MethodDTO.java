package mg.itu.rohymvc.dto;

public class MethodDTO {
     String url;
     String controller;
     String methodname;
     
     public MethodDTO() {
    }
     public MethodDTO(String url, String controller, String methodname) {
        this.url = url;
        this.controller = controller;
        this.methodname = methodname;
    }
     public String getUrl() {
         return url;
     }
     public void setUrl(String url) {
         this.url = url;
     }
     public String getController() {
         return controller;
     }
     public void setController(String controller) {
         this.controller = controller;
     }
     public String getMethodname() {
         return methodname;
     }
     public void setMethodname(String methodname) {
         this.methodname = methodname;
     }
     @Override
     public String toString() {
        return " [url=" + url + ", controller=" + controller + ", methodname=" + methodname + "]";
     }
}
