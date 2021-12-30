package org.chaostocosmos.leap.http.filter;

import java.net.InetAddress;
import java.util.List;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.annotation.PreFilter;

/**
 * Abstract ip filtering object
 * @author 9ins
 */
abstract class AbstractIpPreFilter<R> implements IIpPreFilter<R> {
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
}
