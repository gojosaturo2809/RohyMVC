package mg.itu.rohymvc.session;


import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionManager {

    public static final String SESSIONS_KEY =
        SessionManager.class.getName() + ".SESSIONS";

    private SessionManager() {
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentMap<String, MySession> getSessions(
            ServletContext context) {

        Object objet = context.getAttribute(SESSIONS_KEY);

        if (objet == null) {
            throw new IllegalStateException(
                "SessionManager n'est pas initialisé"
            );
        }

        return (ConcurrentMap<String, MySession>) objet;
    }

    public static MySession getSession(HttpServletRequest request) {

        HttpSession httpSession = request.getSession(true);

        String id = httpSession.getId();

        ConcurrentMap<String, MySession> sessions =
            getSessions(request.getServletContext());

        return sessions.computeIfAbsent(
            id,
            cle -> new MySession()
        );
    }

    public static void invalidate(HttpServletRequest request) {

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null) {
            httpSession.invalidate();
        }
    }
    
}