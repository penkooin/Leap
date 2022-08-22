package org.chaostocosmos.leap.http.services.filters;

import java.net.MalformedURLException;
import java.net.URL;

import org.chaostocosmos.leap.http.UserManager;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.resources.SpringJPAManager;
import org.chaostocosmos.leap.http.services.servicemodel.SpringJPAModel;

/**
 * Filtering request URL
 * @author 9ins
 */
public abstract class AbstractFilter<R, S> implements IHttpFilter<R, S>, SpringJPAModel {

    /**
     * Security manager object
     */
    protected UserManager userManager;

    @Override
    @PreFilter
    public void filterRequest(R r) throws Exception {
        //System.out.println("Processing pre filter..........");
    }

    @Override
    @PostFilter
    public void filterResponse(S s) throws Exception {
        //System.out.println("Processing post filter..........");
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
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public <T> T getBean(String beanName, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanName, args);
    }

    @Override
    public <T> T getBean(Class<?> beanClass, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanClass, args);
    }    
}
