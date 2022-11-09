package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MIME;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.HTTP;
import org.chaostocosmos.leap.http.resource.TemplateBuilder;

/**
 * HttpTransfer object
 * @author 9ins
 */
public class HttpTransfer {
    /**
     * Hosts, Information configured in config.yml
     */
    private Host<?> host;

    /**
     * Client Socket
     */
    private Socket socket;

    /**
     * InputStream
     */
    private InputStream inStream;

    /**
     * OutputStream
     */
    private OutputStream outStream;

    /**
     * Http request
     */
    private Request request;

    /**
     * Http response
     */
    private Response response;

    /**
     * Whether closed
     */
    boolean isClosed = false;

    /**
     * Construct with request host and client socket
     * @param hostId
     * @param socket
     * @throws IOException
     */
    public HttpTransfer(String hostId, Socket socket) throws IOException {
        this.host = Context.hosts().getHost(hostId);
        this.socket = socket;
        this.inStream = socket.getInputStream();
        this.outStream = socket.getOutputStream();
    }

    /**
     * Get requested host
     * @return
     */
    public Host<?> getHost() {
        return this.host;
    }

    /**
     * Get client InputStream
     * @return
     */
    public InputStream getClientInputStream() {
        return inStream;
    }

    /**
     * Get client OutputStream
     * @return
     */
    public OutputStream getClientOutputStream() {
        return outStream;
    }

    /**
     * Get HttpRequestDescriptor
     * @return
     * @throws Exception
     */
    public Request getRequest() throws Exception {
        if(this.request == null) {
            this.request = parseRequest();
        }
        return this.request;
    }

    /**
     * Get HttpResponseDescriptor
     * @return
     * @throws Exception
     * @throws ImageProcessingException
     */
    public Response getResponse() throws Exception {
        if(this.response == null) {
            String msg = TemplateBuilder.buildResponseHtml(this.host, MSG_TYPE.HTTP, 200, Context.messages().http(200));
            Map<String, List<String>> headers = addHeader(new HashMap<>(), "Content-Type", MIME.TEXT_HTML.mimeType());
            headers = addHeader(new HashMap<>(), "Content-Length", String.valueOf(msg.getBytes().length));
            headers = addHeader(new HashMap<>(), "Charset", this.host.<String> charset());
            this.response = HttpResponseBuilder.getBuilder().build(this.request)
                                                            .setStatusCode(200)
                                                            .setBody(msg)
                                                            .setHeaders(headers)
                                                            .get();
        }
        return this.response;
    }

    /**
     * Get HttpRequestDescriptor object
     * @return
     * @throws Exception
     */
    private Request parseRequest() throws Exception {
        Request request = HttpParser.buildRequestParser().parseRequest(this.socket.getInetAddress(), this.inStream);
        return request;
    }

    /**
     * Send response to client by HttpResponseDescriptor object
     * @param response
     * @throws IOException
     */
    public void sendResponse(Response response) throws Exception {
        sendResponse(response.getHostId(), response.getResponseCode(), response.getHeaders(), response.getBody());
    }

    /**
     * Send response to client by requested host, status code, reponse headers, body object
     * @param hostId
     * @param resCode
     * @param headers
     * @param body
     * @throws IOException
     */
    public void sendResponse(String hostId, int resCode, Map<String, List<String>> headers, Object body) throws IOException {
        Charset charset = Context.hosts().charset(hostId);
        String protocol = Context.hosts().getHost(hostId).<String> getProtocol();
        String resMsg = null;
        if(resCode >= 200 && resCode <= 600) {
            resMsg = HTTP.valueOf("RES"+resCode).status();
        } else {
            resMsg = Context.messages().error(resCode, hostId);
        }
        String res = protocol+"/"+Constants.HTTP_VERSION+" "+resCode+" "+resMsg+"\r\n"; 
        this.outStream.write(res.getBytes());
        if(body == null) {
            LoggerFactory.getLogger(hostId).warn("Response body is Null: "+resCode);
            return ;
        }    
        long contentLength = -1;
        if(body instanceof byte[]) {
            contentLength = ((byte[])body).length;
        } else if(body instanceof String) {
            contentLength = ((String)body).getBytes(charset).length;
        } else if(body instanceof Path) {
            contentLength = ((Path)body).toFile().length();
        } else if(body instanceof File) {
            contentLength = ((File)body).length();
        } else {
            throw new HTTPException(HTTP.RES501, Context.messages().<String>error(4, body.getClass().getName()));
        }
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(contentLength));
        headers.put("Content-Length", values);

        //LoggerFactory.getLogger(response.getRequestedHost()).debug(response.toString());
        StringBuffer resStr = new StringBuffer();
        resStr.append("============================== [RESPONSE] : "+res.trim()+" - "+this.socket.getRemoteSocketAddress().toString()+" =============================="+System.lineSeparator());
        resStr.append("RES CODE: "+resCode+System.lineSeparator());
        for(Map.Entry<String, List<String>> e : headers.entrySet()) {
            String hv = e.getValue().stream().map(v -> v.toString()).collect(Collectors.joining("; "));
            this.outStream.write((e.getKey()+": "+hv+"\r\n").getBytes());
            resStr.append(e.getKey()+": "+hv+System.lineSeparator());
        }
        LoggerFactory.getLogger(hostId).debug(resStr.substring(0, resStr.length()-1));
        this.outStream.write("\r\n".getBytes()); 
        this.outStream.flush(); 
        if(body instanceof byte[]) {
            this.outStream.write((byte[]) body);
        } else { 
            if(body instanceof String) {                                       
                this.outStream.write(body.toString().getBytes(charset));
            } else if(body instanceof File) {
                writeToStream((File)body, this.outStream, Context.server().getFileBufferSize());
            } else if(body instanceof Path) {
                writeToStream(((Path)body).toFile(), this.outStream, Context.server().getFileBufferSize());
            } else {
                throw new IllegalArgumentException("Not supported response body type: "+body.getClass().getName());
            }
        }
        this.outStream.flush();
    }

    /**
     * Write resource to OutputStream for client
     * @param resource
     * @param out
     * @param bufferSize
     * @throws IOException
     */
    private void writeToStream(File resource, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        FileInputStream in = new FileInputStream(resource);
        int len;
        while((len=in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
    }
    
    /**
     * Add key-value to header Map
     * @param headers
     * @param key
     * @param value
     * @return
     */
    public static Map<String, List<String>> addHeader(Map<String, List<String>> headers, String key, String value) {
        if(headers != null) {
            headers = new HashMap<>();
        }
        List<String> values = headers.get(key);
        if(values == null) {
            values = new ArrayList<>();
        }
        values.add(value);
        headers.put(key, values);
        return headers;
    }

    /**
     * Close client connection
     * @throws IOException
     */
    public void close() {
        try {
            if(this.inStream != null) {
                this.inStream.close();
            }
            if(this.outStream != null) {
                this.outStream.close();
            }
            if(this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
        } catch(Exception e) {
            LoggerFactory.getLogger(this.host.getHost()).error(e.getMessage(), e);
        }
        LoggerFactory.getLogger(this.host.getHost()).info("Client closing......"+socket.getInetAddress().toString());
    }

    /**
     * Whether client socket is closed
     * @return
     */
    public boolean isClosed() {
        if(this.socket != null) {
            return this.socket.isClosed();
        }
        return false;
    }
}
