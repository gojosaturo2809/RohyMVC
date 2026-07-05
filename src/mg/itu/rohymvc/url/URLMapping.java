package mg.itu.rohymvc.url;

import java.lang.reflect.Method;

public class URLMapping {
    Class<?> c;
    Method methods;
    public URLMapping() {
    }
    public Class<?> getC() {
        return c;
    }
    public void setC(Class<?> c) {
        this.c = c;
    }
    public Method getMethods() {
        return methods;
    }
    public void setMethods(Method methods) {
        this.methods = methods;
    }
    @Override
    public String toString() {
        return "URLMapping [Classe=" + c.getSimpleName() + ", methodes=" + methods.getName() + "]";
    }
   
}
