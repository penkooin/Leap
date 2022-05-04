package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Put Servlet interface
 */
public interface IPutService extends ILeapService {
    /**
     * Put process
     * @param request
     * @param response
     * @throws Exception
     */
    public void servePut(final Request request, final Response response) throws Exception;    
}
