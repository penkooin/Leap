package org.chaostocosmos.leap.http.filters;

import org.chaostocosmos.leap.http.services.IJPAModel;
import org.chaostocosmos.leap.http.user.UserManager;

/**
 * Top model of Leap filters
 * 
 * @author 9ins
 */
public interface ILeapFilter extends IJPAModel {
    /**
     * Set security manager object
     * @param securityManager
     */
    public void setUserManager(UserManager securityManager);
}
