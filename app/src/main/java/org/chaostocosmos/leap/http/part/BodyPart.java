package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.common.StreamUtils;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

import ch.qos.logback.classic.Logger;

/**
 * BodyPart
 * 
 * @author 9ins
 */
public class BodyPart implements Part {
    /**
     * Logger
     */
    Logger logger;
    /**
     * Host
     */
    String host;
    /**
     * Content type
     */
    MIME_TYPE contentType;
    /**
     * Content length
     */
    long contentLength;    
    /**
     * Request stream
     */
    InputStream requestStream;
    /**
     * Whether body loaded
     */
    boolean isLoadedBody = false; 
    /**
     * body data
     */
    Map<String, byte[]> body;
    /**
     * body charset
     */
    Charset charset;
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
    public BodyPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream, boolean loadBody, Charset charset) throws IOException {
        this.logger = LoggerFactory.getLogger(host);
        this.host = host;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.requestStream = requestStream;        
        this.charset = charset;
        this.isLoadedBody = loadBody;
        if(this.isLoadedBody) {
            this.body = readBody();
        }
    }

    /**
     * Get host
     * @return
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get contents
     * @return
     * @throws IOException
     */
    private Map<String, byte[]> readBody() throws IOException {
        if(contentType == MIME_TYPE.MULTIPART_FORM_DATA || contentType == MIME_TYPE.MULTIPART_BYTERANGES) {
            throw new HTTPException(RES_CODE.RES406, "Method not supported on MultiPart operation: readBody()");
        }
        Map<String, byte[]> map = new HashMap<>();
        byte[] data = StreamUtils.readLength(this.requestStream, (int)this.contentLength);        
        map.put("BODY", data);
        return map;
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }

    @Override
    public MIME_TYPE getContentType() {
        return this.contentType;
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public boolean isContentRead() {
        return this.isLoadedBody;
    }

    @Override
    public Map<String, byte[]> getBody() throws IOException {
        if(this.isLoadedBody) {
            return this.body;
        }
        readBody();
        return this.body;
    }

    @Override
    public void save(Path targetPath) throws IOException {        
        if(contentType == MIME_TYPE.MULTIPART_FORM_DATA || contentType == MIME_TYPE.MULTIPART_BYTERANGES) {
            throw new HTTPException(RES_CODE.RES406, "Can not save content. Not supported on Multi Part Operation.");
        }        
        if(this.isLoadedBody) {
            StreamUtils.saveBinary(this.host, this.body.get("BODY"), targetPath, Context.getServer().getFileBufferSize());
        } else {
            StreamUtils.saveBinary(this.host, this.requestStream, getContentLength(), targetPath, Context.getServer().getFileBufferSize());
        }        
        this.logger.debug("[BODY-PART] "+contentType.name()+" saved: "+targetPath.normalize().toString()+"  Path: "+targetPath.toString());
    }    

    @Override
    public String toString() {
        return "{" +
            " logger='" + logger + "'" +
            ", host='" + host + "'" +
            ", contentType='" + contentType + "'" +
            ", contentLength='" + contentLength + "'" +
            ", requestStream='" + requestStream + "'" +
            ", isLoadedBody='" + isLoadedBody + "'" +
            ", body='" + body + "'" +
            ", charset='" + charset + "'" +
            "}";
    }
}
