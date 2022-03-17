package org.chaostocosmos.leap.http.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.AnnotationHelper;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.filters.ILeapFilter;
import org.chaostocosmos.leap.http.resources.Context;

import ch.qos.logback.classic.Logger;

/**
 * Abstraction of SimpleServlet object
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public abstract class AbstractLeapService implements IGetService, IPostService, IPutService, IDeleteService {
    /**
     * Logger
     */
    protected Logger logger;
    /**
     * Context
     */
    protected static final Context context = Context.get();
    /**
     * Method to be called for request
     */
    protected Method invokingMethod;
    /**
     * Filters for previous filtering process of service method
     */
    protected List<ILeapFilter> preFilters;
    /**
     * Filter for after filtering process of service method
     */
    protected List<ILeapFilter> postFilters;
    /**
     * Leap service manager object
     */
    protected ServiceManager serviceManager;
    /**
     * HttpTransfer object
     */
    protected HttpTransfer httpTransfer;

    @Override
    public HttpResponseDescriptor serve(HttpTransfer httpTransfer, Method invokingMethod) throws Exception {
        this.logger = LoggerFactory.getLogger(httpTransfer.getRequest().getRequestedHost());
        this.httpTransfer = httpTransfer;
        this.invokingMethod = invokingMethod;
        if(this.preFilters != null) {
            for(ILeapFilter filter : this.preFilters) {
                List<Method> methods = AnnotationHelper.getFilterMethods(filter, PreFilter.class); 
                for(Method method : methods) {
                    ServiceInvoker.invokeMethod(filter, method, this.httpTransfer.getRequest());
                }
            }
        }                
        HttpRequestDescriptor request = httpTransfer.getRequest();
        HttpResponseDescriptor response = httpTransfer.getResponse();
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
            for(ILeapFilter filter : this.postFilters) {
                List<Method> methods = AnnotationHelper.getFilterMethods(filter, PostFilter.class);
                for(Method method : methods) {
                    ServiceInvoker.invokeMethod(filter, method, response);
                }
            }
        }
        return response;
    }

    @Override
    public void serveGet(HttpRequestDescriptor request, HttpResponseDescriptor response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }

    @Override
    public void servePost(HttpRequestDescriptor request, HttpResponseDescriptor response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }

    @Override
    public void servePut(HttpRequestDescriptor request, HttpResponseDescriptor response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }

    @Override
    public void serveDelete(HttpRequestDescriptor request, HttpResponseDescriptor response) throws Exception {
        this.invokingMethod.invoke(this, request, response);
    }    

    @Override
    public void setFilters(List<ILeapFilter> preFilters, List<ILeapFilter> postFilters) {
        this.preFilters = preFilters;
        this.postFilters = postFilters;
    } 

    @Override
    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }    
}
