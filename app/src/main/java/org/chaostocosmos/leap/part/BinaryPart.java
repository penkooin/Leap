package org.chaostocosmos.leap.part;

import java.io.IOException;
import java.nio.charset.Charset;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HttpRequestStream;

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
    public BinaryPart(Host<?> host, MIME contentType, long contentLength, HttpRequestStream requestStream, boolean loadBody, Charset charset) throws IOException {
        super(host, contentType, contentLength, requestStream, loadBody, charset);
    }
}
