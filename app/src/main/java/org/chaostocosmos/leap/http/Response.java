package org.chaostocosmos.leap.http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http response descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class Response {
    /**
     * Reponse host ID
     */
    final String hostId;
    /**
     * Http request
     */
    final private Request request;
    
    /**
     * Response code
     */
    private int responseCode;    
    
    /**
     * Response body
     */
    private Object responseBody;
    
    /**
     * Body length
     */
    private long contentLength;
    
    /**
     * Response header map
     */
    private Map<String, List<Object>> headers;

    /**
     * Construct with parameters
     * @param request
     * @param statusCode
     * @param responseBody
     * @param headers
     */ 
    public Response(final Request request, int statusCode, Object responseBody, Map<String, List<Object>> headers) {
        this.request = request;
        this.hostId = request.getHostId();
        this.responseCode = statusCode;
        this.responseBody = responseBody;
        this.headers = headers;
        if(responseBody != null) {
            this.contentLength = responseBody instanceof byte[] ? ((byte[])responseBody).length : responseBody instanceof File ? ((File)responseBody).length() : -1;
        }
    }

    /**
     * Get host ID
     * @return
     */
    public final String getHostId() {
        return this.hostId;
    }

    /**
     * Get HttpRequestDescriptor object
     * @return
     */
    public final Request getRequest() {
        return this.request;
    }

    public final String getRequestedHost() {
        if(this.request != null) {
            return this.request.getRequestedHost();
        }
        return null;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Object getBody() {
        return this.responseBody;
    }

    public void setBody(Object responseBody) {
        this.responseBody = responseBody;
        this.contentLength = responseBody instanceof byte[] ? ((byte[])responseBody).length : responseBody instanceof File ? ((File)responseBody).length() : -1;
    }

    public Map<String, List<Object>> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, List<Object>> responseHeader) {
        this.headers = responseHeader;
    }

    public void addHeader(String name, Object value) {
        if(!this.headers.containsKey(name)) {
            List<Object> values = new ArrayList<>();
            values.add(value);
            this.headers.put(name, values);
        } else {
            this.headers.get(name).add(value);
        }
    }

    public void setHeader(String name, Object value) {
        if(this.headers.containsKey(name)) {
            this.headers.get(name).add(value);
        } else {
            throw new IllegalStateException("Specified name of key must be exist in response headers: "+name);
        }
    }

    public void removeHeader(String name) {
        this.headers.remove(name);
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public String toString() {
        return "{" +
            " httpRequestDescriptor='" + request + "'" +
            ", responseCode='" + responseCode + "'" +
            ", responseBody='" + responseBody + "'" +
            ", contentLength='" + contentLength + "'" +
            ", headers='" + headers + "'" +
            "}";
    }    
}
