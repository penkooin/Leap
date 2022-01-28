package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.commons.StreamUtils;

/**
 * BinaryPart
 * 
 * @author 9ins
 */
public class BinaryPart extends BodyPart {

    /**
     * Constructor
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     */
    public BinaryPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream) {
        super(host, contentType, contentLength, requestStream);
    }

    @Override
    public void save(Path targetPath) throws IOException {
        StreamUtils.saveBinary(super.host, super.requestStream, super.contentLength, targetPath, Context.getFileBufferSize());
    }
}
