package org.chaostocosmos.leap.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.common.StreamUtils;

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
    String hostId;
    /**
     * Content type
     */
    MIME contentType;
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
     * @param hostId
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param loadBody
     * @param charset
     * @throws IOException
     */
    public BodyPart(String hostId, MIME contentType, long contentLength, InputStream requestStream, boolean loadBody, Charset charset) throws IOException {
        this.logger = LoggerFactory.getLogger(hostId);
        this.hostId = hostId;
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
    public String getHostId() {
        return this.hostId;
    }

    /**
     * Get contents
     * @return
     * @throws IOException
     */
    private Map<String, byte[]> readBody() throws IOException {
        if(contentType == MIME.MULTIPART_FORM_DATA || contentType == MIME.MULTIPART_BYTERANGES) {
            throw new LeapException(HTTP.RES406, Context.get().messages().<String> error(27));
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
    public MIME getContentType() {
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
        if(contentType == MIME.MULTIPART_FORM_DATA || contentType == MIME.MULTIPART_BYTERANGES) {
            throw new LeapException(HTTP.RES406, "Can not save content. Not supported on Multi Part Operation.");
        }        
        if(this.isLoadedBody) {
            StreamUtils.saveBinary(this.hostId, this.body.get("BODY"), targetPath, Context.get().server().getFileBufferSize());
        } else {
            StreamUtils.saveBinary(this.hostId, this.requestStream, getContentLength(), targetPath, Context.get().server().getFileBufferSize());
        }        
        this.logger.debug("[BODY-PART] "+contentType.name()+" saved: "+targetPath.normalize().toString()+"  Path: "+targetPath.toString());
    }    

    @Override
    public String toString() {
        return "{" +
            " logger='" + logger + "'" +
            ", host='" + hostId + "'" +
            ", contentType='" + contentType + "'" +
            ", contentLength='" + contentLength + "'" +
            ", requestStream='" + requestStream + "'" +
            ", isLoadedBody='" + isLoadedBody + "'" +
            ", body='" + body + "'" +
            ", charset='" + charset + "'" +
            "}";
    }
}
