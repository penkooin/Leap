package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.resources.ResourcesModel;
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
 * 
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
    protected ResourcesModel resource;
    
    /**
     * HttpTransfer object
     */
    protected HttpTransfer httpTransfer;

    /**
     * Request
     */
    protected Request request;

    /**
     * Response
     */
    protected Response response;

    @Override
    public Response serve(final HttpTransfer httpTransfer, final Method invokingMethod) throws Exception {        
        this.logger = LoggerFactory.getLogger(httpTransfer.getRequest().getRequestedHost());
        this.invokingMethod = invokingMethod;
        this.httpTransfer = httpTransfer;
        this.resource = this.httpTransfer.getHost().getResource();
        this.request = this.httpTransfer.getRequest();
        this.response = this.httpTransfer.getResponse();

        if(this.preFilters != null) {
            for(IFilter filter : this.preFilters) {
                List<Method> methods = AnnotationHelper.getFilterMethods(filter, PreFilter.class);
                for(Method method : methods) {
                    ServiceInvoker.invokeMethod(filter, method, request);
                }
            }
        }
        Map<String, Object> paramMap = Arrays.asList(this.getClass().getDeclaredFields()).stream().map(f -> {
            try {
                f.setAccessible(true);
                return new Object[]{ f.getClass().getName(), f.get(this) };
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }).filter(f -> f != null).collect(Collectors.toMap( k -> (String)k[0],  v -> v));
        System.out.println(paramMap.toString());

        //Class<?>[] paramTypes = this.invokingMethod.getParameterTypes();
        //if(paramTypes.length != 2 || paramTypes[0] != request.getClass() || paramTypes[1] != response.getClass()) {
            //org.chaostocosmos.leap.http.WASException: Not Implemented.
        //    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES501.code(), Context.getMessages().<String> getErrorMsg(201, this.invokingMethod.getName()));
        //}

        //setting JPA link
        AnnotationOpr<ServiceModel> annotaionOpr = new AnnotationOpr<>(httpTransfer.getHost().getHost(), this);
        annotaionOpr.injectToAutowired();

        Method serviceMethod = this.getClass().getMethod(request.getRequestType().name(), paramMap.values().stream().map(v -> v.getClass()).toArray(Class[]::new));
        if(serviceMethod == null) {
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
    public void GET(final Object[] params) throws Exception {        
        this.invokingMethod.invoke(this, request, params);
    }

    @Override
    public void POST(final Object[] params) throws Exception {
        this.invokingMethod.invoke(this, request, params);
    }

    @Override
    public void PUT(final Object[] params) throws Exception {
        this.invokingMethod.invoke(this, request, params);
    }

    @Override
    public void DELETE(final Object[] params) throws Exception {
        this.invokingMethod.invoke(this, request, params);
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
    public ResourcesModel getResource() {
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
