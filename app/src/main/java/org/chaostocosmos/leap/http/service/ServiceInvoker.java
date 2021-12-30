package org.chaostocosmos.leap.http.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.slf4j.Logger;

/**
 * Servlet management object
 * @author 9ins
 */
public class ServiceInvoker {
    /**
     * logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Context.getDefaultHost()); 
    
    /**
     * Call service method on servlet
     * @param serviceHolder
     * @param request
     * @param response
     * @throws WASException
     */
    public static void invokeService(ServiceHolder serviceHolder, HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException {
        ILeapService service = serviceHolder.getService();
        service.setFilters(serviceHolder.getPreFilters(), serviceHolder.getPostFilters());
        service.serve(request, response, serviceHolder.getServiceMethod());
    }

    /**
     * Invoke method
     * @param object
     * @param method
     * @param params
     * @throws WASException
     */
    public static void invokeMethod(Object object, String methodName, Object... params) throws WASException {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, Arrays.asList(params).stream().map(o -> o.getClass()).toArray(Class[]::new));
            invokeMethod(object, method, params);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new WASException(e);
        }
    }

    /**
     * Invoke method
     * @param object
     * @param method
     * @param params
     * @throws WASException
     */
    public static void invokeMethod(Object object, Method method, Object... params) throws WASException {
        try {
            method.invoke(object, params);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            throw new WASException(e);
        }
    }
}
