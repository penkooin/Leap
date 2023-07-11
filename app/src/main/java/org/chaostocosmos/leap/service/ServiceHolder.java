package org.chaostocosmos.leap.service;

import java.util.List;

import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.service.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;

/**
 * ServiceHolder
 * @author Kooin Shin
 */
public class ServiceHolder {
    /**
     * Context path
     */
    String contextPath;
    /**
     * Leap service
     */
    ServiceModel serviceModel;    
    /**
     * Service filter for pre process
     */
    protected List<IFilter> preFilters;
    /**
     * Service filter for post process
     */
    protected List<IFilter> postFilters;
    /**
     * Constructor with parameters
     * @param contextPath
     * @param serviceModel
     */
    public ServiceHolder(String contextPath, ServiceModel serviceModel) {
        this.contextPath = contextPath;
        this.serviceModel = serviceModel;
    }
    /**
     * Get service path
     * @return
     */    
    public String getServicePath() {
        return this.contextPath;
    }
    /**
     * Set service path
     * @param servicePath
     */
    public void setServicePath(String servicePath) {
        this.contextPath = servicePath;
    }
    /**
     * Get service model
     * @return
     */
    public ServiceModel getServiceModel() {
        return this.serviceModel;
    }
    /**
     * Set service model
     * @param service
     */
    public void setServiceModel(ServiceModel service) {
        this.serviceModel = service;
    }
    /**
     * Get service class cannonical name
     * @return
     */
    public String getServiceClassName() {
        return this.serviceModel.getClass().getCanonicalName();
    }

    @Override
    public String toString() {
        return "{" +
            " contextPath='" + contextPath + "'" +
            ", service='" + serviceModel + "'" +
            ", preFilters='" + preFilters + "'" +
            ", postFilters='" + postFilters + "'" +
            "}";
    }
}

