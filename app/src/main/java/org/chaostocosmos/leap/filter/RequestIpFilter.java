package org.chaostocosmos.leap.filter;

import java.net.InetAddress;
import java.util.List;

import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.spring.SpringJPAManager;

/**
 * Abstract ip filtering object
 * 
 * @author 9ins
 */
public class RequestIpFilter<T> extends AbstractRequestFilter<HttpRequest<T>> implements IIpFilter<HttpRequest<T>> {

    /**
     * Allowed / forbidden hosts
     */
    List<InetAddress> allowedHosts, forbiddenHosts;

    /**
     * Constructor with allowed / forbidden hosts
     * @param allowedHosts
     * @param forbiddenHosts
     */
    public RequestIpFilter(List<InetAddress> allowedHosts, List<InetAddress> forbiddenHosts) {
        this.allowedHosts = allowedHosts;
        this.forbiddenHosts = forbiddenHosts;
    }
    
    @Override
    public boolean allowedHost(HttpRequest<T> request) {
        if(this.allowedHosts != null && request.getClass().isAssignableFrom(HttpRequest.class)) {
            return this.allowedHosts.stream().anyMatch(i -> i.getHostName().equals(((HttpRequest<?>)request).getHeaders().get("@Client")));
        } else {
            return false;
        }
    }

    @Override
    public boolean forbiddenHost(HttpRequest<T> request) {
        if(this.forbiddenHosts != null && request.getClass().isAssignableFrom(HttpRequest.class)) {
            return !this.forbiddenHosts.stream().anyMatch(i -> i.getHostName().equals(((HttpRequest<?>)request).getHeaders().get("@Client")));
        } else {
            return false;
        }
    }

    @Override
    public <B> B getBean(String beanName, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanName, args);
    }

    @Override
    public <B> B getBean(Class<?> beanClass, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanClass, args);
    }
}
