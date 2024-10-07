package org.chaostocosmos.leap.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpResponse;

/**
 * WebSession
 * 
 * @author 9ins
 */
public class HttpSession implements Session {

    /**
     * Host 
     */
    final Host<?> host;    

    /**
     *  Http session attribute Map
     */    
    final Map<String, Object> attributeMap = new HashMap<>();

    /**
     * Session manager
     */
    final SessionManager sessionManager;

    /**
     * Session ID
     */
    String sessionId;

    /**
     * Session creation time
     */
    long creationTime; 

    /**
     * Last session access time
     */
    long lastAccessedTime;

    /**
     * Max in-active interval second
     */
    int maxInactiveIntervalSecond;

    /**
     * Whether new session
     */
    boolean isNew;

    /**
     * Whether secured session
     */
    boolean isSecure;

    /**
     * Whether the session authenticated
     */
    boolean isAuthenticated;

    /**
     * Constructs with session id, creation millis, last access millis, max interactive second and request object
     * @param sessionManager
     * @param sessionId
     * @param creationTime
     * @param lastAccessedTime
     * @param maxInactiveIntervalSecond
     * @param request
     */
    public HttpSession(SessionManager sessionManager, String sessionId, long creationTime, long lastAccessedTime, int maxInactiveIntervalSecond) {
        this.sessionManager = sessionManager;
        this.host = this.sessionManager.getHost();
        this.sessionId = sessionId;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveIntervalSecond = maxInactiveIntervalSecond;
        this.isSecure = this.host.getProtocol() == PROTOCOL.HTTPS ? true : false;
        this.isAuthenticated = false;
        this.isNew = true;
    }

    @Override
    public Host<?> getHost() {
        return this.host;
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public Object getAttribute(String attrName) {
        return this.attributeMap.get(attrName);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributeMap.keySet());
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public void setLastAccessedTime(long timeMillis) {
        this.lastAccessedTime = timeMillis;
    }

    @Override
    public void invalidate() {
        this.sessionManager.removeSession(this.sessionId);
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @Override
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public void setAttribute(String attrName, Object value) {        
        this.attributeMap.put(attrName, value);
    }

    @Override
    public int getMaxInactiveIntervalSecond() {
        return this.maxInactiveIntervalSecond;
    }

    @Override
    public void setMaxInactiveIntervalSecond(int interval) {
        this.maxInactiveIntervalSecond = interval;
    }

    @Override
    public PROTOCOL getProtocol() {
        return this.host.getProtocol();
    }

    @Override
    public boolean isSecure() {
        return getProtocol().isSecured();
    }

    @Override
    public void close() {
        throw new LeapException(HTTP.RES500, "This connection is unexpected to close.");
    }

    @Override
    public <R> HttpResponse<R> setSessionToResponse(HttpResponse<R> response) {
        Object maxAge = getAttribute("Max-Age");
        Object expires = getAttribute("Expires");
        Object path = getAttribute("Path");
        response.addSetCookie(Constants.SESSION_ID_KEY, getId());
        // if(maxAge != null)  response.addSetCookie("Max-Age", maxAge.toString());
        // if(expires != null) response.addSetCookie("Expires", expires.toString());
        // if(path != null)    response.addSetCookie("Path", path.toString());
        return response;
    }

    @Override
    public String toString() {
        return "{" +
            " host='" + host + "'" +
            ", attributeMap='" + attributeMap + "'" +
            ", sessionManager='" + sessionManager + "'" +
            ", sessionId='" + sessionId + "'" +
            ", creationTime='" + creationTime + "'" +
            ", lastAccessedTime='" + lastAccessedTime + "'" +
            ", maxInactiveIntervalSecond='" + maxInactiveIntervalSecond + "'" +
            ", isNew='" + isNew + "'" +
            ", isSecure='" + isSecure + "'" +
            ", isAuthenticated='" + isAuthenticated + "'" +
            "}";
    }    
}
