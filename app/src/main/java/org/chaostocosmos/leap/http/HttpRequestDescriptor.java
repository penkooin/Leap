package org.chaostocosmos.leap.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Http request descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpRequestDescriptor {

    private HttpRequest httpRequest;
    private String httpVersion;
    private String requestLines;    
    private REQUEST_TYPE requestType;
    private String requestedHost;
    private Map<String, String> reqHeader;
    private String contentType;
    private byte[] reqBody; 
    private String contextPath;
    private URL url;
    private Map<String, String> contextParam;

    /**
     * Constructor
     * @param httpVersion
     * @param requestLines
     * @param requestType
     * @param requestHost
     * @param reqHeader
     * @param reqBody
     * @param contextPath
     * @param url
     */
    public HttpRequestDescriptor(
                                String httpVersion, 
                                String requestLines, 
                                REQUEST_TYPE requestType, 
                                String requestHost,
                                Map<String,String> reqHeader, 
                                String contentType,
                                byte[] reqBody, 
                                String contextPath, 
                                URL url,
                                Map<String,String> contextParam) {
        this.httpVersion = httpVersion;
        this.requestLines = requestLines;
        this.requestType = requestType;
        this.requestedHost = requestHost;
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

    public String getRequestedHost() {
        return this.requestedHost;
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

    public byte[] getReqBody() {
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
        Logger logger = (Logger)LoggerFactory.getLogger(this.requestedHost);
        URI uri = this.url.toURI();
        logger.debug("getHost :"+uri.getHost());
        logger.debug("getPort :"+uri.getPort());
        logger.debug("getQuery :"+uri.getQuery());
        logger.debug("getPath :"+uri.getPath());
        logger.debug("getRawPath :"+uri.getRawPath());
        logger.debug("getRawQuery :"+uri.getRawQuery());
        logger.debug("getFragment :"+uri.getFragment());
        logger.debug("getScheme :"+uri.getScheme());
        logger.debug("getRawAuthority :"+uri.getRawAuthority());
        logger.debug("getRawUserInfo :"+uri.getRawUserInfo());
        logger.debug("getAuthority :"+uri.getAuthority());
    }

    @Override
    public String toString() {
        return "{" +
            " httpRequest='" + getHttpRequest() + "'" +
            ", httpVersion='" + getHttpVersion() + "'" +
            ", requestLines='" + getRequestLines() + "'" +
            ", requestType='" + getRequestType() + "'" +
            ", requestHost='" + getRequestedHost() + "'" +
            ", reqHeader='" + getReqHeader() + "'" +
            ", contentType='" + getContentType() + "'" +
            ", reqBody='" + getReqBody() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", url='" + getUrl() + "'" +
            ", contextParam='" + getContextParam() + "'" +
            "}";
    }
}
