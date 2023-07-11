package org.chaostocosmos.leap.service.filter;

import java.net.MalformedURLException;
import java.net.URL;

import org.chaostocosmos.leap.SpringJPAManager;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.service.model.SpringJPAModel;
import org.chaostocosmos.leap.session.SessionManager;

/**
 * Filtering request URL
 * 
 * @author 9ins
 */
public abstract class AbstractRequestFilter implements IRequestFilter<HttpRequest>, SpringJPAModel {
    /**
     * Session manager object
     */
    SessionManager sessionManager;

    @Override
    public void filterRequest(HttpRequest request) throws Exception {        
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
