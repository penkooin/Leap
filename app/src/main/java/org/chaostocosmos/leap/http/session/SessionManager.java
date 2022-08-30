package org.chaostocosmos.leap.http.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.common.DateUtils;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.security.SessionIDGenerator;

/**
 * SessionManager
 * 
 * @author 9ins
 */
public class SessionManager {
    /**
     * Host object
     */
    final Host<?> host;

    /**
     * Session Map
     */
    final Map<String, Session> sessionMap = Collections.synchronizedMap(new HashMap<String, Session>());
    
    /**
     * Constructs with Host
     * @param host
     */
    public SessionManager(Host<?> host) {
        this.host = host;
    }

    /**
     * Get whether session support
     * @return
     */
    public boolean isApplySession() {
        return this.host.getApplySession();
    }

    /**
     * Set apply session support flag
     * @param applySession
     */
    public void setApplySession(boolean applySession) {
        this.host.setApplySession(applySession);
    }

    /**
     * Whether request exists in sessions
     * @param request
     * @return
     */
    public boolean exists(Request request) {
        String sessionId = request.getCookie("__Leap-Session-ID");
        if(this.sessionMap.containsKey(sessionId)) {
            return true;
        }
        return false;
    }

    /**
     * Get session by session ID
     * @param sessionId
     * @return
     */
    public Session getSession(String sessionId) {
        if(sessionId == null) {
            return null;
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
        long lastAccessedTime = creationTime;
        int maxInteractiveInteralSecond = Context.getHost(this.host.getHostId()).<Integer> getSessionTimeout() / 1000;
        String sessionId = SessionIDGenerator.get(this.host.<String> getHostId()).generateSessionId(idLength);
        Session session = new HttpSession(sessionId, creationTime, lastAccessedTime, maxInteractiveInteralSecond, request); 
        session.setAttribute("Expires", DateUtils.getDateGMT(System.currentTimeMillis() + 1000 * this.host.<Integer>getExpires(), "yyyy/MM/dd HH:mm:ss Z"));
        session.setAttribute("Max-Age", this.host.<Integer> getSessionTimeout());
        session.setAttribute("Path", this.host.<String> getPath());
        return session;
    }

    /**
     * Add session
     * @param session
     */
    public void addSession(Session session) {
        this.sessionMap.put(session.getId(), session);
    }

    /**
     * Close session
     * @param session
     */
    public void closeSession(Session session) {
        removeSession(session.getId());
    }

    /**
     * Remove session
     * @param session
     */
    public void removeSession(String sessionId) {
        if(this.sessionMap.containsKey(sessionId)) {
            this.sessionMap.remove(sessionId).close();
        }
    }

    /**
     * Clear SessionManager
     */
    public void reset() {
        this.sessionMap.values().stream().forEach(s -> closeSession(s));
        this.sessionMap.clear();
    }

    @Override
    public String toString() {
        return "{" +
            " host='" + host + "'" +
            ", sessionMap='" + sessionMap + "'" +
            "}";
    }    
}

