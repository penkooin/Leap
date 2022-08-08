package org.chaostocosmos.leap.http.services.model;

/**
 * Delete Servlet
 */
public interface DeleteServiceModel extends ServiceModel {
    /**
     * Delete process
     * @param params
     * @throws Exception
     */
    public void DELETE(final Object[] params) throws Exception;
}
