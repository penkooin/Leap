package org.chaostocosmos.leap.http.service.model;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;

/**
 * Delete Servlet
 */
public interface DeleteServiceModel extends ServiceModel {
    /**
     * Delete process
     * @param params
     * @throws Exception
     */
    public void DELETE(final Request request, final Response response) throws Exception;
}
