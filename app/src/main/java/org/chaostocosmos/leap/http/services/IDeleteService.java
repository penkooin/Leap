package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;

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
    public void serveDelete(HttpRequestDescriptor request, HttpResponseDescriptor response) throws Exception;
}
