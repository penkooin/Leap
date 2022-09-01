package org.chaostocosmos.leap.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpResponseBuilder
 * 
 * @author 9ins
 */
public class HttpResponseBuilder {
    /**
     * Response object
     */
    Response httpResponse = null;

    /**
     * HttpResponseBuilder static object
     */
    private static HttpResponseBuilder httpResponseBuilder;

    /**
     * Default constructor
     */
    private HttpResponseBuilder() {
        this.httpResponse = null;
    }

    /**
     * Get response builder
     * @return
     */
    public static HttpResponseBuilder getBuilder() {
        if(httpResponseBuilder == null) {
            httpResponseBuilder = new HttpResponseBuilder();
        }
        return httpResponseBuilder;
    }

    /**
     * Build response
     * @param request
     * @return
     * @throws HTTPException
     */
    public HttpResponseBuilder build(Request request) throws HTTPException {
        this.httpResponse =  HttpParser.buildResponseParser().buildResponse(request, -1, null, new HashMap<String, List<String>>()); 
        return httpResponseBuilder;
    }

    /**
     * Set status code
     * @param statusCode
     * @return
     */
    public HttpResponseBuilder setStatusCode(int statusCode) {
        this.httpResponse.setResponseCode(statusCode);
        return httpResponseBuilder;
    }

    /**
     * Set body
     * @param body
     * @return
     */
    public HttpResponseBuilder setBody(Object body) {
        this.httpResponse.setBody(body);
        return httpResponseBuilder;
    }

    /**
     * Set headers
     * @param headers
     * @return
     */
    public HttpResponseBuilder setHeaders(Map<String, List<String>> headers) {
        this.httpResponse.setHeaders(headers);
        return httpResponseBuilder;
    }

    /**
     * Add header attribute key / value
     * @param name
     * @param value
     * @return
     */
    public HttpResponseBuilder addHeader(String name, String value) {
        this.httpResponse.addHeader(name, value);
        return httpResponseBuilder;
    }

    /**
     * Get response object
     * @return
     */
    public Response get() {
        return httpResponse;
    }
}
