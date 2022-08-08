package org.chaostocosmos.leap.http.services.model;

/**
 * Get Servlet interface
 */
public interface GetServiceModel extends ServiceModel { 
    /**
     * Get process
     * @param params
     * @throws Exception
     */
    public void GET(final Object[] params) throws Exception;    
}
