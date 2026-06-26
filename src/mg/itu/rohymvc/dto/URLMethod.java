package mg.itu.rohymvc.dto;

import java.util.Objects;

public class URLMethod {
    String url;
    String method;
    public URLMethod(String url, String method) {  
        this.url = url;
            this.method = method;
        }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        URLMethod other = (URLMethod) obj;
        return url.equals(other.url) && method.equals(other.method);
    }
    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

}
