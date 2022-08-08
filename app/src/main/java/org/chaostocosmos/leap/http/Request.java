package org.chaostocosmos.leap.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.part.Part;

import ch.qos.logback.classic.Logger;

/**
 * Http request descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class Request {

    final private REQUEST_TYPE requestType;
    final private String hostId;
    final private String requestedHost;
    final private MIME_TYPE contentType;
    final private String httpVersion;
    final private Map<String, String> reqHeader;
    final private String contextPath;
    final private Map<String, Object> contextParam;
    final private Part bodyPart;
    final private long contentLength;
    final private Charset charset;
    
    /**
     * Constructor
     * 
     * @param hostId
     * @param requestHost
     * @param httpVersion
     * @param requestType
     * @param reqHeader
     * @param contextPath
     * @param url
     * @param contextParam
     * @param bodyPart
     * @param contentLength
     * @param charset
     */
    public Request(
            String hostId,
            String requestHost,
            String httpVersion, 
            REQUEST_TYPE requestType, 
            Map<String, String> reqHeader, 
            MIME_TYPE contentType,
            String contextPath, 
            Map<String,Object> contextParam,
            Part bodyPart,
            long contentLength,
            Charset charset
        ) {
            this.hostId = hostId;
            this.requestedHost = requestHost;
            this.httpVersion = httpVersion;
            this.requestType = requestType;
            this.reqHeader = reqHeader;
            this.contentType = contentType;
            this.contextPath = contextPath;
            this.contextParam = contextParam;
            this.bodyPart = bodyPart;
            this.contentLength = contentLength;
            this.charset = charset;
    }

    public final String getHostId() {
        return this.hostId;
    }

    public final String getRequestedHost() {
        return this.requestedHost;
    }

    public final String getHttpVersion() {
        return this.httpVersion;
    }

    public final REQUEST_TYPE getRequestType() {
        return this.requestType;
    }

    public final Map<String, String> getReqHeader() {
        return this.reqHeader;
    }

    public final MIME_TYPE getContentType() {
        return this.contentType;
    }

    public final String getContextPath() {
        return this.contextPath;
    }

    public final Map<String, Object> getContextParam() {
        return this.contextParam;
    }

    public final Object getParameter(String name) {
        return this.contextParam.get(name);
    }

    public final Part getBodyPart() {
        return this.bodyPart;
    }

    public final long getContentLength() {
        return this.contentLength;
    }

    public final Charset charset() {
        return this.charset;
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
            ", httpVersion='" + getHttpVersion() + "'" +
            ", requestType='" + getRequestType() + "'" +
            ", requestedHost='" + getRequestedHost() + "'" +
            ", reqHeader='" + getReqHeader() + "'" +
            ", contentType='" + getContentType() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", contextParam='" + getContextParam() + "'" +
            ", bodyPart='" + getBodyPart() + "'" +
            ", contentLength='" + getContentLength() + "'" +
            ", charset='" + charset() + "'" +
            "}";
    }
}
