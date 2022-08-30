package org.chaostocosmos.leap.http;

import java.util.List;

import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.service.filter.IFilter;
import org.chaostocosmos.leap.http.service.model.ServiceModel;

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
    REQUEST_TYPE requestType;

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
    public ServiceHolder(String contextPath, ServiceModel serviceModel, REQUEST_TYPE requestType) {
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

    public REQUEST_TYPE getRequestType() {
        return this.requestType;
    }

    public void setRequestType(REQUEST_TYPE requestType) {
        this.requestType = requestType;
    }

    public ServiceModel getServiceModel() {
        return this.serviceModel;
    }

    public void setServiceModel(ServiceModel service) {
        this.serviceModel = service;
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

