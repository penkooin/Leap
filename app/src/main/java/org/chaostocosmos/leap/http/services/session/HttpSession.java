package org.chaostocosmos.leap.http.services.session;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * WebSession
 * 
 * @author 9ins
 */
public class HttpSession implements Session {

    final Map<String, Object> attributeMap = new HashMap<>();
    final Request request;
    String sessionId;
    long creationTime; 
    long lastAccessedTime;
    int maxInteractiveInteralSecond;
    boolean isNew;
    boolean isSecure;

    /**
     * Constructs with session id, creation millis, last access millis, max interactive second and request object
     * @param sessionId
     * @param creationTime
     * @param lastAccessedTime
     * @param maxInteractiveInteralSecond
     * @param request
     */
    public HttpSession(String sessionId, long creationTime, long lastAccessedTime, int maxInteractiveInteralSecond, Request request) {
        this.request = request;
        this.sessionId = sessionId;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInteractiveInteralSecond = maxInteractiveInteralSecond;
        this.isSecure = request.getProtocol() == PROTOCOL.HTTPS ? true : false;
        this.isNew = true;
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
        return this.maxInteractiveInteralSecond;
    }

    @Override
    public void getMaxInactiveIntervalSecond(int interval) {
        this.maxInteractiveInteralSecond = interval;        
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
    public void close() throws IOException {
        throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES500.code(), "This connection is unexpected to close.");
    }

    @Override
    public String toString() {
        return "{" +
            " protocol='" + getProtocol() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", sessionId='" + sessionId + "'" +
            ", creationTime='" + creationTime + "'" +
            ", lastAccessedTime='" + lastAccessedTime + "'" +
            ", maxInteractiveInteralSecond='" + maxInteractiveInteralSecond + "'" +
            ", isNew='" + isNew + "'" +
            ", isSecure='" + isSecure + "'" +
            ", attributeMap='" + attributeMap + "'" +
            "}";
    }    
}
