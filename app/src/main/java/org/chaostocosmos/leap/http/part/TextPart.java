package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.commons.FileUtils;
import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.resources.Context;

/**
 * TextPart
 * 
 * @authon 9ins
 */
public class TextPart extends BodyPart {
    
    /**
     * Constructor
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param sustainBody
     * @param charset
     * @throws IOException
     */
    public TextPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream, boolean sustainBody, Charset charset) throws IOException {
        super(host, contentType, contentLength, requestStream, sustainBody, charset);
    }

    @Override
    public void save(Path targetPath) throws IOException {
        if(super.isLoadedBody) {
            FileUtils.saveText(super.body, targetPath, super.charset, Context.getFileBufferSize());
        } else if(!super.isClosedStream) {
            StreamUtils.saveText(super.host, super.requestStream, super.contentLength, targetPath, super.charset);
            super.isClosedStream = true;
        } else {
            throw new IOException(Context.getErrorMsg(48, super.isLoadedBody, super.isClosedStream));
        }
        super.logger.debug(super.contentType.name()+" saved: "+targetPath.toString()+"  Size: "+targetPath.toFile().length());
    }
}
