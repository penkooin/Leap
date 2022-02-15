package org.chaostocosmos.leap.http.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.AnnotationHelper;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.filters.ILeapFilter;
import org.chaostocosmos.leap.http.security.UserManager;

/**
 * Abstraction of SimpleServlet object
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public abstract class AbstractLeapService implements IGetService, 
                                              IPostService, 
                                              IPutService, 
                                              IDeleteService {
    /**
     * Context
     */
    private static final Context context = Context.getInstance();
    /**
     * Method to be called for request
     */
    private Method invokingMethod;
    /**
     * Filters for previous filtering process of service method
     */
    private List<ILeapFilter> preFilters;
    /**
     * Filter for after filtering process of service method
     */
    private List<ILeapFilter> postFilters;
    /**
     * Leap security manager object
     */
    private UserManager securityManager;

    @Override
    public void serve(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws WASException {
        this.invokingMethod = invokingMethod;
        if(this.preFilters != null) {
            for(ILeapFilter filter : this.preFilters) {
                List<Method> methods = AnnotationHelper.getFilterMethods(filter, PreFilter.class); 
                for(Method method : methods) {
                    ServiceInvoker.invokeMethod(filter, method, request);
                }
            }
        }
        switch(request.getRequestType()) {
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
    }

    @Override
    public void serveGet(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException {
        try {
            this.invokingMethod.invoke(this, request, response);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new WASException(e);
        }
    }

    @Override
    public void servePost(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException {
        try {
            this.invokingMethod.invoke(this, request, response);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new WASException(e);
        }
    }

    @Override
    public void servePut(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException {
        try {
            this.invokingMethod.invoke(this, request, response);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new WASException(e);
        }
    }

    @Override
    public void serveDelete(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException {
        try {
            this.invokingMethod.invoke(this, request, response);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new WASException(e);
        }
    }    

    @Override
    public void setFilters(List<ILeapFilter> preFilters, List<ILeapFilter> postFilters) throws WASException {
        this.preFilters = preFilters;
        this.postFilters = postFilters;
    } 

    @Override
    public void setSecurityManager(UserManager securityManager) {
        this.securityManager = securityManager;
    }
}
