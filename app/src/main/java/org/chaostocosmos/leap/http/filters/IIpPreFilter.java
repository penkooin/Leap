package org.chaostocosmos.leap.http.filters;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.PreFilter;

/**
 * Ip filter(allowed/forbidden)
 * 
 * Implement using request header's @Client property 
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public interface IIpPreFilter<R> extends ILeapFilter {    
    /**
     * Check allowed hosts when request be happening.
     * @param request
     * @return
     */
    @PreFilter
    public boolean allowedHost(R request) throws WASException;

    /**
     * Check forbidden hots when request be happening.
     * @param request
     * @return
     */
    @PreFilter
    public boolean forbiddenHost(R request) throws WASException;
}
