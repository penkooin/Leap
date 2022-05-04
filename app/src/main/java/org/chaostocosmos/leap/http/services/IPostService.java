package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Put Servlet interface
 */
public interface IPostService extends ILeapService {
    /**
     * Post process
     * @param request
     * @param response
     * @throws Exception
     */
    public void servePost(final Request request, final Response response) throws Exception;    
}
