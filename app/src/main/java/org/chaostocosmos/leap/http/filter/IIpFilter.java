package org.chaostocosmos.leap.http.filter;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;

/**
 * Ip filter(allowed/forbidden)
 * 
 * Implement using request header's @Client property 
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public interface IIpFilter extends IFilter {    
    /**
     * Whether allowed host
     * @param request
     * @return
     */
    public abstract boolean allowedHost(HttpRequestDescriptor request);
    /**
     * Whether forbidden host
     * @param request
     * @return
     */
    public abstract boolean forbiddenHost(HttpRequestDescriptor request);
}
