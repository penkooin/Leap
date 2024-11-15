package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.chaostocosmos.leap.enums.MIME;

/**
 * Request part model
 * 
 * @author 9ins
 */
public interface Part <T> {    

    /**
     * Get content type
     * @return
     */
    public MIME getContentType();

    /**
     * Get content length
     * @return
     */
    public long getContentLength();

    /**
     * Get charset of Body content
     * @return
     */
    public Charset getCharset();

    /**
     * Get body data 
     * @return
     */
    public T getBody() throws IOException;

    /**
     * Whether existing body content
     * @return
     */
    public boolean isBody();

    /**
     * Save body to parameted directory
     * @param targetPath
     * @param isDirect
     * @throws Exception
     */
    public void saveTo(Path targetDir, boolean isDirect) throws Exception;
}
