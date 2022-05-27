package org.chaostocosmos.leap.http.services.filters;

import java.net.InetAddress;
import java.util.List;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.UserManager;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.resources.SpringJPAManager;

/**
 * Abstract ip filtering object
 * @author 9ins
 */
public abstract class AbstractIpFilter<R> implements IIpFilter<R> {

    /**
     * Security manager object
     */
    protected UserManager securityManager;
    /**
     * Allowed / forbidden hosts
     */
    List<InetAddress> allowedHosts, forbiddenHosts;

    /**
     * Constructor with allowed / forbidden hosts
     * @param allowedHosts
     * @param forbiddenHosts
     */
    public AbstractIpFilter(List<InetAddress> allowedHosts, List<InetAddress> forbiddenHosts) {
        this.allowedHosts = allowedHosts;
        this.forbiddenHosts = forbiddenHosts;
    }
    
    @Override
    @PreFilter
    public boolean allowedHost(R r) {
        if(this.allowedHosts != null && r.getClass().isAssignableFrom(Request.class)) {
            return this.allowedHosts.stream().anyMatch(i -> i.getHostName().equals(((Request)r).getReqHeader().get("@Client")));
        } else {
            return false;
        }
    }

    @Override
    @PreFilter
    public boolean forbiddenHost(R r) {
        if(this.forbiddenHosts != null && r.getClass().isAssignableFrom(Request.class)) {
            return !this.forbiddenHosts.stream().anyMatch(i -> i.getHostName().equals(((Request)r).getReqHeader().get("@Client")));
        } else {
            return false;
        }        
    }

    @Override
    public void setUserManager(UserManager securityManager) {
        this.securityManager = securityManager;
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
