package org.chaostocosmos.leap.http;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.service.AbstractService;
import org.slf4j.Logger;

/**
 * Servlet management object
 * @author 9ins 
 */
public class ServiceInvoker {
    /**
     * logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Context.hosts().getDefaultHost().getHostId()); 
    
    /**
     * Call service method on servlet
     * @param serviceHolder
     * @param httpTransfer
     * @param doClone
     * @throws Throwable
     * @throws CloneNotSupportedException
     */
    public static Response invokeServiceMethod(ServiceHolder serviceHolder, HttpTransfer httpTransfer) throws Exception {
        Response response = httpTransfer.getResponse();
        AbstractService service = (AbstractService)serviceHolder.getServiceModel();
        try {
            response = service.handle(httpTransfer);
        } catch(Exception e) {
            if(service.errorHandling(httpTransfer.getResponse(), e) != null) {
                throw e;
            }
            logger.error(Context.messages().warn(2, serviceHolder.getServiceModel().getClass().getName(), e.getMessage()));
        }
        return response;
    }

    /**
     * Invoke method
     * @param object
     * @param method
     * @param params
     * @throws HTTPException
     */
    public static void invokeMethod(Object object, String methodName, Object... params) throws Exception {
        Method method = object.getClass().getDeclaredMethod(methodName, Arrays.asList(params).stream().map(o -> o.getClass()).toArray(Class[]::new));
        invokeMethod(object, method, params);
    }

    /**
     * Invoke method
     * @param object
     * @param method
     * @param params
     * @throws HTTPException
     */
    public static void invokeMethod(Object object, Method method, Object... params) throws Exception {
        method.invoke(object, params);
    }
}
