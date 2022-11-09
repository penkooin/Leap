package org.chaostocosmos.leap.http.service;

import java.util.Date;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.MethodMapper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MIME;
import org.chaostocosmos.leap.http.enums.REQUEST;

/**
 * Time serving servlet object
 * 
 * @author 9ins
 * @since 2021.09.17
 */
@ServiceMapper(mappingPath = "/time")
public class TimeServiceImpl extends AbstractService {  
    
    public String cloneTestString = "";

    /**
     * Get current time
     * @param request
     * @param response
     */
    @MethodMapper(method = REQUEST.GET, mappingPath = "/GetTime", autheticated = {}, allowed = {}, forbidden = {})
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
