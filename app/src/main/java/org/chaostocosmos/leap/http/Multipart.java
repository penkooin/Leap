package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.SaveUtils;

import ch.qos.logback.classic.Logger;

/**
 * Multi part descriptor
 * 
 * @author 9ins
 */
public class Multipart implements BodyPart {

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
    public Multipart(String host, MIME_TYPE contentType, String boundary, long contentLength, InputStream requestStream) {
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
    public byte[] getContents() {
        throw new UnsupportedOperationException("This method cannot support at Multipart object!!!");
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public void save(Path targetPath) throws WASException {
        try {
            SaveUtils.saveMultipart(this.host, this.requestStream, targetPath, Context.getFileBufferSize(), this.boundary);
        } catch(IOException e) {
            throw new WASException(MSG_TYPE.ERROR, 43, targetPath.toString());
        }
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
