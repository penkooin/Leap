package org.chaostocosmos.leap.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.ServiceManager;
import org.chaostocosmos.leap.SpringJPAManager;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.http.Http;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.service.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;
import org.chaostocosmos.leap.session.SessionManager;

import ch.qos.logback.classic.Logger;

/**
 * Abstraction of SimpleServlet object
 * 
 * @author Kooin Shin
 * @since 2021.09.15
 */
public abstract class AbstractService implements ServiceModel {
    /**
     * Logger
     */
    protected Logger logger;
    /**
     * Host object
     */
    Host<?> host;
    /**
     * SessionManager object
     */
    protected SessionManager sessionManager;

    /**
     * Leap service manager object
     */
    protected ServiceManager serviceManager;

    /**
     * ResourcesModel object
     */
    protected ResourcesModel resourcesModel;

    /**
     * HttpTransfer object
     */
    protected HttpTransfer httpTransfer;

    @Override
    public Host<?> getHost() {
        return this.host;
    }

    @Override
    public void setHost(Host<?> host) {
        this.host = host;
    }

    @Override
    public HttpResponse handle(final HttpTransfer httpTransfer) { 
        this.logger = httpTransfer.getLogger();
        this.httpTransfer = httpTransfer;
        this.resourcesModel = httpTransfer.getHost().getResource();
        try {
            HttpRequest request = httpTransfer.getRequest();
            HttpResponse response = httpTransfer.getResponse();

            Map<Class<? extends Http>, Object> paramMap = Map.of(HttpTransfer.class, httpTransfer, HttpRequest.class, request, HttpResponse.class, response);

            //Set service method
            Method targetMethod = this.serviceManager.getServiceMethod(request.getRequestType(), request.getContextPath(), this);
            Class<?>[] paramTypes = targetMethod.getParameterTypes();
            if(paramTypes.length != 2 || paramTypes[0] != request.getClass() || paramTypes[1] != response.getClass()) {
                throw new LeapException(HTTP.RES501, "There isn't exist target method: "+targetMethod.getName());
            }
            Object[] params = Arrays.asList(paramTypes).stream().map(c -> paramMap.get(c)).toArray();
            targetMethod.invoke(this, params);
            
            //setting JPA link
            InjectionMapper<ServiceModel> annotaionOpr = new InjectionMapper<>(httpTransfer.getHost().getHost(), this);
            annotaionOpr.injectToAutowired();
            return response;            
        } catch(Exception e) {
            throw new LeapException(HTTP.RES503, e);
        }
    }

    @Override
    public void sendResponse(final HttpResponse response) throws Exception {
        this.httpTransfer.sendResponse(response);
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public ResourcesModel getResourcesModel() {
        return this.resourcesModel;
    }

    @Override
    public void setResourcesModel(ResourcesModel resourcesModel) {
        this.resourcesModel = resourcesModel;
    }

    @Override
    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    @Override
    public void setServiceManager(final ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }    

    @Override
    public <T> T getBean(String beanName, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanName, args);
    }

    @Override
    public <T> T getBean(Class<?> beanClass, Object... args) throws Exception {
        return SpringJPAManager.get().getBean(beanClass, args);
    }

    @Override
    public void setPreFilters(final List<IFilter> preFilters) {
    }

    @Override
    public void setPostFilters(final List<IFilter> postFilters) {
    }

}

