package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.chaostocosmos.leap.http.enums.MIME_TYPE;

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
     * @param loadBody
     * @param charset
     * @throws IOException
     */
    public BinaryPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream, boolean loadBody, Charset charset) throws IOException {
        super(host, contentType, contentLength, requestStream, loadBody, charset);
    }
}
