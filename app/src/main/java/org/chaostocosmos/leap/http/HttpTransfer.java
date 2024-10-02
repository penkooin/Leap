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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.file.FileUtils;
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
    public Map<REQUEST_LINE, String> getRequestLine() throws IOException {
        return this.httpParser.parseRequestLine();
    }

    /**
     * Get request headers map
     * @return
     * @throws IOException
     */
    public Map<String, List<?>> getRequestHeaders() throws IOException {        
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
     * @throws Exception 
     */
    public HttpResponse getResponse() throws Exception {
        if(this.response == null) {
            String msg = this.resolvePlaceHolder(TEMPLATE.RESPONSE.loadTemplatePage(this.host.getId()), Map.of("@serverName", this.host.getHost(), "@code", HTTP.RES200.code(), "@status", "OK", "@message", HTTP.RES200.status()));
            Map<String, List<String>> headers = addHeader(new HashMap<>(), "Content-Type", MIME.TEXT_HTML.mimeType());
            headers = addHeader(new HashMap<>(), "Content-Length", String.valueOf(msg.getBytes().length));
            headers = addHeader(new HashMap<>(), "Charset", this.host.charset());
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
        this.response.sendResponse();
    }

    /**
     * Process error
     * @param err
     * @throws Exception 
     */
    public void processError(LeapException err) throws Exception {        
        this.host.getLogger().error(err.getMessage(), err);
        if(this.response == null) {
            try {
                this.response = getResponse();
            } catch (IOException e) {
                this.host.getLogger().throwable(e);
            }
        }
        Throwable throwable = err;
        while(throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        String serverName = this.host.getHost();
        int resCode = HTTP.RES500.code();
        String status = HTTP.valueOf("RES"+resCode).status();
        String message = throwable.getMessage()+"";
        String stacktrace = "";
        Map<String, ?> paramMap = Map.of("@serverName", serverName, 
                                         "@code", resCode, 
                                         "@status", status, 
                                         "@message", message, 
                                         "@stacktrace", stacktrace);
        Object body = resolvePlaceHolder(TEMPLATE.ERROR.loadTemplatePage(this.host.getId()), paramMap);                                                                                    
        String hostId = this.host.getId();
        if(err instanceof LeapException) {
            resCode = err.code();
            if(err.code() == HTTP.RES401.code() && host.getAuthentication() == AUTH.BASIC) {
                this.response.addHeader("WWW-Authenticate", "Basic");
            } else if(err.code() == HTTP.RES307.code()) {
                RedirectException redirect = (RedirectException) throwable;
                this.response.removeAllHeader();
                Html.makeRedirectHeader(0, redirect.getURLString()).entrySet().stream().forEach(e -> this.response.addHeader(e.getKey(), e.getValue()));
                this.response.addHeader("Location", redirect.getURLString());
            }
            if(Context.get().host(hostId).getLogsDetails()) {
                stacktrace = "<pre>" + err.getStackTraceMessage() + "<pre>";
            }
        } else {
            resCode = HTTP.RES500.code();            
        }        
        this.response.setResponseCode(resCode);
        this.response.setContentLength(body.toString().getBytes().length);
        this.response.addHeader("Refresh", "0; /error?"+paramMap.entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining("&")));
        this.response.setBody(body);
        sendResponse();
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
     * Resolve placeholders in HTML page
     * @param htmlPage
     * @param placeHolderValueMap
     * @return
     */
    public String resolvePlaceHolder(String htmlPage, Map placeHolderValueMap) {
        String regex = Constants.PLACEHOLDER_REGEX;
        Pattern ptrn = Pattern.compile(regex);
        Matcher matcher = ptrn.matcher(htmlPage);
        while(matcher.find()) {
            String match = matcher.group(1).trim();
            String key = match.replace("<!--", "").replace("-->", "").trim();
            System.out.println(match+"////////////////////"+key);
            if(placeHolderValueMap.containsKey(key)) {
                Object obj = placeHolderValueMap.get(key);
                System.out.println(obj);                
                htmlPage = htmlPage.substring(0, htmlPage.indexOf(match))
                           + placeHolderValueMap.get(key)
                           + htmlPage.substring(htmlPage.indexOf(match)+match.length());
            }
        }
        return htmlPage;
    }

    /**
     * Close client connection
     */
    public void close() {
        try {
            if(this.inputStream != null) this.inputStream.close();
            if(this.outStream != null) this.outStream.close();
            if(this.socket != null) this.socket.close();
        } catch(Exception e) {
            LoggerFactory.getLogger(this.host.getHost()).throwable(e);
        }
        this.inputStream = null;
        this.outStream = null;
        this.socket = null;
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
