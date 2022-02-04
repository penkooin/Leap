package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.IFilter;

/**
 * ServiceHolder
 * @author 9ins
 */
public class ServiceHolder {
    /**
     * Service path 
     */
    String servicePath;

    /**
     * Request type
     */
    REQUEST_TYPE requestType;

    /**
     * Leap service
     */
    ILeapService service;

    /**
     * Service method
     */
    Method serviceMethod;

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
     * @param servicePath
     * @param service
     * @param requestType
     * @param serviceMethod
     */
    public ServiceHolder(String servicePath, ILeapService service, REQUEST_TYPE requestType, Method serviceMethod) {
        this(servicePath, service, requestType, serviceMethod, null, null);
    }

    /**
     * Construct with parameters
     * @param servicePath
     * @param service
     * @param requestType
     * @param serviceMethod
     * @param preFilters
     * @param postFilters
     */
    public ServiceHolder(String servicePath, ILeapService service, REQUEST_TYPE requestType, Method serviceMethod, List<IFilter> preFilters, List<IFilter> postFilters) {
        this.servicePath = servicePath;
        this.requestType = requestType;
        this.service = service;
        this.serviceMethod = serviceMethod;
        this.preFilters = preFilters;
        this.postFilters = postFilters;
    }    

    public String getServicePath() {
        return this.servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public REQUEST_TYPE getRequestType() {
        return this.requestType;
    }

    public void setRequestType(REQUEST_TYPE requestType) {
        this.requestType = requestType;
    }

    public ILeapService getService() {
        return this.service;
    }

    public void setService(ILeapService service) {
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
            " servicePath='" + getServicePath() + "'" +
            " requestType='" + getRequestType()+ "'" +
            ", serviceMethod='" + getServiceMethod() + "'" +
            ", preFilters='" + getPreFilters() + "'" +
            ", postFilters='" + getPostFilters() + "'" +
            "}";
    }
}

