package org.chaostocosmos.leap.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.common.FileUtils;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.common.StreamUtils;

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
    public TextPart(Host<?> host, MIME contentType, long contentLength, InputStream requestStream, boolean sustainBody, Charset charset) throws IOException {
        super(host, contentType, contentLength, requestStream, sustainBody, charset);
    }

    @Override
    public void save(Path targetPath) throws IOException {
        if(super.isLoadedBody) {
            FileUtils.saveText(new String(super.body.get("BODY"), super.charset), targetPath, Context.get().server().getFileBufferSize());
        } else {
            StreamUtils.saveStream(super.requestStream, super.contentLength, targetPath, super.charset);
        }
        super.logger.debug("[TEXT-PART] "+super.contentType.name()+" saved: "+targetPath.toString()+"  Size: "+targetPath.toFile().length());
    }
}
