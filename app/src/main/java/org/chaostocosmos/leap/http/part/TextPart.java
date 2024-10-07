package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HttpRequestStream;

/**
 * TextPart
 * 
 * @authon 9ins
 */
public class TextPart extends AbstractPart<String> {    

    /**
     * Constructor
     * 
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param charset
     * @throws Exception 
     */
    public TextPart(Host<?> host, 
                    MIME contentType, 
                    long contentLength, 
                    HttpRequestStream requestStream, 
                    Charset charset) throws Exception {
        super(host, contentType, contentLength, requestStream, charset);
    }

    @Override
    public String getBody() throws IOException {
        if(this.body == null) {
            this.body = new String(super.requestStream.readStream((int) getContentLength()));
        }
        return body;
    }

    @Override
    public void saveTo(Path targetPath, boolean isDirect) throws IOException {
        if(!isDirect) {
            super.requestStream.saveString(getBody().toString(), targetPath);
        } else {
            super.requestStream.saveTo((int) getContentLength(), targetPath);
        }
        super.logger.debug("[TEXT-PART] "+super.contentType.name()+" saved: "+targetPath.toString()+"  Size: "+targetPath.toFile().length());
    }
}
