package org.chaostocosmos.leap.service.filter;

import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.service.model.SpringJPAModel;

/**
 * Ip filter(allowed/forbidden)
 * 
 * Implement using request header's @Client property 
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public interface IIpFilter<R> extends IPreFilter, SpringJPAModel {    
    /**
     * Check allowed hosts when request be happening.
     * @param request
     * @return
     */
    public boolean allowedHost(R request) throws LeapException;

    /**
     * Check forbidden hots when request be happening.
     * @param request
     * @return
     */
    public boolean forbiddenHost(R request) throws LeapException;
}
