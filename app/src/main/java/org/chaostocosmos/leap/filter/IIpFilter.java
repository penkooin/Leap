package org.chaostocosmos.leap.filter;

import org.chaostocosmos.leap.service.model.SpringJPAModel;

/**
 * Ip filter(allowed/forbidden)
 * 
 * Implement using request header's @Client property 
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public interface IIpFilter<F> extends IPreFilter<F>, SpringJPAModel {    

    /**
     * Check allowed IP to request or response
     * @param request
     * @return
     */
    public boolean allowedHost(F request) throws Exception;

    /**
     * Check forbidden IP to request or response
     * @param request
     * @return
     */
    public boolean forbiddenHost(F request) throws Exception;
}
