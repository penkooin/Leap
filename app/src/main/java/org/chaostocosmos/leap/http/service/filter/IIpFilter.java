package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.inject.PreFilterIndicates;
import org.chaostocosmos.leap.http.service.model.SpringJPAModel;

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
    @PreFilterIndicates
    public boolean allowedHost(R request) throws HTTPException;

    /**
     * Check forbidden hots when request be happening.
     * @param request
     * @return
     */
    @PreFilterIndicates
    public boolean forbiddenHost(R request) throws HTTPException;
}
