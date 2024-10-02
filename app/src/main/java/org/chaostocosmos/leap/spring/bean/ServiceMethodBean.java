package org.chaostocosmos.leap.spring.bean;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;

/**
 * Service info bean object
 * 
 * @author 9ins
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

    /**
     * Get ServiceModel
     * @return
     */
    public ServiceModel getService() {
        return this.service;
    }

    /**
     * Set ServiceModel
     * @param service
     */
    public void setService(ServiceModel service) {
        this.service = service;
    }

    /**
     * Get ServiceMethod
     * @return
     */
    public Method getServiceMethod() {
        return this.serviceMethod;
    }

    /**
     * Set ServiceMethod
     * @param serviceMethod
     */
    public void setServiceMethod(Method serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    /**
     * Get PreFilters
     * @return
     */
    public List<IFilter> getPreFilters() {
        return this.preFilters;
    }

    /**
     * Set PreFilters
     * @param preFilters
     */
    public void setPreFilters(List<IFilter> preFilters) {
        this.preFilters = preFilters;
    }

    /**
     * Get PostFilters
     * @return
     */
    public List<IFilter> getPostFilters() {
        return this.postFilters;
    }

    /**
     * Set PostFilters
     * @param postFilters
     */
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

