package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Get Servlet interface
 */
public interface IGetService extends ILeapService { 
    /**
     * Get process
     * @param request
     * @param response
     * @throws Exception
     */
    public void serveGet(final Request request, final Response response) throws Exception;    
}
