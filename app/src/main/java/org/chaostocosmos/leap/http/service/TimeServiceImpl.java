package org.chaostocosmos.leap.http.service;

import java.util.Date;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MIME;
import org.chaostocosmos.leap.http.enums.REQUEST;
import org.chaostocosmos.leap.http.inject.FilterIndicates;
import org.chaostocosmos.leap.http.inject.MethodIndicates;
import org.chaostocosmos.leap.http.inject.ServiceIndicates;
import org.chaostocosmos.leap.http.service.filter.BasicAuthFilter;
import org.chaostocosmos.leap.http.service.filter.BasicHttpRequestFilter;

/**
 * Time serving servlet object
 * 
 * @author 9ins
 * @since 2021.09.17
 */
@ServiceIndicates(path = "/time")
public class TimeServiceImpl extends AbstractService {  

    public String cloneTestString = "";

    /**
     * Get current time
     * @param request
     * @param response
     */
    @MethodIndicates(method = REQUEST.GET, path = "/GetTime")
    @FilterIndicates(preFilters = {BasicAuthFilter.class, BasicHttpRequestFilter.class})
    public void getTime(Request request, Response response) {
        LoggerFactory.getLogger(request.getRequestedHost()).debug("getTime servlet started....+++++++++++++++++++++++++++++++++++++++++++++++++");
        String resBody = "<html><title>This is what time</title><body><h2>"+new Date().toString()+"</h2><body></html>";
        response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType());
        response.setResponseCode(200);
        response.setBody(resBody.getBytes());
    }

    @Override
    public Exception errorHandling(Response response, Exception e) {
        return e;
    }
} 
