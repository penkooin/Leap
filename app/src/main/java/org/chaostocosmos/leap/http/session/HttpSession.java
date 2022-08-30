package org.chaostocosmos.leap.http.session;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * WebSession
 * 
 * @author 9ins
 */
public class HttpSession implements Session {
    /**
     *  Http session attribute Map
     */    
    final Map<String, Object> attributeMap = new HashMap<>();

    /** 
     * Http request
     */
    final Request request;

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
     * @param sessionId
     * @param creationTime
     * @param lastAccessedTime
     * @param maxInactiveIntervalSecond
     * @param request
     */
    public HttpSession(String sessionId, long creationTime, long lastAccessedTime, int maxInactiveIntervalSecond, Request request) {
        this.request = request;
        this.sessionId = sessionId;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveIntervalSecond = maxInactiveIntervalSecond;
        this.isSecure = request.getProtocol() == PROTOCOL.HTTPS ? true : false;
        this.isNew = true;
        this.isAuthenticated = false;
    }

    @Override
    public Host<?> getHost() {
        return Context.getHost(this.request.getHostId());
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
    public String getContextPath() {
        return this.request.getContextPath();
    }

    @Override
    public URI getRequestURI() {
        return this.request.getRequestURI();
    }

    @Override
    public void invalidate() {
    }

    @Override
    public boolean isNew() {
        return this.isNew;
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
    public void getMaxInactiveIntervalSecond(int interval) {
        this.maxInactiveIntervalSecond = interval;
    }

    @Override
    public PROTOCOL getProtocol() {
        return this.request.getProtocol();
    }

    @Override
    public boolean isSecure() {
        return getProtocol().isSecured();
    }

    @Override
    public void close() {
        throw new HTTPException(RES_CODE.RES500, "This connection is unexpected to close.");
    }

    @Override
    public String toString() {
        return "{" +
            " protocol='" + getProtocol() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", sessionId='" + sessionId + "'" +
            ", creationTime='" + creationTime + "'" +
            ", lastAccessedTime='" + lastAccessedTime + "'" +
            ", maxInteractiveInteralSecond='" + maxInactiveIntervalSecond + "'" +
            ", isNew='" + isNew + "'" +
            ", isSecure='" + isSecure + "'" +
            ", attributeMap='" + attributeMap + "'" +
            "}";
    }    
}
