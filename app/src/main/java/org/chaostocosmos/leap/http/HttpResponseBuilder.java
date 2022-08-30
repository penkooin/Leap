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

    Response httpResponse;

    private static HttpResponseBuilder httpResponseBuilder;

    private HttpResponseBuilder() {
        this.httpResponse = null;
    }

    public static HttpResponseBuilder getBuilder() {
        if(httpResponseBuilder == null) {
            httpResponseBuilder = new HttpResponseBuilder();
        }
        return httpResponseBuilder;
    }

    public HttpResponseBuilder build(Request httpRequest) throws HTTPException {
        this.httpResponse =  HttpParser.buildResponseParser().buildResponse(httpRequest, -1, null, new HashMap<String, List<String>>()); 
        return httpResponseBuilder;
    }

    public HttpResponseBuilder setStatusCode(int statusCode) {
        this.httpResponse.setResponseCode(statusCode);
        return httpResponseBuilder;
    }

    public HttpResponseBuilder setBody(Object body) {
        this.httpResponse.setBody(body);
        return httpResponseBuilder;
    }

    public HttpResponseBuilder setHeaders(Map<String, List<String>> headers) {
        this.httpResponse.setHeaders(headers);
        return httpResponseBuilder;
    }

    public HttpResponseBuilder addHeader(String name, String value) {
        this.httpResponse.addHeader(name, value);
        return httpResponseBuilder;
    }

    public Response get() {
        return httpResponse;
    }
}
