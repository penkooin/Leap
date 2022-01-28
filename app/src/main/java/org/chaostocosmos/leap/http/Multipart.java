package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.StreamUtils;

import ch.qos.logback.classic.Logger;

/**
 * Multi part descriptor
 * 
 * @author 9ins
 */
public class MultiPart extends BodyPart {

    Logger logger;
    String host;
    List<Path> filePaths;
    MIME_TYPE contentType;
    String boundary;
    long contentLength;
    InputStream requestStream;

    /**
     * Constructor of Multipart
     * @param host
     * @param contentType
     * @param boundary
     * @param contentLength
     * @param requestStream
     */
    public MultiPart(String host, MIME_TYPE contentType, String boundary, long contentLength, InputStream requestStream) {
        super(host, contentType, contentLength, requestStream);
        this.host = host;
        this.contentType = contentType;
        this.boundary = boundary;
        this.contentLength = contentLength;
        this.requestStream = requestStream;
        this.filePaths = new ArrayList<>();
        this.logger = LoggerFactory.getLogger(this.host);
    }

    @Override
    public MIME_TYPE getContentType() {
        return this.contentType;
    }

    @Override
    public byte[] getAllContents() {
        throw new UnsupportedOperationException("This method cannot support at Multipart object!!!");
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public void save(Path targetPath) throws IOException {
        this.filePaths = StreamUtils.saveMultiPart(this.host, this.requestStream, targetPath, Context.getFileBufferSize(), this.boundary);
    }

    /**
     * Get contents Map
     * @return
     * @throws IOException
     */
    public Map<String, byte[]> getMultiPartContents() throws IOException {
        return StreamUtils.getMultiPartContents(this.host, this.requestStream, this.boundary);
    }

    /**
     * Get saved file Paths
     * @return
     */
    public List<Path> getFilePaths() {
        return this.filePaths;
    }

    /**
     * Get boundary String
     * @return
     */
    public String getBoundary() {
        return this.boundary;
    }

    /**
     * Get InputStream of request
     * @return
     */
    public InputStream getInputStream() {
        return this.requestStream;
    }
}
