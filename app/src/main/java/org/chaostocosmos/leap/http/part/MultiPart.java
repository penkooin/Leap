package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequestStream;

/**
 * Multi part descriptor
 * 
 * @author 9ins
 */
public class MultiPart extends AbstractPart<Map<String, byte[]>> {

    /**
     * Multipart saved files
     */
    List<Path> filePaths = new ArrayList<>();

    /**
     * Multipart boundary
     */
    String boundary;

    /**
     * Constructor of Multipart
     * @param hostId
     * @param contentType
     * @param boundary
     * @param contentLength
     * @param requestStream
     * @param charset
     */
    public MultiPart(Host<?> host, 
                     MIME contentType, 
                     String boundary, 
                     long contentLength, 
                     HttpRequestStream requestStream, 
                     Charset charset) {
        super(host, contentType, contentLength, requestStream, charset);
        this.boundary = boundary;
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
    public Map<String, byte[]> getBody() throws IOException {
        if(super.body == null) {
            super.body = super.requestStream.readPartData(this.boundary, super.host.charset(), this.host.<Integer> getValue("network.receive-buffer-size"));
        }
        return super.body;
    }

    @Override
    public void saveTo(Path targetDir, boolean isDirect) throws Exception {
        System.out.println(contentType+" "+MIME.MULTIPART_FORM_DATA);
        if(contentType != MIME.MULTIPART_FORM_DATA && contentType != MIME.MULTIPART_BYTERANGES) {
            throw new LeapException(HTTP.RES406, "Can not save content. Not supported on Multi Part Operation.");
        }        
        if(!targetDir.toFile().isDirectory()) {
            throw new IOException("Multi part saving must be directory path: "+targetDir.toString());
        }
        if(!isDirect) {            
            this.getBody().entrySet().stream().forEach(e -> {
                try {
                    Path path = targetDir.resolve(e.getKey());
                    if(!Files.exists(path)) {
                        Files.createFile(path);
                    }
                    Files.write(path, e.getValue(), StandardOpenOption.TRUNCATE_EXISTING);
                    filePaths.add(path);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            //this.filePaths = super.requestStream.saveMultiPart(targetDir, this.boundary, Charset.forName(super.host.charset()));    
        }
        super.logger.debug("[MULTI-PART] "+super.contentType.name()+" saved: "+targetDir.normalize().toString()+"  Size: "+filePaths.stream().map(p -> p.toFile()).map(f -> f.getName()+": "+f.length()).collect(Collectors.joining(", ")));
    }    

    @Override 
    public String toString() {
        return super.toString() + "{" +
            " filePaths='" + filePaths + "'" +
            ", boundary='" + boundary + "'" +
            "}";
    }
}
