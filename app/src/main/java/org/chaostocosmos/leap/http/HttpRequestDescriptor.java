package org.chaostocosmos.leap.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.part.BodyPart;

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
    private REQUEST_TYPE requestType;
    private String requestedHost;
    private Map<String, String> reqHeader;
    private String contentType;
    private byte[] reqBody; 
    private String contextPath;
    private Map<String, String> contextParam;
    private BodyPart bodyPart;
    private long contentLength;
    
    /**
     * Constructor
     * 
     * @param httpVersion
     * @param requestType
     * @param requestHost
     * @param reqHeader
     * @param reqBody
     * @param contextPath
     * @param url
     * @param contextParam
     * @param bodyPart
     * @param contentLength
     */
    public HttpRequestDescriptor(
                                String httpVersion, 
                                REQUEST_TYPE requestType, 
                                String requestHost,
                                Map<String,String> reqHeader, 
                                String contentType,
                                byte[] reqBody, 
                                String contextPath, 
                                Map<String,String> contextParam,
                                BodyPart bodyPart,
                                long contentLength
                                ) {
        this.httpVersion = httpVersion;
        this.requestType = requestType;
        this.requestedHost = requestHost;
        this.reqHeader = reqHeader;
        this.contentType = contentType;
        this.reqBody = reqBody;
        this.contextPath = contextPath;
        this.contextParam = contextParam;
        this.bodyPart = bodyPart;
        this.contentLength = contentLength;
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

    public REQUEST_TYPE getRequestType() {
        return this.requestType;
    }

    public String getRequestedHost() {
        return this.requestedHost;
    }

    public void setRequestedHost(String requestedHost) {
        this.requestedHost = requestedHost;
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

    public Map<String, String> getContextParam() {
        return this.contextParam;
    }

    public BodyPart getBodyPart() {
        return this.bodyPart;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public void printURLInfo() throws URISyntaxException, MalformedURLException {
        Logger logger = (Logger)LoggerFactory.getLogger(this.requestedHost);
        String url = !this.requestedHost.startsWith("http") ? "http://"+this.requestedHost : requestedHost;
        URI uri = new URL(url).toURI();
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
            ", requestType='" + getRequestType() + "'" +
            ", requestedHost='" + getRequestedHost() + "'" +
            ", reqHeader='" + getReqHeader() + "'" +
            ", contentType='" + getContentType() + "'" +
            ", reqBody='" + getReqBody() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", contextParam='" + getContextParam() + "'" +
            ", bodyPart='" + getBodyPart() + "'" +
            ", contentLength='" + getContentLength() + "'" +
            "}";
    }
}
