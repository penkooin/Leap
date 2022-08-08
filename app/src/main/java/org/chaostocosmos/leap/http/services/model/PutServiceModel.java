package org.chaostocosmos.leap.http.services.model;

/**
 * Put Servlet interface
 */
public interface PutServiceModel extends ServiceModel {
    /**
     * Put process
     * @param params
     * @throws Exception
     */
    public void PUT(final Object[] params) throws Exception;    
}
