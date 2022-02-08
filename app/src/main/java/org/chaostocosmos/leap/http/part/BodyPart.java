package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;

import ch.qos.logback.classic.Logger;

/**
 * BodyPart
 * 
 * @author 9ins
 */
public abstract class BodyPart {
    /**
     * Logger
     */
    Logger logger;
    /**
     * Host
     */
    String host;
    /**
     * Content type
     */
    MIME_TYPE contentType;
    /**
     * Content length
     */
    long contentLength;    
    /**
     * Request stream
     */
    InputStream requestStream;
    /**
     * whether stream closed
     */
    boolean isClosedStream = false;
    /**
     * Whether body loaded
     */
    boolean isLoadedBody = false; 
    /**
     * body data
     */
    byte[] body;
    /**
     * body charset
     */
    Charset charset;

    /**
     * Constructor
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param loadBody
     * @param charset
     * @throws IOException
     */
    public BodyPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream, boolean loadBody, Charset charset) throws IOException {
        this.logger = LoggerFactory.getLogger(this.host);
        this.host = host;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.requestStream = requestStream;
        if(loadBody) {
            body = getAllBody();
            this.isClosedStream = true;
            this.isLoadedBody = true;
        }
        this.charset = charset;
    }

    /**
     * Get host
     * @return
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get Mime type
     * @return
     */
    public MIME_TYPE getContentType() {
        return this.contentType;
    }
    
    /**
     * Get content length
     * @return
     */
    public long getContentLength() {
        return this.contentLength;
    }

    /**
     * whether body closed
     * @return
     */
    public boolean isClosedStream() {
        return this.isClosedStream;
    }

    /**
     * whether body loaded
     * @return
     */
    public boolean isLoadedBody() {
        return this.isLoadedBody;
    }

    /**
     * Get contents
     * @return
     * @throws IOException
     */
    public byte[] getAllBody() throws IOException {
        this.body = StreamUtils.readAll(this.requestStream);
        this.requestStream.close();
        return this.body;
    }

    /**
     * Get body charset
     * @return
     */
    public Charset getBodyCharset() {
        return this.charset;
    }

    /**
     * Save binary data
     * @param targetPath
     * @throws IOException
     */
    public abstract void save(Path targetPath) throws IOException;
}
