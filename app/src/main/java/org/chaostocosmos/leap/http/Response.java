package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.context.Host;

/**
 * Http response descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class Response implements Http {
    /**
     * Host
     */
    private final Host<?> host;
    /**
     * OutputStream
     */
    private final OutputStream outputStream;
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
    private final Map<String, List<String>> headers;
    /**
     * Construct with parameters
     * @param host
     * @param outputStream
     * @param statusCode
     * @param request
     * @param responseBody
     * @param headers
     */ 
    public Response(Host<?> host, OutputStream outputStream, int statusCode, Object responseBody, Map<String, List<String>> headers) {
        this.host = host;
        this.outputStream = outputStream;
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
    public final Host<?> getHost() {
        return this.host;
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

    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public void addHeader(String key, String value) {
        addHeader(key, List.of(value));
    }

    public void addHeader(String key, List<String> value) {
        this.headers.putIfAbsent(key, value);
    }

    public void removeAllHeader() {
        this.headers.clear();
    }

    public void setHeader(String name, String value) {
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

    public void addSetCookie(String attrKey, String attrValue) {
        List<String> cookieList = this.headers.get("Set-Cookie");
        if(cookieList == null) {
            cookieList = new ArrayList<>();
            this.headers.put("Set-Cookie", cookieList);
        }        
        this.headers.get("Set-Cookie").add(attrKey+"="+attrValue);        
    }

    public String getSetCookie(String attrKey) {
        List<String> cookieList = this.headers.get("Set-Cookie");
        return cookieList.stream().filter(c -> c.toString().startsWith(attrKey)).map(c -> c.toString().substring(c.toString().indexOf("=")+1)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "{" +
            " host='" + this.host.toString() + "'" +
            ", responseCode='" + responseCode + "'" +
            ", responseBody='" + responseBody + "'" +
            ", contentLength='" + contentLength + "'" +
            ", headers='" + headers + "'" +
            "}";
    }
}
