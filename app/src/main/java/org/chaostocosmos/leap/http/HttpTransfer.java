package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.AUTH;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.REQUEST_LINE;
import org.chaostocosmos.leap.enums.TEMPLATE;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.session.Session;

/**
 * HttpTransfer object
 * 
 * @author 9ins
 */
public class HttpTransfer<T, R> implements Http {

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
    private HttpParser<T, R> httpParser;

    /**
     * Request
     */
    private HttpRequest<T> request;

    /**
     * Response
     */
    private HttpResponse<R> response;

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
        this.httpParser = new HttpParser<>(this.host, this.inputStream, this.outStream);
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
    public Map<REQUEST_LINE, String> getRequestLine() throws IOException {
        return this.httpParser.getFirstLines();
    }

    /**
     * Get request headers map
     * @return
     * @throws IOException
     */
    public Map<String, String> getRequestHeaders() throws IOException {        
        return this.httpParser.getRequestHeaders();
    }

    /**
     * Get request cookies map
     * @return
     * @throws IOException
     */
    public Map<String, String> getRequestCookies() throws IOException {
        return this.httpParser.getRequestCookies();
    }

    /**
     * Get http request
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpRequest<T> getRequest() throws IOException, URISyntaxException {
        if(this.request == null) {
            this.request = this.httpParser.parseRequest();
        }
        return this.request;
    }

    /**
     * Get http response
     * @return
     * @throws IOException 
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public HttpResponse<R> getResponse() throws IOException {
        if(this.response == null) {
            String msg = Html.resolvePlaceHolder(TEMPLATE.RESPONSE.loadTemplatePage(this.host.getId()), new HashMap<String, Object>() {{
                put("@serverName", host.getHost());
                put("@code", HTTP.RES200.code());
                put("@status", "OK");
                put("@message", HTTP.RES200.status());
            }});
            Map<String, List<String>> headers = addHeader(new HashMap<>(), "Content-Type", MIME.TEXT_HTML.mimeType());
            headers = addHeader(new HashMap<>(), "Content-Length", String.valueOf(msg.toString().getBytes().length));
            headers = addHeader(new HashMap<>(), "Charset", this.host.charset().name());
            this.response = this.httpParser.getResponse(HTTP.RES200.code(), (R) msg, headers);
        }
        return this.response;
    }
    
    /**
     * Replace placeholder to html page
     * @param htmlPage
     * @param placeHolderParams
     * @return
     */
    public String resolvePlaceHolder(String htmlPage, Map<String, Object> placeHolderParams) {
        return Html.resolvePlaceHolder(htmlPage, placeHolderParams);
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
    public HttpParser<T, R> getHttpParser() {
        return this.httpParser;
    }

    /**
     * Send respose to client
     * @throws IOException
     */
    public void sendResponse() {
        this.response.sendResponse();
    }

    /**
     * Process error
     * @param err
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public void processError(LeapException err) throws Exception {        

        //Get caused source error
        Throwable throwable = err;
        while(throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        PROTOCOL protocol = this.host.getProtocol();
        String hostName = this.host.getHost();
        int resCode = err.getHTTP().code();
        String status = HTTP.valueOf("RES"+resCode).status();

        StringBuffer resBuffer = new StringBuffer();
        String errorPage = Html.makeDefaultErrorHtml(this.host, err);
        resBuffer.append(protocol.name()+"/"+Constants.HTTP_VERSION+" "+resCode+" "+status+"\r\n"); 
        resBuffer.append("Date: "+new Date().toString()+Constants.CRLF);
        resBuffer.append("Server: "+hostName+Constants.CRLF);
        resBuffer.append("Content-Type: "+MIME.TEXT_HTML.mimeType()+"; charset="+this.host.charset()+Constants.CRLF);
        resBuffer.append("Content-Length: "+errorPage.getBytes().length+Constants.CRLF);
        resBuffer.append("Connection: close"+Constants.CRLF);
        if(err.code() == HTTP.RES401.code()) {
            if(this.host.getValue("global.login-page") != null) {
                String path = this.host.getValue("global.login-page").toString();
                path = path.startsWith("/") ? path.substring(1) : path;
                Path loginPage = this.host.getStatic().resolve(path);
                if(Files.exists(loginPage)) {                    
                    resBuffer.append("Set-Cookie: sessionid=deleted; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; HttpOnly"+Constants.CRLF);
                    resBuffer.append("Cache-Control: no-store"+Constants.CRLF);
                    resBuffer.append("X-Custom-Header: SomeValue"+Constants.CRLF);
                    resBuffer.append("Location: /"+path+Constants.CRLF);
                }    
            }
        } else if(err.code() == HTTP.RES307.code()) {
            RedirectException redirect = (RedirectException) throwable;
            resBuffer.append("Location: "+redirect.getURLString()+Constants.CRLF);
        }        
        resBuffer.append(Constants.CRLF2);
        resBuffer.append(errorPage);

        //Write error page bytes to output stream
        this.outStream.write(resBuffer.toString().getBytes());
        this.outStream.flush();
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
            this.inputStream.close();
            this.outStream.close();
            this.socket.close();
        } catch(Exception e) {
            e.printStackTrace();
            LoggerFactory.getLogger(this.host.getHost()).throwable(e);
        }
        LoggerFactory.getLogger().info("CLIENT SOCKET CLOSED: "+socket.getInetAddress().toString());
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
