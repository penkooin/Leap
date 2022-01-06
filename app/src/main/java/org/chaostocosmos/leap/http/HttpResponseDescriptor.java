package org.chaostocosmos.leap.http;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Http response descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpResponseDescriptor {

    /**
     * Http request descriptor
     */
    private HttpRequestDescriptor httpRequestDescriptor;

    /**
     * HttpResponse object
     */
    private HttpResponse<byte[]> httpResponse;

    /**
     * Content type
     */
    private String contentType;

    /**
     * Response code
     */
    private int responseCode;
    
    /**
     * Response body bytes
     */
    private byte[] responseBody;

    /**
     * Response header map
     */
    private Map<String, Object> responseHeader;

    /**
     * Construct with HttpRequestDescriptor
     * @param requestDescriptor
     */
    public HttpResponseDescriptor(HttpRequestDescriptor requestDescriptor) {
        this(requestDescriptor, 200, "text/html; charset=utf-8", null, new HashMap<>());
    }

    /**
     * Construct with parameters
     * @param httpRequestDescriptor
     * @param responseCode
     * @param contentType
     * @param responseBody
     * @param responseHeader
     */
    public HttpResponseDescriptor(HttpRequestDescriptor httpRequestDescriptor, int responseCode, String contentType, byte[] responseBody, Map<String, Object> responseHeader) {
        this.httpRequestDescriptor = httpRequestDescriptor;
        this.responseCode = responseCode;
        this.contentType = contentType;
        this.responseBody = responseBody;
        this.responseHeader = responseHeader;
    }

    /**
     * Get HttpRequestDescriptor object
     * @return
     */
    public HttpRequestDescriptor getHttpRequestDescriptor() {
        return this.httpRequestDescriptor;
    }

    public void setHttpRequestDescriptor(HttpRequestDescriptor httpRequestDescriptor) {
        this.httpRequestDescriptor = httpRequestDescriptor;
    }

    public HttpResponse<byte[]> getHttpResponse() {
        return this.httpResponse;
    }

    public void setHttpResponse(HttpResponse<byte[]> httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getRequestedHost() {
        if(this.httpRequestDescriptor != null) {
            return this.httpRequestDescriptor.getRequestedHost();
        }
        return null;
    }

    public void setRequestedHost(String requestedHost) {
        this.httpRequestDescriptor.setRequestedHost(requestedHost);
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    public Map<String, Object> getResponseHeader() {
        return this.responseHeader;
    }

    public void setResponseHeader(Map<String, Object> responseHeader) {
        this.responseHeader = responseHeader;
    }

    public void addHeader(String name, Object value) {
        this.responseHeader.put(name, value);
    }

    public void removeHeader(String name) {
        this.responseHeader.remove(name);
    }

    @Override
    public String toString() {
        return "{" +
            " httpRequestDescriptor='" + getHttpRequestDescriptor() + "'" +
            ", httpResponse='" + getHttpResponse() + "'" +
            ", requestedHost='" + getRequestedHost() + "'" +
            ", responseCode='" + getResponseCode() + "'" +
            ", responseBody='" + getResponseBody() + "'" +
            ", responseHeader='" + getResponseHeader() + "'" +
            "}";
    }
}
