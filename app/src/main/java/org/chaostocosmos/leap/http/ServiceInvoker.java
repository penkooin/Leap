package org.chaostocosmos.leap.http;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.services.AbstractService;
import org.slf4j.Logger;

/**
 * Servlet management object
 * @author 9ins
 */
public class ServiceInvoker {
    /**
     * logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId()); 
    
    /**
     * Call service method on servlet
     * @param serviceHolder
     * @param httpTransfer
     * @param doClone
     * @throws Exception
     * @throws CloneNotSupportedException
     */
    public static Response invokeServiceMethod(ServiceHolder serviceHolder, HttpTransfer httpTransfer) throws Throwable {
        Response response = httpTransfer.getResponse();
        AbstractService service = (AbstractService)serviceHolder.getService();
        try {
            response = service.serve(httpTransfer, serviceHolder.getServiceMethod());
        } catch(Throwable e) {
            if(service.errorHandling(httpTransfer.getResponse(), e) != null) {
                throw e;
            }
            logger.error(Context.getMessages().getWarnMsg(2, serviceHolder.getService().getClass().getName(), e.getMessage()));
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
        Method method = object.getClass().getDeclaredMethod(methodName, Arrays.asList(params).stream().map(o -> o.getClass()).toArray(Class[]::new));
        invokeMethod(object, method, params);
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
