package org.chaostocosmos.leap.http.services.model;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Delete Servlet
 */
public interface DeleteServiceModel extends ServiceModel {
    /**
     * Delete process
     * @param request
     * @param response
     * @throws Exception
     */
    public void serveDelete(final Request request, final Response response) throws Exception;
}
