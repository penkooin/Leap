package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.ServiceInvoker;
import org.chaostocosmos.leap.http.ServiceManager;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.AnnotationHelper;
import org.chaostocosmos.leap.http.annotation.AnnotationOpr;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.Resources;
import org.chaostocosmos.leap.http.resources.SpringJPAManager;
import org.chaostocosmos.leap.http.services.filters.IFilter;
import org.chaostocosmos.leap.http.services.model.DeleteServiceModel;
import org.chaostocosmos.leap.http.services.model.GetServiceModel;
import org.chaostocosmos.leap.http.services.model.PostServiceModel;
import org.chaostocosmos.leap.http.services.model.PutServiceModel;
import org.chaostocosmos.leap.http.services.model.ServiceModel;

import ch.qos.logback.classic.Logger;

/**
 * Abstraction of SimpleServlet object
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public abstract class AbstractService implements GetServiceModel, PostServiceModel, PutServiceModel, DeleteServiceModel {
    /**
     * Logger
     */
    protected Logger logger;
    /**
     * Method to be called for request
     */
    protected Method invokingMethod;
    /**
     * Filters for previous filtering process of service method
     */
    protected List<IFilter> preFilters;
    /**
     * Filter for after filtering process of service method
     */
    protected List<IFilter> postFilters;
    /**
     * Leap service manager object
     */
    protected ServiceManager serviceManager;
    /**
     * Resource object
     */
    protected Resources resource;
    /**
     * HttpTransfer object
     */
    protected HttpTransfer httpTransfer;

    @Override
    public Response serve(final HttpTransfer httpTransfer, final Method invokingMethod) throws Exception {        
        this.logger = LoggerFactory.getLogger(httpTransfer.getRequest().getRequestedHost());
        this.httpTransfer = httpTransfer;
        this.invokingMethod = invokingMethod;
        this.resource = this.httpTransfer.getHost().getResource();
        Request request = httpTransfer.getRequest();
        Response response = httpTransfer.getResponse();

        if(this.preFilters != null) {
            for(IFilter filter : this.preFilters) {
                List<Method> methods = AnnotationHelper.getFilterMethods(filter, PreFilter.class); 
                for(Method method : methods) {
                    ServiceInvoker.invokeMethod(filter, method, request);
                }
            }
        }        

        Class<?>[] paramTypes = this.invokingMethod.getParameterTypes();
        if(paramTypes.length != 2 || paramTypes[0] != request.getClass() || paramTypes[1] != response.getClass()) {
            //org.chaostocosmos.leap.http.WASException: Not Implemented.
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES501.code(), Context.getMessages().<String> getErrorMsg(201, this.invokingMethod.getName()));
        }

        //setting JPA link
        new AnnotationOpr<ServiceModel>(httpTransfer.getHost().getHost(), this).injectToAutowired();
        //aOpr.injectToAutowired();
        switch(httpTransfer.getRequest().getRequestType()) {
            case GET: 
            serveGet(request, response);
            break;
            case POST: 
            servePost(request, response);
            break;
            case PUT:
            servePost(request, response);
            break;
            case DELETE:
            serveDelete(request, response);
            break;
            default:
                throw new WASException(MSG_TYPE.ERROR, 16, request.getRequestType().name());
        }
        if(this.postFilters != null) {
            for(IFilter filter : this.postFilters) {
                List<Method> methods = AnnotationHelper.getFilterMethods(filter, PostFilter.class);
                for(Method method : methods) {
                    ServiceInvoker.invokeMethod(filter, method, response);
                }
            }
        }
        return response;
    }

    @Override
    public void serveGet(final Request request, final Response response) throws Exception {        
        this.invokingMethod.invoke(this, request, response);
    }

    @Override
    public void servePost(final Request request, final Response response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }

    @Override
    public void servePut(final Request request, final Response response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }

    @Override
    public void serveDelete(final Request request, final Response response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }    

    @Override
    public void setFilters(final List<IFilter> preFilters, final List<IFilter> postFilters) {
        this.preFilters = preFilters;
        this.postFilters = postFilters;
    } 

    @Override
    public void sendResponse(final Response response) throws Throwable {
        this.httpTransfer.sendResponse(response);
        this.httpTransfer.close();     
    }

    @Override
    public Resources getResource() {
        return this.resource;
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
}
