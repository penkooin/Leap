package org.chaostocosmos.leap.http.servlet;

import java.util.Date;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.REQUEST_TYPE;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServletMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Time serving servlet object
 * 
 * @author 9ins
 * @since 2021.09.17
 */
@ServletMapper(path = "/time")
public class TimeServletImpl extends AbstractLeapServlet { 
    /**
     * logger
     */
    Logger logger = LoggerFactory.getLogger(TimeServletImpl.class);
    
    /**
     * Constructor
     */
    public TimeServletImpl() {}  

    @MethodMappper(requestMethod = REQUEST_TYPE.GET, path = "/GetTime")
    public void getTime(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        logger.info("getTime servlet started....++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        String resBody = "<html><title>This is what time</title><body><h2>"+new Date().toString()+"</h2><body></html>";
        response.setBody(resBody);
    } 
}
