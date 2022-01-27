package org.chaostocosmos.leap.http;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * BinaryPart
 * 
 * @author 9ins
 */
public class BinaryPart implements BodyPart {

    String host;
    MIME_TYPE contentType;
    long contentLength;
    InputStream requestStream;

    public BinaryPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream) {
        this.host = host;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.requestStream = requestStream;
    }

    @Override
    public MIME_TYPE getContentType() {
        return null;
    }

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public byte[] getContents() {
        return null;
    }

    @Override
    public void save(Path targetPath) throws WASException {
    }
}
