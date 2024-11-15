package org.chaostocosmos.leap.http.part;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
public class KeyValuePart extends AbstractPart <Map<String, String>> {

    /**
     * Constructor 
     * @param host
     * @param contentType
     * @param contentLength
     * @param requestStream
     * @param charset
     */
    public KeyValuePart(Host<?> host, 
                        MIME contentType, 
                        long contentLength, 
                        HttpRequestStream requestStream, 
                        Charset charset) {
        super(host, contentType, contentLength, requestStream, charset);
    }

    @Override
    public Map<String, String> getBody() throws IOException {
        if(this.body == null) {
            byte[] body = super.requestStream.readStream((int)super.contentLength);
            String keyVal = new String(body, super.charset);
            this.body = Arrays.asList(keyVal.split("&")).stream().map(t -> t.split("=", -1)).collect(Collectors.toMap(k -> k[0], v -> v[1]));
        }
        return this.body;
    }

    @Override
    public void saveTo(Path targetDir, boolean isDirect) throws Exception {
        String keyVal = ((Map<String, String>)this.body).entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining(System.lineSeparator()));
        Files.writeString(targetDir, keyVal);
        this.logger.debug("[BODY-PART] "+contentType.name()+" saved: "+targetDir.normalize().toString()+"  Path: "+targetDir.toString());
    }
}
