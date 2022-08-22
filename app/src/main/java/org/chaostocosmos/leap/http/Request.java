package org.chaostocosmos.leap.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
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
    final private Map<String, Object> queryParam;
    final private Part bodyPart;
    final private long contentLength;
    final private Charset charset;
    final private URI requestURI;
    final private PROTOCOL protocol;
    final private Map<String, String> cookies;
    
    /**
     * Constructor
     * 
     * @param protocol
     * @param hostId
     * @param requestHost
     * @param httpVersion
     * @param requestType
     * @param reqHeader
     * @param contextPath
     * @param requestURI
     * @param url
     * @param queryParam
     * @param bodyPart
     * @param contentLength
     * @param charset
     * @param cookies
     * @throws URISyntaxException
     */
    public Request(
            PROTOCOL protocol,
            String hostId,
            String requestHost,
            String httpVersion, 
            REQUEST_TYPE requestType, 
            Map<String, String> reqHeader, 
            MIME_TYPE contentType,
            String contextPath,
            URI requestURI, 
            Map<String, Object> queryParam,
            Part bodyPart,
            long contentLength,
            Charset charset,
            Map<String, String> cookies
        ) throws URISyntaxException {
            this.protocol = protocol;
            this.hostId = hostId;
            this.requestedHost = requestHost;
            this.httpVersion = httpVersion;
            this.requestType = requestType;
            this.reqHeader = reqHeader;
            this.contentType = contentType;
            this.contextPath = contextPath;
            this.requestURI = requestURI;
            this.queryParam = queryParam;
            this.bodyPart = bodyPart;
            this.contentLength = contentLength;
            this.charset = charset;
            this.cookies = cookies;
    }

    public final PROTOCOL getProtocol() {
        return this.protocol;
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

    public final URI getRequestURI() {
        return this.requestURI;
    }

    public final Map<String, Object> getContextParam() {
        return this.queryParam;
    }

    public final Object getParameter(String name) {
        return this.queryParam.get(name);
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

    public final Map<String, String> getCookies() {
        return this.cookies;
    }

    public final String getCookies(String attrKey) {
        return this.cookies.get(attrKey);
    }

    public void printURLInfo() throws URISyntaxException, MalformedURLException {
        Logger logger = (Logger)LoggerFactory.getLogger(this.requestedHost);
        logger.debug(this.toString());
    }

    @Override
    public String toString() {
        return "{" +
            " requestType='" + requestType + "'" +
            ", hostId='" + hostId + "'" +
            ", requestedHost='" + requestedHost + "'" +
            ", contentType='" + contentType + "'" +
            ", httpVersion='" + httpVersion + "'" +
            ", reqHeader='" + reqHeader + "'" +
            ", contextPath='" + contextPath + "'" +
            ", queryParam='" + queryParam + "'" +
            ", bodyPart='" + bodyPart + "'" +
            ", contentLength='" + contentLength + "'" +
            ", charset='" + charset + "'" +
            ", requestURI='" + requestURI + "'" +
            ", protocol='" + protocol + "'" +
            ", cookies='" + cookies + "'" +
            "}";
    }
}
