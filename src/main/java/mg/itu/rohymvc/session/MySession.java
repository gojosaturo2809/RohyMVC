package mg.itu.rohymvc.session;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

public class MySession {
    private Map<String, Object> sessionData;

    public MySession() {
        sessionData = new java.util.concurrent.ConcurrentHashMap<>();
    }

    public MySession(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    public void persistSessionData(HttpSession session) {
        for (Map.Entry<String, Object> entry : sessionData.entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void loadSessionData(HttpSession session) {
        while (session.getAttributeNames().hasMoreElements()) {
            String key = session.getAttributeNames().nextElement();
            Object value = session.getAttribute(key);
            sessionData.put(key, value);
        }
    }
    public void clearSessionData(HttpSession session) {
        sessionData.clear();
        session.invalidate();
    }
    public void clear() {
        sessionData.clear();
    }
}