package org.chaostocosmos.leap.http.services;

/**
 * IDeploy
 * 
 * @author 9ins
 */
public interface IDeploy {
    /**
     * Deploy service
     * @param service
     * @throws Exception
     */
    public void deployService(Class<ILeapService> service) throws Exception;

    /**
     * Remove service
     * @param services
     * @throws Exception
     */
    public void removeService(String serviceName) throws Exception;
    
}
