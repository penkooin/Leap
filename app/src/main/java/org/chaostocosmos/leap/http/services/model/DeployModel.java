package org.chaostocosmos.leap.http.services.model;

/**
 * IDeploy
 * 
 * @author 9ins
 */
public interface DeployModel {
    /**
     * Deploy service
     * @param service
     * @throws Exception
     */
    public void deployService(Class<ServiceModel> service) throws Exception;

    /**
     * Remove service
     * @param services
     * @throws Exception
     */
    public void removeService(String serviceName) throws Exception;
    
}
