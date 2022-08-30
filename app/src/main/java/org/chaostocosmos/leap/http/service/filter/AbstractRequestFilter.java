package org.chaostocosmos.leap.http.service.filter;

import java.net.MalformedURLException;
import java.net.URL;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.PostFilterIndicates;
import org.chaostocosmos.leap.http.annotation.PreFilterIndicates;
import org.chaostocosmos.leap.http.resource.SpringJPAManager;
import org.chaostocosmos.leap.http.service.model.SpringJPAModel;
import org.chaostocosmos.leap.http.session.SessionManager;

/**
 * Filtering request URL
 * 
 * @author 9ins
 */
public abstract class AbstractRequestFilter implements IRequestFilter<Request>, SpringJPAModel {
    /**
     * Session manager object
     */
    SessionManager sessionManager;

    @Override
    @PreFilterIndicates
    public void filterRequest(Request request) throws Exception {        
    }

    @Override
    public boolean isValidURL(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    @Override
    public <T> T getBean(String beanName, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanName, args);
    }

    @Override
    public <T> T getBean(Class<?> beanClass, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanClass, args);
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }
}
