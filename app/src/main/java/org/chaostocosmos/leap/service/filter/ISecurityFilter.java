package org.chaostocosmos.leap.service.filter;

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
    public void setSecurityManager(SecurityManager securityManager);    

    /**
     * Get security manager object
     * @return
     */
    public SecurityManager getSecurityManager();
}
