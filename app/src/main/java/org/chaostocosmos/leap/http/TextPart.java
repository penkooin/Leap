package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * TextPart
 * 
 * @authon 9ins
 */
public class TextPart extends BodyPart {
    
    Map<String, String> formParamMap;

    /**
     * Constructor
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     */
    public TextPart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream) {
        super(host, contentType, contentLength, requestStream);
    }

    @Override
    public void save(Path targetPath) throws IOException {
        // try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(super.requestStream, Context.charset()))) {
            
        // }
    }
}
