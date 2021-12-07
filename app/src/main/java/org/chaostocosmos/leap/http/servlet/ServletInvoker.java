package org.chaostocosmos.leap.http.servlet;

import java.lang.reflect.Method;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet management object
 */
public class ServletInvoker {
    /**
     * logger
     */
    public static final Logger logger = LoggerFactory.getLogger(ServletInvoker.class);
    
    /**
     * Call service method on servlet
     * @param servlet
     * @param invokingMethod
     * @param request
     * @param response
     * @throws Exception
     */
    public static void invokeService(ILeapServlet servlet, Method invokingMethod, HttpRequestDescriptor request, HttpResponseDescriptor response) throws Exception {
        servlet.service(request, response, invokingMethod);
    }
}
