package org.chaostocosmos.leap.http.service.filter;

/**
 * ISecurityFilter
 * 
 * @author 9ins
 */
public interface ISecurityFilter {
    /**
     * Set security manager object
     * @param securityManager
     */
    public void setSecurityManager(org.chaostocosmos.leap.http.security.SecurityManager securityManager);    

    /**
     * Get security manager object
     * @return
     */
    public org.chaostocosmos.leap.http.security.SecurityManager getSecurityManager();
}
