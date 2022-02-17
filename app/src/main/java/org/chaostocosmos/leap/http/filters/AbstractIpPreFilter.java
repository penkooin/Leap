package org.chaostocosmos.leap.http.filters;

import java.net.InetAddress;
import java.util.List;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.user.UserManager;

/**
 * Abstract ip filtering object
 * @author 9ins
 */
public abstract class AbstractIpPreFilter<R> implements IIpPreFilter<R> {

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
    public AbstractIpPreFilter(List<InetAddress> allowedHosts, List<InetAddress> forbiddenHosts) {
        this.allowedHosts = allowedHosts;
        this.forbiddenHosts = forbiddenHosts;
    }
    
    @Override
    @PreFilter
    public boolean allowedHost(R r) {
        if(this.allowedHosts != null && r.getClass().isAssignableFrom(HttpRequestDescriptor.class)) {
            return this.allowedHosts.stream().anyMatch(i -> i.getHostName().equals(((HttpRequestDescriptor)r).getReqHeader().get("@Client")));
        } else {
            return false;
        }
    }

    @Override
    @PreFilter
    public boolean forbiddenHost(R r) {
        if(this.forbiddenHosts != null && r.getClass().isAssignableFrom(HttpRequestDescriptor.class)) {
            return !this.forbiddenHosts.stream().anyMatch(i -> i.getHostName().equals(((HttpRequestDescriptor)r).getReqHeader().get("@Client")));
        } else {
            return false;
        }        
    }

    @Override
    public void setSecurityManager(UserManager securityManager) {
        this.securityManager = securityManager;
    }    
}
