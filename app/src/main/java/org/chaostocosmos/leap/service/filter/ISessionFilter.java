package org.chaostocosmos.leap.service.filter;

import org.chaostocosmos.leap.manager.SessionManager;

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
