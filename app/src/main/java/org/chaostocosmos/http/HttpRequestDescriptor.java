package org.chaostocosmos.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Map;

/**
 * Http request descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpRequestDescriptor {

    HttpRequest httpRequest;
    private String httpVersion;
    private String requestLines;
    private REQUEST_TYPE requestType;
    private Map<String, String> reqHeader;
    private String contentType;
    private String reqBody; 
    private String contextPath;
    private URL url;
    private Map<String, String> contextParam;

    /**
     * Constructor
     * @param httpVersion
     * @param requestLines
     * @param requestType
     * @param reqHeader
     * @param reqBody
     * @param contextPath
     * @param url
     */
    public HttpRequestDescriptor(
                                String httpVersion, 
                                String requestLines, 
                                REQUEST_TYPE requestType, 
                                Map<String,String> reqHeader, 
                                String contentType,
                                String reqBody, 
                                String contextPath, 
                                URL url,
                                Map<String,String> contextParam) {
        this.httpVersion = httpVersion;
        this.requestLines = requestLines;
        this.requestType = requestType;
        this.reqHeader = reqHeader;
        this.contentType = contentType;
        this.reqBody = reqBody;
        this.contextPath = contextPath;
        this.url = url;
        this.contextParam = contextParam;
    }

    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public String getRequestLines() {
        return this.requestLines;
    }

    public REQUEST_TYPE getRequestType() {
        return this.requestType;
    }

    public Map<String,String> getReqHeader() {
        return this.reqHeader;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getReqBody() {
        return this.reqBody;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public URL getUrl() {
        return this.url;
    }

    public Map<String, String> getContextParam() {
        return contextParam;
    }

    public void printURLInfo() throws URISyntaxException {
        URI uri = this.url.toURI();
        System.out.println("getHost :"+uri.getHost());
        System.out.println("getPort :"+uri.getPort());
        System.out.println("getQuery :"+uri.getQuery());
        System.out.println("getPath :"+uri.getPath());
        System.out.println("getRawPath :"+uri.getRawPath());
        System.out.println("getRawQuery :"+uri.getRawQuery());
        System.out.println("getFragment :"+uri.getFragment());
        System.out.println("getScheme :"+uri.getScheme());
        System.out.println("getRawAuthority :"+uri.getRawAuthority());
        System.out.println("getRawUserInfo :"+uri.getRawUserInfo());
        System.out.println("getAuthority :"+uri.getAuthority());
    }

    @Override
    public String toString() {
        return "{" +
            " httpVersion='" + getHttpVersion() + "'" +
            ", requestLines='" + getRequestLines() + "'" +
            ", requestType='" + getRequestType() + "'" +
            ", reqHeader='" + getReqHeader() + "'" +
            ", reqBody='" + getReqBody() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", url='" + getUrl() + "'" +
            ", url='" + getContextParam() + "'" +
            "}";
    }

}
