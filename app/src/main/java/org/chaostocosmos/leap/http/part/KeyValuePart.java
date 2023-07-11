package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HttpRequestStream;

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
    public KeyValuePart(Host<?> host, MIME contentType, long contentLength, HttpRequestStream requestStream, boolean preLoadBody, Charset charset) throws IOException {
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
            byte[] body = super.requestStream.readLength((int)super.contentLength);
            keyVal = new String(body, super.charset);
        }
        return Arrays.asList(keyVal.split("&")).stream().map(t -> t.split("=", -1)).collect(Collectors.toMap(k -> k[0], v -> v[1]));
    }
}
