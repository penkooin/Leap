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
     * Request type
     */
    REQUEST requestType;

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
     * @param requestType
     */
    public ServiceHolder(String contextPath, ServiceModel serviceModel, REQUEST requestType) {
        this.contextPath = contextPath;
        this.requestType = requestType;
        this.serviceModel = serviceModel;
    }

    public String getServicePath() {
        return this.contextPath;
    }

    public void setServicePath(String servicePath) {
        this.contextPath = servicePath;
    }

    public REQUEST getRequestType() {
        return this.requestType;
    }

    public void setRequestType(REQUEST requestType) {
        this.requestType = requestType;
    }

    public ServiceModel getServiceModel() {
        return this.serviceModel;
    }

    public void setServiceModel(ServiceModel service) {
        this.serviceModel = service;
    }

    public String getServiceClassName() {
        return this.serviceModel.getClass().getCanonicalName();
    }

    @Override
    public String toString() {
        return "{" +
            " contextPath='" + contextPath + "'" +
            ", requestType='" + requestType + "'" +
            ", service='" + serviceModel + "'" +
            ", preFilters='" + preFilters + "'" +
            ", postFilters='" + postFilters + "'" +
            "}";
    }
}

