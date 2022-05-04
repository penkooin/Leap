package org.chaostocosmos.leap.http.services;

import java.util.Date;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
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
    public void getTime(Request request, Response response) {
        LoggerFactory.getLogger(request.getRequestedHost()).debug("getTime servlet started....+++++++++++++++++++++++++++++++++++++++++++++++++");
        String resBody = "<html><title>This is what time</title><body><h2>"+new Date().toString()+"</h2><body></html>";
        response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType());
        response.setResponseCode(200);
        response.setBody(resBody.getBytes());
    }

    @Override
    public Throwable errorHandling(Response response, Throwable t) throws Throwable {
        response.addHeader("WWW-Authenticate", "Basic");
        response.setResponseCode(401);
        response.setBody("".getBytes());
        super.sendResponse(response);
        return t;
    }
} 
