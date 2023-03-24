package org.chaostocosmos.leap.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.enums.MIME;

/**
 * Request part model
 */
public interface Part {    

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
     * Whether body content read when request be connected.
     * @return
     */
    public boolean isContentRead();

    /**
     * Get charset of Body content
     * @return
     */
    public Charset getCharset();

    /**
     * Get body data Map<String,>
     * @return
     */
    public Map<String, byte[]> getBody() throws IOException;

    /**
     * Save binary data
     * @param targetPath
     * @throws IOException
     */
    public abstract void save(Path targetPath) throws IOException;
}
