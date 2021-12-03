package org.chaostocosmos.http;

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

    private HttpResponse httpResponse;
    private int responseCode;
    private Map<String, Object> header = new HashMap<>();
    private String contentType;
    private String body;

    /**
     * constructor with response header Map
     */
    public HttpResponseDescriptor() {
    }

    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Map<String,Object> getHeader() {
        return this.header;
    }

    public void setHeader(Map<String,Object> header) {
        this.header = header;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addHeader(String key, Object value) {
        this.header.put(key, value);
    }

    @Override
    public String toString() {
        return "{" +
            " httpResponse='" + httpResponse + "'" +
            ", responseCode='" + responseCode + "'" +
            ", header='" + header + "'" +
            ", contentType='" + contentType + "'" +
            ", responseCode='" + body + "'" +
            "}";
    }
}
