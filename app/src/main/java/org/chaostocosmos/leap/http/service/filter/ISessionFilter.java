package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.session.SessionManager;

/**
 * ISessionFilter
 * 
 * @author 9ins
 */
public interface ISessionFilter {   
    /**
     * Set session manager object
     * @param sessionManager
     */
    public void setSessionManager(SessionManager sessionManager);

    /**
     * Get session manager
     * @return
     */
    public SessionManager getSessionManager();
}
