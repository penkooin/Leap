package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
     * @param preLoadBody
     * @param charset
     * @throws IOException
     */
    public KeyValuePart(String host, MIME_TYPE contentType, long contentLength, InputStream requestStream, boolean preLoadBody, Charset charset) throws IOException {
        super(host, contentType, contentLength, requestStream, preLoadBody, charset);
        this.keyValueMap = new HashMap<>();
    }

    /**
     * Get key value Map
     * @return
     * @throws IOException
     */
    public Map<String, String> getKeyValueMap() throws IOException {        
        String keyVal = null;
        if(super.isLoadedBody) {
            keyVal = new String(super.body.get("BODY"), super.charset);
        } else {
            byte[] body = StreamUtils.readLength(super.requestStream, (int)super.contentLength);
            keyVal = new String(body, super.charset);
        }
        return Arrays.asList(keyVal.split("&")).stream().map(t -> t.split("=", -1)).collect(Collectors.toMap(k -> k[0], v -> v[1]));
    }
}
