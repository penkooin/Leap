package org.chaostocosmos.leap.http.part;

import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HttpRequestStream;

/**
 * BinaryPart
 * 
 * @author 9ins
 */
public class BinaryPart extends AbstractPart <byte[]> {

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
    public BinaryPart(Host<?> host, 
                      MIME contentType, 
                      long contentLength, 
                      HttpRequestStream requestStream, 
                      Charset charset) throws Exception {
        super(host, contentType, contentLength, requestStream, charset);
    }

    @Override
    public byte[] getBody() throws Exception {
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
        this.logger.debug("[BODY-PART] "+contentType.name()+" saved: "+targetDir.normalize().toString()+"  Path: "+targetDir.toString());
    }
}
