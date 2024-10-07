package org.chaostocosmos.leap.filter;

import java.net.MalformedURLException;
import java.net.URL;

import org.chaostocosmos.leap.service.model.SpringJPAModel;
import org.chaostocosmos.leap.session.SessionManager;
import org.chaostocosmos.leap.spring.SpringJPAManager;

/**
 * Filtering request URL
 * 
 * @author 9ins
 */
public abstract class AbstractRequestFilter<F> implements IRequestFilter<F>, SpringJPAModel {

    /**
     * Session manager object
     */
    SessionManager sessionManager;

    @Override
    public void filterRequest(F request) throws Exception {        
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
    public <B> B getBean(String beanName, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanName, args);
    }

    @Override
    public <B> B getBean(Class<?> beanClass, Object... args) throws Exception {
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
