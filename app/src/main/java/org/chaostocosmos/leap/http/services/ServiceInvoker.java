package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.resources.Context;
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
        try {
            if(doClone) {
                service = (AbstractLeapService) service.clone();
                response = service.serve(httpTransfer, serviceHolder.getServiceMethod());
            } else {
                synchronized(service) {
                    response = service.serve(httpTransfer, serviceHolder.getServiceMethod());
                }                
            }
            
        } catch(Throwable e) {
            if(service.errorHandling(httpTransfer.getResponse(), e) != null) {
                throw e;
            }
            System.out.println(Context.getWarnMsg(2, serviceHolder.getService().getClass().getName(), e.getMessage()));
            logger.warn(Context.getWarnMsg(2, serviceHolder.getService().getClass().getName(), e.getMessage()));
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
