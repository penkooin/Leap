package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequestStream;

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
    Host<?> host;

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
    HttpRequestStream requestStream;

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
    public BodyPart(Host<?> host, MIME contentType, long contentLength, HttpRequestStream requestStream, boolean loadBody, Charset charset) throws IOException {
        this.logger = host.getLogger();
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
     * Get host ID
     * @return
     */
    public String getHostId() {
        return this.host.getId();
    }

    /**
     * Get Host instance
     * @return
     */
    public Host<?> getHost() {
        return this.host;
    }

    /**
     * Get contents
     * @return
     * @throws IOException
     */
    private Map<String, byte[]> readBody() throws IOException {
        if(contentType == MIME.MULTIPART_FORM_DATA || contentType == MIME.MULTIPART_BYTERANGES) {
            throw new LeapException(HTTP.RES406, new Exception("Multipart form data or byteranges can not use this method"));
        }
        Map<String, byte[]> map = new HashMap<>();
        byte[] data = this.requestStream.readLength((int)this.contentLength);        
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
            this.requestStream.saveBinary(this.body.get("BODY"), targetPath);
        } else {
            this.requestStream.saveBinary(getContentLength(), targetPath);
        }        
        this.logger.debug("[BODY-PART] "+contentType.name()+" saved: "+targetPath.normalize().toString()+"  Path: "+targetPath.toString());
    }    

    @Override
    public String toString() {
        return "{" +
            " logger='" + logger + "'" +
            ", host='" + getHostId() + "'" +
            ", contentType='" + contentType + "'" +
            ", contentLength='" + contentLength + "'" +
            ", requestStream='" + requestStream + "'" +
            ", isLoadedBody='" + isLoadedBody + "'" +
            ", body='" + body + "'" +
            ", charset='" + charset + "'" +
            "}";
    }
}
