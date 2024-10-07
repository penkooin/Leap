package org.chaostocosmos.leap.http.part;

import java.nio.charset.Charset;

import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HttpRequestStream;

/**
 * BodyPart
 * 
 * @author 9ins
 */
public abstract class AbstractPart <T> implements Part <T> {

    /**
     * Logger
     */
    Logger logger;

    /**
     * Host
     */
    Host<?> host;

    /**
     * Content length
     */
    long contentLength;    

    /**
     * body data
     */
    T body;

    /**
     * body charset
     */
    Charset charset;
    
    /**
     * Content type
     */
    MIME contentType;

    /**
     * Request stream
     */
    HttpRequestStream requestStream;

    /**
     * Constructor
     * 
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param charset
     */
    public AbstractPart(Host<?> host, 
                        MIME contentType, 
                        long contentLength, 
                        HttpRequestStream requestStream, 
                        Charset charset) {
        this.host = host;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.requestStream = requestStream;        
        this.charset = charset;
        this.logger = host.getLogger();
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }

    @Override
    public MIME getContentType() {
        return this.contentType;
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    /**
     * Get host ID
     * @return
     */
    public String getHostId() {
        return this.host.getId();
    }

    /**
     * Get Host instance
     * @return
     */
    public Host<?> getHost() {
        return this.host;
    }

    @Override
    public String toString() {
        return "{" +
            " logger='" + logger + "'" +
            ", host='" + getHostId() + "'" +
            ", contentType='" + contentType + "'" +
            ", contentLength='" + contentLength + "'" +
            ", requestStream='" + requestStream + "'" +
            ", body='" + body + "'" +
            ", charset='" + charset + "'" +
            "}";
    }
}
