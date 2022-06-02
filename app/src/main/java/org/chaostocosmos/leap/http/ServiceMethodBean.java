package org.chaostocosmos.leap.http;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.services.filters.IFilter;
import org.chaostocosmos.leap.http.services.model.ServiceModel;

/**
 * Service info bean object
 */
public class ServiceMethodBean {
    /**
     * Service object
     */
    protected ServiceModel service;
    /**
     * Service method
     */
    protected Method serviceMethod;
    /**
     * Service filter for pre process
     */
    protected List<IFilter> preFilters;
    /**
     * Service filter for post process
     */
    protected List<IFilter> postFilters;
    /**
     * Constructor with attributes
     * @param service
     * @param serviceMethod
     * @param preFilters
     * @param postFilters
     */
    public ServiceMethodBean(ServiceModel service, Method serviceMethod, List<IFilter> preFilters, List<IFilter> postFilters) {
        this.service = service;
        this.serviceMethod = serviceMethod;
        this.preFilters = preFilters;
        this.postFilters = postFilters;
    }

    public ServiceModel getService() {
        return this.service;
    }

    public void setService(ServiceModel service) {
        this.service = service;
    }

    public Method getServiceMethod() {
        return this.serviceMethod;
    }

    public void setServiceMethod(Method serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    public List<IFilter> getPreFilters() {
        return this.preFilters;
    }

    public void setPreFilters(List<IFilter> preFilters) {
        this.preFilters = preFilters;
    }

    public List<IFilter> getPostFilters() {
        return this.postFilters;
    }

    public void setPostFilters(List<IFilter> postFilters) {
        this.postFilters = postFilters;
    }

    @Override
    public String toString() {
        return "{" +
            " service='" + getService() + "'" +
            ", serviceMethod='" + getServiceMethod() + "'" +
            ", preFilters='" + getPreFilters() + "'" +
            ", postFilters='" + getPostFilters() + "'" +
            "}";
    }
}

