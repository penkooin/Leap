package org.chaostocosmos.leap.http.services.servicemodel;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Put Servlet interface
 */
public interface PutServiceModel extends ServiceModel {
    /**
     * Put process
     * @param request
     * @param response
     * @throws Exception
     */
    public void PUT(final Request request, final Response response) throws Exception;    
}
