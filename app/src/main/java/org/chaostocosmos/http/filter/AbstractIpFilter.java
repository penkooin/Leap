package org.chaostocosmos.http.filter;

import java.net.InetAddress;
import java.util.List;

import org.chaostocosmos.http.HttpRequestDescriptor;

/**
 * Abstract ip filtering object
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public abstract class AbstractIpFilter implements IIpFilter {
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
    public boolean allowedHost(HttpRequestDescriptor request) {
        if(this.allowedHosts == null) {
            return true;
        }
        return this.allowedHosts.stream().anyMatch(i -> i.getHostName().equals(request.getReqHeader().get("@Client")));
    }
    @Override
    public boolean forbiddenHost(HttpRequestDescriptor request) {
        if(this.forbiddenHosts == null) {
            return false;
        }
        return !this.forbiddenHosts.stream().anyMatch(i -> i.getHostName().equals(request.getReqHeader().get("@Client")));
    }
}
