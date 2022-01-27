package org.chaostocosmos.leap.http;

import java.nio.file.Path;

/**
 * Part
 * 
 * @author 9ins
 */
public interface BodyPart {

    /**
     * Get Mime type
     * @return
     */
    public MIME_TYPE getContentType();
    
    /**
     * Get content length
     * @return
     */
    public long getContentLength();

    /**
     * Get contents
     * @return
     */
    public byte[] getContents();

    /**
     * Save contents
     * @param targetPath
     */
    public void save(Path targetPath) throws WASException;
}
