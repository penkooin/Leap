package org.chaostocosmos.leap.http.session;

import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.common.DateUtils;
import org.chaostocosmos.leap.http.common.UNIT;
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
    final Map<String, Session> sessionMap = new HashMap<String, Session>();
    
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
    public synchronized boolean exists(Request request) {
        String sessionId = request.getCookie(Constants.SESSION_ID_KEY);
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
    public synchronized Session getSessionCreateIfNotExists(Request request) {
        String sessionId = request.getCookie(Constants.SESSION_ID_KEY);                
        if(this.sessionMap.containsKey(sessionId)) {
            return this.sessionMap.get(sessionId);
        } else {
            Session session = createSession(request);
            this.addSession(session);
            return session;
        }
    }

    /**
     * Create session
     * @param request
     * @return
     */
    public synchronized Session createSession(Request request) {
        int idLength = Context.getHost(this.host.getHostId()).getSessionIDLength();
        long creationTime = System.currentTimeMillis();
        long lastAccessedTime = creationTime;
        int maxInteractiveInteralSecond = Context.getHost(this.host.getHostId()).<Integer> getSessionTimeout();
        String sessionId = SessionIDGenerator.get(this.host.<String> getHostId()).generateSessionId(idLength);
        Session session = new HttpSession(this, sessionId, creationTime, lastAccessedTime, maxInteractiveInteralSecond, request); 
        session.setAttribute("Expires", DateUtils.getDateLocalAddedOffset(System.currentTimeMillis() + this.host.<Integer>getExpires() * (int) UNIT.DY.getUnit()));
        session.setAttribute("Max-Age", this.host.<Integer> getMaxAge());
        session.setAttribute("Path", this.host.<String> getPath());
        return session;
    }

    /**
     * Add session
     * @param session
     */
    public synchronized void addSession(Session session) {
        this.sessionMap.put(session.getId(), session);
    }

    /**
     * Remove session
     * @param session
     */
    public synchronized boolean removeSession(String sessionId) {
        if(this.sessionMap.containsKey(sessionId)) {
            this.sessionMap.remove(sessionId);
            return true;
        }
        return false;
    }

    /**
     * Close session
     * @param session
     */
    public synchronized boolean removeSession(Session session) {
        return removeSession(session.getId());
    }

    /**
     * Clear SessionManager
     */
    public void reset() {
        this.sessionMap.values().stream().forEach(s -> removeSession(s));
        this.sessionMap.clear();
    }

    /**
     * Get session Map
     * @return
     */
    public Map<String, Session> getSessionMap() {
        return this.sessionMap;
    }

    @Override
    public String toString() {
        return "{" +
            " host='" + host + "'" +
            ", sessionMap='" + sessionMap + "'" +
            "}";
    }    
}

