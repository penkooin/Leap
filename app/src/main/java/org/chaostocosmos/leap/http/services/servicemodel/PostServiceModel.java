package org.chaostocosmos.leap.http.services.servicemodel;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Put Servlet interface
 */
public interface PostServiceModel extends ServiceModel {
    /**
     * Post process
     * @param request
     * @param response
     * @throws Exception
     */
    public void POST(final Request request, final Response response) throws Exception;    
}
