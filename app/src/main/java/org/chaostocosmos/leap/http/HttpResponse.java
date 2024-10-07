package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.file.FileUtils;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.exception.LeapException;

/**
 * Http response descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpResponse<R> implements Http {    

    /**
     * Host
     */
    private final Host<?> host;

    /**
     * OutputStream
     */
    private final OutputStream outputStream;

    /**
     * Response code
     */
    private int responseCode; 

    /**
     * Response body
     */
    private R responseBody;    

    /**
     * Body length
     */
    private long contentLength;  

    /**
     * Response header map
     */
    private final Map<String, List<String>> headers;

    /**
     * Whether be sent to client
     */
    private boolean isSent = false;

    /**
     * Construct with parameters
     * @param host
     * @param outputStream
     * @param statusCode
     * @param responseBody
     * @param headers
     */ 
    public HttpResponse(Host<?> host, 
                        OutputStream outputStream, 
                        int statusCode, 
                        R responseBody, 
                        Map<String, List<String>> headers) {
        this.host = host;
        this.outputStream = outputStream;
        this.responseCode = statusCode;
        this.responseBody = responseBody;
        this.headers = headers;
        this.contentLength = getContentLength();
    }

    /**
     * Get response body content length
     * @param responseBody
     * @return
     */
    public long getContentLength(R responseBody) {
        if(responseBody instanceof CharSequence) {
            return ((CharSequence) responseBody).length();
        } else if(responseBody instanceof File) {
            return ((File) responseBody).length();
        } else if(responseBody instanceof Path) {
            return ((Path) responseBody).toFile().length();
        } else if(responseBody instanceof byte[]) {
            return ((byte[]) responseBody).length;
        } else {
            return 0;
        }
    }

    /**
     * Get host name
     * @return
     */
    public final Host<?> getHost() {
        return this.host;
    }

    /**
     * Get response code
     * @return
     */
    public int getResponseCode() {
        return this.responseCode;
    }

    /**
     * Set response code
     * @param responseCode
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Get body object
     * @return
     */
    public R getBody() {
        return this.responseBody;
    }

    /**
     * Set response body
     * @param responseBody
     */
    public void setBody(R responseBody) {
        this.responseBody = responseBody;
        this.contentLength = getContentLength();
    }

    /**
     * Get response header Map
     * @return
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Add header key/value
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        addHeader(key, List.of(value));
    }

    /**
     * Add header key/ value splited token
     * @param key
     * @param value
     */
    public void addHeader(String key, List<String> value) {
        this.headers.putIfAbsent(key, value);
    }

    /**
     * Remove all header
     */
    public void removeAllHeader() {
        this.headers.clear();
    }

    /**
     * Set header name / value
     * @param name
     * @param value
     */
    public void setHeader(String name, String value) {
        if(this.headers.containsKey(name)) {
            this.headers.get(name).add(value);
        } else {
            throw new IllegalStateException("Specified name of key must be exist in response headers: "+name);
        }
    }

    /**
     * Remove header with name
     * @param name
     */
    public void removeHeader(String name) {
        this.headers.remove(name);
    }

    /**
     * Set content length
     * @param contentLength
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Get content length
     * @return
     */
    public long getContentLength() {
        return this.contentLength;
    }

    /**
     * Add set cookie with attribute key / value
     * @param attrKey
     * @param attrValue
     */
    public void addSetCookie(String attrKey, String attrValue) {
        List<String> cookieList = this.headers.get("Set-Cookie");
        if(cookieList == null) {
            cookieList = new ArrayList<>();
            this.headers.put("Set-Cookie", cookieList);
        }        
        this.headers.get("Set-Cookie").add(attrKey+"="+attrValue);        
    }

    /**
     * Get cookie by attribute key
     * @param attrKey
     * @return
     */
    public String getSetCookie(String attrKey) {
        List<String> cookieList = this.headers.get("Set-Cookie");
        return cookieList.stream().filter(c -> c.toString().startsWith(attrKey)).map(c -> c.toString().substring(c.toString().indexOf("=")+1)).findFirst().orElse(null);
    }

    /**
     * Get whether response is sent to client
     * @return
     */
    public boolean isSent() {
        return this.isSent;
    }

    /**
     * Send response to client by requested host, status code, reponse headers, body object
     * @param host
     * @param responseCode
     * @param headers
     * @param responseBody
     */
    public void sendResponse() {
        try {
            if(isSent) {
                host.getLogger().warn("RESPONSE IS ALREADY SENT TO CLIENT: "+toString());
                return;
            }
            Charset charset = this.host.charset();
            PROTOCOL protocol = this.host.getProtocol();
            String resMsg = responseCode < 900 ? HTTP.valueOf("RES"+responseCode).status() : responseCode >= 900 && responseCode < 1000 ? HTTP.valueOf("LEAP"+responseCode).status() : "Error code not supported: "+responseCode;
            String res = protocol.name()+"/"+Constants.HTTP_VERSION+" "+responseCode+" "+resMsg+"\r\n"; 
            if(responseBody == null) {
                this.host.getLogger().warn("Response body is Null: "+responseCode);
                return ;
            }    
            byte[] body = null;
            if(responseBody instanceof byte[]) {
                body = ((byte[])responseBody);                
            } else if(responseBody instanceof CharSequence) {
                body = ((CharSequence)responseBody).toString().getBytes(charset);
            } else if(responseBody instanceof File || responseBody instanceof Path) {
                body = FileUtils.readFile(responseBody instanceof File ? (File)responseBody : ((Path)responseBody).toFile(), host.<Integer> getValue("file.read-buffer-size"));
            } else {
                throw new LeapException(HTTP.RES501, new NotSupportedException("Not support response body type: " + responseBody.getClass().getName()));
            }            
            long contentLength = body.length;
            if(contentLength > host.<Integer> getValue("network.reponse-limit-byte-size")) {
                throw new LeapException(HTTP.RES501, new NotSupportedException("Respose body size is too big: "+contentLength+" Limit: " + SIZE.GB.get(host.<Integer> getValue("network.response-limit-byte-size"), 2)));
            }
            List<String> values = new ArrayList<>();
            values.add(String.valueOf(contentLength));
            headers.put("Content-Length", values);

            //LoggerFactory.getLogger(response.getRequestedHost()).debug(response.toString());
            StringBuffer resStr = new StringBuffer();
            resStr.append("////////////////////////////// [RESPONSE] : "+res.trim()+" - "+host.getInetAddress().getHostName()+System.lineSeparator());
            resStr.append("RES CODE: "+responseCode+System.lineSeparator());
            this.outputStream.write(res.getBytes());
            for(Map.Entry<String, List<String>> e : headers.entrySet()) {
                String hv = e.getValue().stream().map(v -> v.toString()).collect(Collectors.joining("; "));
                this.outputStream.write((e.getKey()+": "+hv+"\r\n").getBytes());
                resStr.append(e.getKey()+": "+hv+System.lineSeparator());
            }
            this.host.getLogger().debug(resStr.substring(0, resStr.length()-1));
            this.outputStream.write("\r\n".getBytes()); 
            this.outputStream.flush(); 
            if(responseBody instanceof byte[]) {
                this.outputStream.write((byte[]) responseBody);
            } else { 
                if(responseBody instanceof String) {                                       
                    this.outputStream.write(responseBody.toString().getBytes());
                } else if(responseBody instanceof File) {
                    writeToStream((File)responseBody, this.outputStream);
                } else if(responseBody instanceof Path) {
                    writeToStream(((Path)responseBody).toFile(), this.outputStream);
                } else {
                    throw new IllegalArgumentException("Not supported response body type: "+responseBody.getClass().getName());
                }
            }
            this.outputStream.flush();
            this.isSent = true;
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new LeapException(HTTP.RES500, e);
        } 
    }

    /**
     * Write resource to OutputStream for client
     * @param resource
     * @param out
     * @param bufferSize
     * @throws IOException
     */
    private void writeToStream(File resource, OutputStream out) throws IOException {
        byte[] buffer = new byte[this.host.<Integer> getValue("file.write-buffer-size")];
        FileInputStream in = new FileInputStream(resource);
        int len;
        while((len=in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
    }

    @Override
    public String toString() {
        return "HttpResponse [host=" + host + ", outputStream=" + outputStream + ", responseCode=" + responseCode
                + ", responseBody=" + responseBody + ", contentLength=" + contentLength + ", headers=" + headers
                + ", isSent=" + isSent + "]";
    }       
}
