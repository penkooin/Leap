package org.chaostocosmos.leap.http.service.model;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Put Servlet interface
 */
public interface PutServiceModel extends ServiceModel {
    /**
     * Put process
     * @param params
     * @throws Exception
     */
    public void PUT(final Request request, final Response response) throws Exception;    
}
