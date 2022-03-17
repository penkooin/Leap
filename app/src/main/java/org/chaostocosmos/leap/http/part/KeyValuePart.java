package org.chaostocosmos.leap.http.part;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.resources.Context;

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
            keyVal = new String(super.body, super.charset);
        } else if(!super.isClosedStream) {
            super.body = StreamUtils.readAll(super.requestStream);
            keyVal = new String(super.body, super.charset);
            super.isClosedStream = true;
        } else {
            throw new IOException(Context.getErrorMsg(48, super.isLoadedBody, super.isClosedStream));
        }
        return Arrays.asList(keyVal.split("&")).stream().map(t -> t.split("=", -1)).collect(Collectors.toMap(k -> k[0], v -> v[1]));
    }

    @Override
    public void save(Path targetPath) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetPath.toFile()), super.charset));
        for(String key : this.keyValueMap.keySet()) {
            out.write(key+"="+this.keyValueMap.get(key));
        }
        out.close();
        super.logger.debug(super.contentType.name()+" saved: "+targetPath.toString()+"  Size: "+targetPath.toFile().length());
    }
}
