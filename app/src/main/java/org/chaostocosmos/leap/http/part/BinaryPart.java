package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.commons.FileUtils;
import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.context.Context;
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

    @Override
    public void save(Path targetPath) throws IOException {
        if(super.isLoadedBody) {
            FileUtils.saveBinary(super.body, targetPath, Context.getServer().getFileBufferSize());
        } else if(!super.isClosedStream) {
            StreamUtils.saveBinary(super.host, super.requestStream, super.contentLength, targetPath, Context.getServer().getFileBufferSize());
            this.isClosedStream = true;
        } else {
            throw new IOException(Context.getMessages().getErrorMsg(48, super.isLoadedBody, super.isClosedStream));
        }
        super.logger.debug(super.contentType.name()+" saved: "+targetPath.toString()+"  Size: "+targetPath.toFile().length());
    }
}
