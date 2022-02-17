package org.chaostocosmos.leap.http.services;

import java.util.Date;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.BasicAuthFilter;
import org.chaostocosmos.leap.http.filters.BasicHttpFilter;

/**
 * Time serving servlet object
 * 
 * @author 9ins
 * @since 2021.09.17
 */
@ServiceMapper(path = "/time")
public class TimeServiceImpl extends AbstractLeapService {  

    public String cloneTestString = "";

    /**
     * Get current time
     * @param request
     * @param response
     */
    @MethodMappper(mappingMethod = REQUEST_TYPE.GET, path = "/GetTime")
    @FilterMapper(preFilters = {BasicAuthFilter.class, BasicHttpFilter.class})
    public void getTime(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        LoggerFactory.getLogger(request.getRequestedHost()).debug("getTime servlet started....+++++++++++++++++++++++++++++++++++++++++++++++++");
        String resBody = "<html><title>This is what time</title><body><h2>"+new Date().toString()+"</h2><body></html>";
        response.setStatusCode(200);
        response.setBody(resBody.getBytes());
    }

    @Override
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable t) throws Throwable {
        response.addHeader("WWW-Authenticate", "Basic");
        response.setStatusCode(401);
        response.setBody("".getBytes());
        return null;
    }
} 
