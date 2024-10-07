package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HttpRequestStream;

/**
 * SinglePart object
 * 
 * @author 9ins
 */
public class SinglePart extends AbstractPart<byte[]> {

    /**
     * Constructor
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param charset
     */
    public SinglePart(Host<?> host, 
                      MIME contentType, 
                      long contentLength, 
                      HttpRequestStream requestStream, 
                      Charset charset) {
        super(host, contentType, contentLength, requestStream, charset);
    }
    
    @Override
    public byte[] getBody() throws IOException {
        if(this.body == null) {
            this.body = this.requestStream.readStream((int) getContentLength());
        }
        return this.body;
    }

    @Override
    public void saveTo(Path targetDir, boolean isDirect) throws Exception {
        if(!isDirect) {
            this.requestStream.saveBinary(getBody(), targetDir);
        } else {
            this.requestStream.saveTo((int) getContentLength(), targetDir);
        }
    }
}
