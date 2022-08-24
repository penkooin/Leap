package org.chaostocosmos.leap.http.services.filters;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.services.security.SecurityManager;
import org.chaostocosmos.leap.http.services.session.SessionManager;

/**
 * AbstractAuthFilter
 * 
 * @author 9ins
 */
public abstract class AbstractAuthFilter extends AbstractFilter<Request, Response> implements IAuthenticate {
    
    /**
     * Security manager object
     */
    protected SecurityManager userManager;

    /**
     * Session manager object
     */
    protected SessionManager sessionManager;


    @Override
    public void setUserManager(SecurityManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
