package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;

/**
 * KeyValuePart
 * 
 * @author 9ins
 */
public class KeyValuePart extends BodyPart {

    Map<String, String> keyValueMap;

    /**
     * Constructor 
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     */
    public KeyValuePart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream) {
        super(host, contentType, contentLength, requestStream);
        this.keyValueMap = new HashMap<>();
    }

    /**
     * Get key value Map
     * @return
     * @throws IOException
     */
    public Map<String, String> getKeyValueMap() throws IOException {
        String body = getBody();
        if(body.trim().equals("")) {
            return null;
        }
        return Arrays.asList(body.split("&")).stream().map(t -> t.split("=", -1)).collect(Collectors.toMap(k -> k[0], v -> v[1]));
    }

    /**
     * Get body String 
     * @return
     * @throws IOException
     */
    public String getBody() throws IOException {
        byte[] data = StreamUtils.readStream(super.requestStream, (int)super.contentLength);
        return new String(data, Context.charset());
    }

    @Override
    public void save(Path targetPath) throws IOException {
    }    
}
