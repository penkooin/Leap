package org.chaostocosmos.leap.http.services.filters;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.context.User;
import org.chaostocosmos.leap.http.services.security.SecurityManager;
import org.chaostocosmos.leap.http.services.session.SessionManager;

/**
 * User authentication interface
 */
public interface IAuthenticate {
    /**
     * Sign in
     * @param username
     * @param password
     * @return
     * @throws WASException
     */
    public boolean signIn(String username, String password) throws WASException;

    /**
     * Sign up
     * @param user
     * @throws WASException
     */
    public void signUp(User user) throws WASException;
    
    /**
     * Set user manager object
     * @param userManager
     */
    public void setUserManager(SecurityManager userManager);

    /**
     * Set session manager object
     * @param sessionManager
     */
    public void setSessionManager(SessionManager sessionManager);
}
