package org.chaostocosmos.leap.http.session;

import java.net.URI;
import java.util.Enumeration;

import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.PROTOCOL;

/**
 * Session model interface
 * 
 * @author 9ins
 */
public interface Session {   
    /**
     * Get Host
     * @return
     */ 
    public Host<?> getHost();

    /**
     * Whether the session is authenticated
     * @return
     */
    public boolean isAuthenticated();

    /**
     * Set whether authenticated
     * @param isAuthenticated
     */
    public void setAuthenticated(boolean isAuthenticated);
    
    /**
     * Get attribute name
     * @param attrName
     * @return
     */
    public Object getAttribute(String attrName);

    /**
     * Get attribute names
     * @return
     */
    public Enumeration<String> getAttributeNames();

    /**
     * Get creation time
     * @return
     */
    public long getCreationTime();

    /**
     * Get session ID
     * @return
     */
    public String getId();

    /**
     * Get time of last accessed
     * @return
     */
    public long getLastAccessedTime();

    /**
     * Get max inactive interval
     * @return
     */
    public int getMaxInactiveIntervalSecond();

    /**
     * Get context path
     * @return
     */
    public String getContextPath();

    /**
     * Get request URI
     * @return
     */
    public URI getRequestURI();

    /**
     * Invalidate the session
     */
    public void invalidate();

    /**
     * Whether the session is new session
     * @return
     */
    public boolean isNew();

    /**
     * Set isNew attribute
     * @param isNew
     */
    public void setNew(boolean isNew);

    /**
     * Set attribute of the session
     * @param attrName
     * @param value
     */
    public void setAttribute(String attrName, Object value);

    /**
     * Get maximum interactive interval
     * @param interval
     */
    public void setMaxInactiveIntervalSecond(int interval);

    /**
     * Get protocol
     * @return
     */
    public PROTOCOL getProtocol();

    /**
     * Whether the session is secured
     * @return
     */
    public boolean isSecure();

    /**
     * Close session
     */
    public void close();
}
