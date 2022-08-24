package org.chaostocosmos.leap.http.services.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.services.security.SessionIDGenerator;

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
    Map<String, Session> sessionMap = Collections.synchronizedMap(new HashMap<String, Session>());
    
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
        return host.getApplySession();
    }

    /**
     * Set apply session support flag
     * @param applySession
     */
    public void setApplySession(boolean applySession) {
        host.setApplySession(applySession);
    }

    /**
     * Whether request exists in sessions
     * @param request
     * @return
     */
    public boolean exists(Request request) {
        String sessionId = request.getCookies("LEAP-SESSION-ID");
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
        if(!this.sessionMap.containsKey(sessionId)) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES511.code(), "Specified session ID not exist. Log in required.");
        }
        return this.sessionMap.get(sessionId);
    }

    /**
     * Get session or if not exist, create new session
     * @param request
     * @return
     */
    public void setSession(Request request) {
        String sessionId = request.getCookies("LEAP-SESSION-ID");
        Session session = getSession(sessionId);
        if(System.currentTimeMillis() - session.getLastAccessedTime() > session.getMaxInactiveIntervalSecond() * 1000L) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES503.code(), "Over the session timeout seconds.");
        }
        request.setSession(session);
    }

    /**
     * Create session
     * 
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
        this.sessionMap.put(sessionId, session);
        return session;
    }

    /**
     * Close session
     * 
     * @param session
     */
    public void closeSession(Session session) {
        removeSession(session.getId());
    }

    /**
     * Remove session
     * 
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
}

