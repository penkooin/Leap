package org.chaostocosmos.leap.http.services.model;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Get Servlet interface
 */
public interface GetServiceModel extends ServiceModel { 
    /**
     * Get process
     * @param request
     * @param response
     * @throws Exception
     */
    public void serveGet(final Request request, final Response response) throws Exception;    
}
