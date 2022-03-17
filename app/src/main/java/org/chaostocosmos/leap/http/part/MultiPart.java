package org.chaostocosmos.leap.http.part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.resources.Context;
import org.chaostocosmos.leap.http.resources.HostsManager;

/**
 * Multi part descriptor
 * 
 * @author 9ins
 */
public class MultiPart extends BodyPart {

    /**
     * Multipart saved files
     */
    List<Path> filePaths;
    /**
     * Multipart boundary
     */
    String boundary;

    /**
     * Constructor of Multipart
     * @param host
     * @param contentType
     * @param boundary
     * @param contentLength
     * @param requestStream
     * @param preLoadBody
     * @param charset
     * @throws IOException
     */
    public MultiPart(String host, MIME_TYPE contentType, String boundary, long contentLength, InputStream requestStream, boolean preLoadBody, Charset charset) throws IOException {
        super(host, contentType, contentLength, requestStream, preLoadBody, charset);
        this.filePaths = new ArrayList<>();
        this.boundary = boundary;
    }

    /**
     * Get contents Map
     * @return
     * @throws IOException
     */
    public Map<String, byte[]> getMultiPartContents() throws IOException {
        return StreamUtils.getMultiPartContents(this.host, this.requestStream, this.boundary, super.charset);
    }

    /**
     * Get saved file Paths
     * @return
     */
    public List<Path> getFilePaths() {
        return this.filePaths;
    }

    /**
     * Get boundary String
     * @return
     */
    public String getBoundary() {
        return this.boundary;
    }

    /**
     * Delete all Multi-Part files
     */
    public void deleteFiles() {
        this.filePaths.stream().forEach(p -> p.toFile().delete());
    }

    @Override
    public void save(Path targetPath) throws IOException {
        if(super.isLoadedBody) {
            this.filePaths = StreamUtils.saveMultiPart(this.host, new ByteArrayInputStream(super.body), targetPath.normalize(), Context.getFileBufferSize(), this.boundary, super.charset);    
        } else if(!super.isClosedStream) {
            this.filePaths = StreamUtils.saveMultiPart(this.host, super.requestStream, targetPath.normalize(), Context.getFileBufferSize(), this.boundary, super.charset);    
            this.isClosedStream = true;
        } else {
            throw new IOException(Context.getErrorMsg(48, super.isLoadedBody, super.isClosedStream));
        }
        super.logger.debug(super.contentType.name()+" saved: "+targetPath.normalize().toString()+"  Size: "+filePaths.stream().map(p -> p.toFile()).map(f -> f.getName()+": "+f.length()).collect(Collectors.joining(", ")));
    }    
}
