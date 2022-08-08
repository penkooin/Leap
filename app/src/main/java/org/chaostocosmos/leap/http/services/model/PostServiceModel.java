package org.chaostocosmos.leap.http.services.model;

/**
 * Put Servlet interface
 */
public interface PostServiceModel extends ServiceModel {
    /**
     * Post process
     * @param params
     * @throws Exception
     */
    public void POST(final Object[] params) throws Exception;    
}
