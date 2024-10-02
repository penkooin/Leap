package org.chaostocosmos.leap.filter;

import org.chaostocosmos.leap.session.SessionManager;

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
