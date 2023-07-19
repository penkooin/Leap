package org.chaostocosmos.leap.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.part.Part;
import org.chaostocosmos.leap.session.Session;

import ch.qos.logback.classic.Logger;

/**
 * Http request descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpRequest implements Http {

    final private Host<?> host; 
    final private long requestTimstamp;
    final private REQUEST requestType;    
    final private String requestedHost;
    final private MIME contentType;
    final private String httpVersion;
    final private Map<String, Object> reqHeader;
    final private String contextPath;
    final private Map<String, String> queryParam;
    final private Part bodyPart;
    final private long contentLength;
    final private Charset charset;
    final private URI requestURI;
    final private PROTOCOL protocol;
    private Map<String, String> cookies;
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
     * @param bodyPart
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
            Map<String, Object> reqHeader, 
            MIME contentType,
            String contextPath,
            URI requestURI, 
            Map<String, String> queryParam,
            Part bodyPart,
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
            this.bodyPart = bodyPart;
            this.contentLength = contentLength;
            this.charset = charset;
            this.cookies = cookies;
            this.session = session;
    }

    public final long getTimestamp() {
        return this.requestTimstamp;
    }

    public final PROTOCOL getProtocol() {
        return this.protocol;
    }

    public final String getHostId() {
        return this.host.getHostId();
    }

    public final String getRequestedHost() {
        return this.requestedHost;
    }

    public final String getHttpVersion() {
        return this.httpVersion;
    }

    public final REQUEST getRequestType() {
        return this.requestType;
    }

    public final Map<String, Object> getReqHeader() {
        return this.reqHeader;
    }

    public final MIME getContentType() {
        return this.contentType;
    }

    public final String getContextPath() {
        return this.contextPath;
    }

    public final URI getRequestURI() {
        return this.requestURI;
    }

    public final Map<String, String> getContextParam() {
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

    public final String getCookie(String attrKey) {
        if(this.cookies == null) {
            return null;
        }        
        return this.cookies.get(attrKey);
    }

    public final void setCookie(String attrKey, String attrValue) {
        if(this.cookies == null) {
            this.cookies = new HashMap<>();
        }
        this.cookies.put(attrKey, attrValue);
    }

    public final Session getSession() {
        return this.session;
    }

    public final void setSession(Session session) {
        this.session = session;
    }

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
            ", bodyPart='" + bodyPart + "'" +
            ", contentLength='" + contentLength + "'" +
            ", charset='" + charset + "'" +
            ", requestURI='" + requestURI + "'" +
            ", protocol='" + protocol + "'" +
            ", cookies='" + cookies + "'" +
            ", session='" + session + "'" +
            "}";
    }
}
