package org.chaostocosmos.leap.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.part.Part;
import org.chaostocosmos.leap.session.Session;

/**
 * Http request descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpRequest <T> implements Http {

    /**
     * Host object
     */
    final private Host<?> host; 

    /**
     * Request timestamp
     */
    final private long requestTimstamp;

    /**
     * Request enum
     */
    final private REQUEST requestType;    

    /**
     * Requested host
     */
    final private String requestedHost;

    /**
     * Mime type
     */
    final private MIME contentType;

    /**
     * Http version
     */
    final private String httpVersion;

    /**
     * Requested header
     */
    final private Map<String, String> reqHeader;

    /**
     * Conetxt path
     */
    final private String contextPath;

    /**
     * Query parameter Map
     */
    final private Map<String, String> queryParam;

    /**
     * Request body part
     */
    final private Part<T> body;

    /**
     * Content length
     */
    final private long contentLength;

    /**
     * Charset
     */
    final private Charset charset;

    /**
     * URI
     */
    final private URI requestURI;

    /**
     * Reuqested protocol
     */
    final private PROTOCOL protocol;

    /**
     * Cookies
     */
    private Map<String, String> cookies;

    /**
     * Session object
     */
    private Session session;

    /**
     * Constructor
     * 
     * @param host
     * @param newTimeStamp
     * @param protocol
     * @param requestHost
     * @param httpVersion
     * @param requestType
     * @param reqHeader
     * @param contextPath
     * @param requestURI
     * @param url
     * @param queryParam
     * @param body
     * @param contentLength
     * @param charset
     * @param cookies
     * @param session
     * @throws URISyntaxException
     */
    public HttpRequest(
            Host<?> host,
            long newTimeStamp,
            PROTOCOL protocol,
            String requestHost,
            String httpVersion, 
            REQUEST requestType, 
            Map<String, String> reqHeader, 
            MIME contentType,
            String contextPath,
            URI requestURI, 
            Map<String, String> queryParam,
            Part<T> body,
            long contentLength,
            Charset charset,
            Map<String, String> cookies, 
            Session session
        ) throws URISyntaxException {
            this.host = host;
            this.requestTimstamp = newTimeStamp;
            this.protocol = protocol;
            this.requestedHost = requestHost;
            this.httpVersion = httpVersion;
            this.requestType = requestType;
            this.reqHeader = reqHeader;
            this.contentType = contentType;
            this.contextPath = contextPath;
            this.requestURI = requestURI;
            this.queryParam = queryParam;
            this.body = body;
            this.contentLength = contentLength;
            this.charset = charset;
            this.cookies = cookies;
            this.session = session;
    }

    /**
     * Get timestamp
     * @return
     */
    public final long getTimestamp() {
        return this.requestTimstamp;
    }

    /**
     * Get protocol
     * @return
     */
    public final PROTOCOL getProtocol() {
        return this.protocol;
    }

    /**
     * Get requested host id
     * @return
     */
    public final String getHostId() {
        return this.host.getId();
    }

    /**
     * Get rquested host name
     * @return
     */
    public final String getRequestedHost() {
        return this.requestedHost;
    }

    /**
     * Get HTTP version
     * @return
     */
    public final String getHttpVersion() {
        return this.httpVersion;
    }

    /**
     * Get request type 
     * @return
     */
    public final REQUEST getRequestType() {
        return this.requestType;
    }

    /**
     * Get request header Map
     * @return
     */
    public final Map<String, String> getHeaders() {
        return this.reqHeader;
    }

    /**
     * Get header value 
     * @param headerKey
     * @return
     */
    public final String getHeader(String headerKey) {
        return this.reqHeader.get(headerKey);
    }

    /**
     * Get header value for the key
     * @param headerKey
     * @param valueIdx
     * @return
     */
    public final List<String> getHeader(String headerKey, String spliter) {
        return Stream.of(this.reqHeader.get(headerKey).split(spliter)).map(h -> h.trim()).collect(Collectors.toList());
    }

    /**
     * Get content type
     * @return
     */
    public final MIME getContentType() {
        return this.contentType;
    }

    /**
     * Get context path
     * @return
     */
    public final String getContextPath() {
        return this.contextPath;
    }

    /**
     * Get request URI
     * @return
     */
    public final URI getRequestURI() {
        return this.requestURI;
    }

    /**
     * Get context parameters
     * @return
     */
    public final Map<String, String> getParameters() {
        return this.queryParam;
    }

    /**
     * Get query parameters
     * @param name
     * @return
     */
    public final Object getParameter(String name) {
        return this.queryParam.get(name);
    }

    /**
     * Get get body part object
     * @return
     */
    public Part<T> getBody() {
        return this.body;
    }

    /**
     * Whether existing body content
     * @return
     */
    public boolean isBody() {
        return this.body.isBody();
    }

    /**
     * Get content length
     * @return
     */
    public final long getContentLength() {
        return this.contentLength;
    }

    /**
     * Get charset
     * @return
     */
    public final Charset charset() {
        return this.charset;
    }

    /**
     * Get cookies
     * @return
     */
    public final Map<String, String> getCookies() {
        return this.cookies;
    }

    /**
     * Get cookies by attribute key
     * @param attrKey
     * @return
     */
    public final String getCookie(String attrKey) {
        if(this.cookies == null) {
            return null;
        }        
        return this.cookies.get(attrKey);
    }

    /**
     * Set cookie with attribute key/value
     * @param attrKey
     * @param attrValue
     */
    public final void setCookie(String attrKey, String attrValue) {
        if(this.cookies == null) {
            this.cookies = new HashMap<>();
        }
        this.cookies.put(attrKey, attrValue);
    }

    /**
     * Get session object
     * @return
     */
    public final Session getSession() {
        return this.session;
    }

    /**
     * Set session object
     * @param session
     */
    public final void setSession(Session session) {
        this.session = session;
    }

    /**
     * Print URL infomation
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public void printURLInfo() throws URISyntaxException, MalformedURLException {
        Logger logger = (Logger)LoggerFactory.getLogger(this.requestedHost);
        logger.debug(this.toString());
    }

    @Override
    public String toString() {
        return "{" +
            " requestType='" + requestType + "'" +
            ", host='" + host + "'" +
            ", requestedHost='" + requestedHost + "'" +
            ", contentType='" + contentType + "'" +
            ", httpVersion='" + httpVersion + "'" +
            ", reqHeader='" + reqHeader + "'" +
            ", contextPath='" + contextPath + "'" +
            ", queryParam='" + queryParam + "'" +
            ", bodyPart='" + body + "'" +
            ", contentLength='" + contentLength + "'" +
            ", charset='" + charset + "'" +
            ", requestURI='" + requestURI + "'" +
            ", protocol='" + protocol + "'" +
            ", cookies='" + cookies + "'" +
            ", session='" + session + "'" +
            "}";
    }
}
