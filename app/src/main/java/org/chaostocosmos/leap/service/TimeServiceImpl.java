package org.chaostocosmos.leap.service;

import java.lang.instrument.Instrumentation;
import java.util.Date;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.service.abstraction.AbstractService;

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
    public void getTime(HttpRequest request, HttpResponse response) {
        LoggerFactory.getLogger(super.getHost().getId()).debug("getTime servlet started....+++++++++++++++++++++++++++++++++++++++++++++++++");
        String resBody = "<html><title>This is what time</title><body><h2>"+new Date().toString()+"</h2><body></html>";
        response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType());
        response.setResponseCode(200);
        response.setBody(resBody.getBytes());
    }

    @Override
    public Exception errorHandling(HttpResponse response, Exception e) {
        return e;
    }
} 
