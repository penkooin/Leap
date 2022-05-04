package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Delete Servlet
 */
public interface IDeleteService extends ILeapService {
    /**
     * Delete process
     * @param request
     * @param response
     * @throws Exception
     */
    public void serveDelete(final Request request, final Response response) throws Exception;
}
