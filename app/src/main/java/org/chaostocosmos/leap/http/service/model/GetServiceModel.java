package org.chaostocosmos.leap.http.service.model;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Get Servlet interface
 */
public interface GetServiceModel extends ServiceModel { 
    /**
     * Get process
     * @param params
     * @throws Exception
     */
    public void GET(final Request request, final Response response) throws Exception;    
}
