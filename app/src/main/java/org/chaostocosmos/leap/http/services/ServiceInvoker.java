package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
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
     * @param httpTransfer
     * @param doClone
     * @throws Exception
     * @throws CloneNotSupportedException
     */
    public static HttpResponseDescriptor invokeService(ServiceHolder serviceHolder, HttpTransfer httpTransfer, boolean doClone) throws Throwable {
        HttpResponseDescriptor response = httpTransfer.getResponse();
        AbstractLeapService service = (AbstractLeapService)serviceHolder.getService();
        if(doClone) {
            service = (AbstractLeapService) service.clone();
        }
        try {
            response = service.serve(httpTransfer, serviceHolder.getServiceMethod());
        } catch(Throwable e) {
            if(service.errorHandling(httpTransfer.getResponse(), e) != null) {
                throw e;
            }
        }
        return response;
    }

    /**
     * Invoke method
     * @param object
     * @param method
     * @param params
     * @throws WASException
     */
    public static void invokeMethod(Object object, String methodName, Object... params) throws Exception {
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
    public static void invokeMethod(Object object, Method method, Object... params) throws Exception {
        method.invoke(object, params);
    }
}
