package org.chaostocosmos.leap.http.filters;

import java.net.MalformedURLException;
import java.net.URL;

import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.user.UserManager;

/**
 * Filtering request URL
 * @author 9ins
 */
public abstract class AbstractHttpFilter<R, S> implements IHttpFilter<R, S> {

    /**
     * Security manager object
     */
    protected UserManager securityManager;

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
    public void setSecurityManager(UserManager securityManager) {
        this.securityManager = securityManager;
    }
}
