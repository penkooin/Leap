package org.chaostocosmos.leap.http.services.filters;

import org.chaostocosmos.leap.http.UserManager;

/**
 * Top level of Filter
 * 
 * @author 9ins
 * @since 2021.09.17
 */
public interface IFilter {
    /**
     * Set security manager object
     * @param securityManager
     */
    public void setUserManager(UserManager securityManager);
}
