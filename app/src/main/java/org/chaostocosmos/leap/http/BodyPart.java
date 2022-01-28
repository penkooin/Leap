package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.commons.StreamUtils;

/**
 * BodyPart
 * 
 * @author 9ins
 */
abstract class BodyPart {
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
     * Constructor
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     */
    public BodyPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream) {
        this.host = host;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.requestStream = requestStream;
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
     * Get contents
     * @return
     * @throws IOException
     */
    public byte[] getAllContents() throws IOException {
        return StreamUtils.readAll(this.requestStream);
    }

    /**
     * Save contents
     * @param targetPath
     */
    public abstract void save(Path targetPath) throws IOException;
}
