package org.chaostocosmos.leap.http.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FormData
 * 
 * @author 9ins
 */
public class FormData <T> {
    /**
     * MIME 
     */
    MIME contentType;
    /**
     * Content
     */
    T content;
    /**
     * content length
     */    
    int contentLength;
    /**
     * content bytes
     */
    byte[] contentBytes;
    /**
     * Creates with content type, content
     * @param contentType
     * @param content
     * @throws IOException
     */
    public FormData(MIME contentType, T content) throws IOException {
        this.contentType = contentType;
        this.content = content;
        if(this.content instanceof byte[]) {
            this.contentBytes = (byte[])this.content;            
        } else if(this.content instanceof String) {
            this.contentBytes = ((String)this.content).getBytes();
        } else if(this.content instanceof Path) {
            this.contentBytes = Files.readAllBytes((Path)this.content);
        } else if(this.content instanceof File) {
            this.contentBytes = Files.readAllBytes(((File)this.content).toPath());
        } else {
            throw new IllegalArgumentException("Not supported content type: "+this.content.getClass().getName());
        }
    }    
    /**
     * Get content type
     * @return
     */
    public MIME getContentType() {
        return contentType;
    }
    /**
     * Get content
     */
    public T getContent() {
        return content;
    }
    /**
     * Get content length
     * @return
     */
    public int getContentLength() {
        return contentLength;
    }
    /**
     * Get content bytes
     * @return
     */
    public byte[] getContentBytes() {
        return contentBytes;
    }
}
