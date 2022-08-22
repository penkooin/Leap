package org.chaostocosmos.leap.http.security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.services.session.HttpSession;
import org.chaostocosmos.leap.http.services.session.Session;

/**
 * SessionManager
 * 
 * @author 9ins
 */
public class SessionManager {
    /**
     * Host object
     */
    Host<?> host;

    /**
     * Session Map
     */
    Map<String, Session> sessionMap;
    
    /**
     * Constructs with Host
     * @param host
     */
    public SessionManager(Host<?> host) {
        this.host = host;
        this.sessionMap = Collections.synchronizedMap(new HashMap<String, Session>());
    }

    /**
     * Get session
     * @param request
     * @return
     */
    public Session getSession(Request request) {
        String sessionId = request.getCookies("sessionid");
        if(sessionId == null) {
            return createSession(request);
        }
        return this.sessionMap.get(sessionId);
    }

    /**
     * Create session
     * @param request
     * @return
     */
    public Session createSession(Request request) {
        int idLength = Context.getHost(this.host.getHostId()).getSessionIDLength();
        long creationTime = System.currentTimeMillis();
        int maxInteractiveInteralSecond = Context.getHost(this.host.getHostId()).<Integer> getSessionTimeout() / 1000;
        String sessionId = SessionIDGenerator.get(this.host.<String> getHostId()).generateSessionId(idLength);
        Session session = new HttpSession(sessionId, creationTime, -1L, maxInteractiveInteralSecond, request); 
        this.sessionMap.put(sessionId, session);
        return session;
    }

    public void closeSession(Session session) throws IOException {
        session.close();
    }

    public void clear() {
        
    }
}
