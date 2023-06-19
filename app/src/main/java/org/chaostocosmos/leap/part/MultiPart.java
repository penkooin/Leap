package org.chaostocosmos.leap.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.common.StreamUtils;

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
     * Previously load body to MultiPart memory
     */
    boolean preLoadBody;
    /**
     * Constructor of Multipart
     * @param hostId
     * @param contentType
     * @param boundary
     * @param contentLength
     * @param requestStream
     * @param preLoadBody
     * @param charset
     * @throws IOException
     */
    public MultiPart(String hostId, MIME contentType, String boundary, long contentLength, InputStream requestStream, boolean preLoadBody, Charset charset) throws IOException {
        super(hostId, contentType, contentLength, requestStream, false, charset);
        this.filePaths = new ArrayList<>();
        this.boundary = boundary;
        this.preLoadBody = preLoadBody;
        if(this.preLoadBody) {
            super.body = getBody();
        }
    }

    @Override
    public Map<String, byte[]> getBody() throws IOException {
        if(super.body == null) {
            return StreamUtils.getMultiPartContents(this.hostId, this.requestStream, this.boundary, super.charset);
        }
        return super.body;
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
    public void save(final Path targetPath) throws IOException {
        if(contentType != MIME.MULTIPART_FORM_DATA || contentType != MIME.MULTIPART_BYTERANGES) {
            throw new LeapException(HTTP.RES406, "Can not save content. Not supported on Multi Part Operation.");
        }        
        if(targetPath.toFile().isDirectory()) {
            throw new IOException("Multi part saving must be provided directory Path: "+targetPath.toString());
        }
        if(super.isLoadedBody) {
            super.body.entrySet().stream().forEach(e -> {
                try {
                    Path path = targetPath.resolve(e.getKey());
                    Files.write(path, e.getValue(), StandardOpenOption.TRUNCATE_EXISTING);
                    filePaths.add(path);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            this.filePaths = StreamUtils.saveMultiPart(this.hostId, super.requestStream, targetPath.normalize(), Context.server().getFileBufferSize(), this.boundary, super.charset);    
        }
        super.logger.debug("[MULTI-PART] "+super.contentType.name()+" saved: "+targetPath.normalize().toString()+"  Size: "+filePaths.stream().map(p -> p.toFile()).map(f -> f.getName()+": "+f.length()).collect(Collectors.joining(", ")));
    }    

    @Override
    public String toString() {
        return super.toString() + "{" +
            " filePaths='" + filePaths + "'" +
            ", boundary='" + boundary + "'" +
            "}";
    }
}
