package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.common.ExceptionUtils;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.AUTH;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST_LINE;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.resource.Html;
import org.chaostocosmos.leap.resource.TemplateBuilder;
import org.chaostocosmos.leap.session.Session;

import ch.qos.logback.classic.Logger;

/**
 * HttpTransfer object
 * @author 9ins
 */
public class HttpTransfer implements Http {
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
    private InputStream inputStream;
    /**
     * OutputStream
     */
    private OutputStream outStream;
    /**
     * Http parser
     */
    private HttpParser httpParser;
    /**
     * Request
     */
    private HttpRequest request;
    /**
     * Response
     */
    private HttpResponse response;
    /**
     * Http session
     */
    private Session session;
    /**
     * Whether closed
     */
    boolean isClosed = false;
    /**
     * Construct with request host and client socket
     * @param host
     * @param socket
     * @throws IOException
     */
    public HttpTransfer(Host<?> host, Socket socket) throws IOException {
        this.host = host;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outStream = socket.getOutputStream();
        this.httpParser = new HttpParser(this.host, this.inputStream, this.outStream);
    }
    /**
     * Get requested host
     * @return
     */
    public Host<?> getHost() {
        return this.host;
    }
    /**
     * Get logger for this HttpTransfer
     * @return
     */
    public Logger getLogger() {
        return this.host.getLogger();
    }
    /**
     * Get Client socket object
     * @return
     */
    public Socket getSocket() {
        return this.socket;
    }
    /**
     * Get client InputStream
     * @return
     */
    public InputStream getClientInputStream() {
        return inputStream;
    }
    /**
     * Get client OutputStream
     * @return
     */
    public OutputStream getClientOutputStream() {
        return outStream;
    }
    /**
     * Get request first line
     * @return
     * @throws IOException
     */
    public Map<REQUEST_LINE, Object> getRequestLine() throws IOException {
        return this.httpParser.parseRequestLine();
    }
    /**
     * Get request headers map
     * @return
     * @throws IOException
     */
    public Map<String, String> getRequestHeaders() throws IOException {        
        return this.httpParser.parseRequestHeaders();
    }
    /**
     * Get request cookies map
     * @return
     * @throws IOException
     */
    public Map<String, String> getRequestCookies() throws IOException {
        return this.httpParser.parseRequestCookies();
    }
    /**
     * Get http request
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpRequest getRequest() throws IOException, URISyntaxException {
        if(this.request == null) {
            this.request = this.httpParser.parseRequest();
        }
        return this.request;
    }
    /**
     * Get http response
     * @return
     * @throws IOException
     */
    public HttpResponse getResponse() throws IOException {
        if(this.response == null) {
            String msg = TemplateBuilder.buildResponseHtml(this.host, 200, HTTP.RES200.status());
            Map<String, List<String>> headers = addHeader(new HashMap<>(), "Content-Type", MIME.TEXT_HTML.mimeType());
            headers = addHeader(new HashMap<>(), "Content-Length", String.valueOf(msg.getBytes().length));
            headers = addHeader(new HashMap<>(), "Charset", this.host.<String> charset());
            this.response = this.httpParser.buildResponse(HTTP.RES200.code(), msg, headers);
        }
        return this.response;
    }    
    /**
     * Get session instance
     * @return
     */
    public Session getSession() {
        return this.session;
    }
    /**
     * Set session instance
     * @param session
     */
    public void setSession(Session session) {
        this.session = session;
    }
    /**
     * Get HttpParser
     * @return
     */
    public HttpParser getHttpParser() {
        return this.httpParser;
    }
    /**
     * Send respose to client
     * @throws IOException
     */
    public void sendResponse() {
        sendResponse(this.response);
    }
    /**
     * Send response to client by Response object
     * @param response
     * @throws IOException
     */
    public void sendResponse(HttpResponse response) {
        sendResponse(this.host, response.getResponseCode(), response.getHeaders(), response.getBody());
    }
    /**
     * Send response to client by requested host, status code, reponse headers, body object
     * @param host
     * @param resCode
     * @param headers
     * @param body
     */
    public void sendResponse(Host<?> host, int resCode, Map<String, List<String>> headers, Object body) {
        Charset charset = Charset.forName(this.host.charset());
        String protocol = this.host.<String> getProtocol();
        String resMsg = HTTP.valueOf("RES"+resCode).status();
        String res = protocol+"/"+Constants.HTTP_VERSION+" "+resCode+" "+resMsg+"\r\n"; 
        if(body == null) {
            this.host.getLogger().warn("Response body is Null: "+resCode);
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
            throw new LeapException(HTTP.RES501, new NotSupportedException("Not support response body type: "+body.getClass().getName()));
        }
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(contentLength));
        headers.put("Content-Length", values);

        //LoggerFactory.getLogger(response.getRequestedHost()).debug(response.toString());
        StringBuffer resStr = new StringBuffer();
        resStr.append("////////////////////////////// [RESPONSE] : "+res.trim()+" - "+this.socket.getRemoteSocketAddress().toString()+" //////////////////////////////"+System.lineSeparator());
        resStr.append("RES CODE: "+resCode+System.lineSeparator());
        try {
            this.outStream.write(res.getBytes());
            for(Map.Entry<String, List<String>> e : headers.entrySet()) {
                String hv = e.getValue().stream().map(v -> v.toString()).collect(Collectors.joining("; "));
                this.outStream.write((e.getKey()+": "+hv+"\r\n").getBytes());
                resStr.append(e.getKey()+": "+hv+System.lineSeparator());
            }
            this.host.getLogger().debug(resStr.substring(0, resStr.length()-1));
            this.outStream.write("\r\n".getBytes()); 
            this.outStream.flush(); 
            if(body instanceof byte[]) {
                this.outStream.write((byte[]) body);
            } else { 
                if(body instanceof String) {                                       
                    this.outStream.write(body.toString().getBytes());
                } else if(body instanceof File) {
                    writeToStream((File)body, this.outStream, Context.get().server().getFileBufferSize());
                } else if(body instanceof Path) {
                    writeToStream(((Path)body).toFile(), this.outStream, Context.get().server().getFileBufferSize());
                } else {
                    throw new IllegalArgumentException("Not supported response body type: "+body.getClass().getName());
                }
            }
            this.outStream.flush();
        } catch(Exception e) {
            this.host.getLogger().error(e.getMessage(), e);
        } finally {
            close();
        }
    }
    /**
     * Process error
     * @param err
     */
    public void processError(LeapException err) {        
        //this.host.getLogger().error(err.getMessage(), err);
        if(this.response == null) {
            try {
                this.response = getResponse();
            } catch (IOException e) {
                this.host.getLogger().error(e.getMessage(), e);
            }
        }
        Throwable throwable = err;
        while(throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        String message = throwable.getMessage()+"";
        String stackTrace = "";
        String hostId = this.host.getHostId();
        int resCode = HTTP.RES500.code();
        if(err instanceof LeapException) {
            resCode = err.code();
            if(err.code() == HTTP.RES401.code() && AUTH.valueOf(host.getAuthentication()) == AUTH.BASIC) {
                this.response.addHeader("WWW-Authenticate", "Basic");
            } else if(err.code() == HTTP.RES307.code()) {
                RedirectException redirect = (RedirectException) throwable;
                this.response.removeAllHeader();
                Html.makeRedirectHeader(0, redirect.getURLString()).entrySet().stream().forEach(e -> this.response.addHeader(e.getKey(), e.getValue()));
                this.response.addHeader("Location", redirect.getURLString());
            }
            if(Context.get().host(hostId).<Boolean> getErrorDetails()) {
                stackTrace = "<pre>" + err.getStackTraceMessage() + "<pre>";
            }
        } else {
            resCode = HTTP.RES500.code();            
        }
        if(!Context.get().hosts().isExistHostname(hostId)) {
            hostId = Context.get().hosts().getDefaultHost().getHostId();
        }        
        try {
            Object body = TemplateBuilder.buildErrorHtml(Context.get().host(hostId), resCode, message, stackTrace);            
            this.response.setResponseCode(resCode);
            this.response.setContentLength(body.toString().getBytes().length);
            this.response.setBody(body);
            sendResponse();
        } catch (IOException e) {
            this.host.getLogger().error(e.getMessage(), e);            
        }
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
        if(headers == null) {
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
     */
    public void close() {
        try {
            if(this.inputStream != null) {
                this.inputStream.close();
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
        LoggerFactory.getLogger(this.host.getHost()).info("CLIENT SOCKET CLOSED: "+socket.getInetAddress().toString());
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
