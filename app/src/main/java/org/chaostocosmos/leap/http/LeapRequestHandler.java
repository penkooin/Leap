package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.ResourceHelper;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.services.ServiceHolder;
import org.chaostocosmos.leap.http.services.ServiceInvoker;
import org.chaostocosmos.leap.http.services.ServiceManager;

import ch.qos.logback.classic.Logger;

/**
 * Client request processor object
 * 
 * @author 9ins
 * @since 2021.09.16
 */
public class LeapRequestHandler implements Runnable {
    /**
     * Logger
     */
    private static Logger logger;
    /**
     * Path doc root
     */
    private Path rootPath;
    /**
     * welcome index html
     */
    private String welcome = Context.getWelcome();
    /**
     * Client socket
     */
    private Socket connection;
    private InputStream in = null; 
    private OutputStream out = null;
    private HttpRequestDescriptor request = null;
    private HttpResponseDescriptor response = null;
    private String requestedHost = null;
    /**
     * Servlet manager
     */
    private ServiceManager serviceManager;

    /**
     * Constructor with HeapHttpServer, root direcotry, index.html file, client socket 
     * @param httpServer
     * @param rootPath
     * @param welcome
     * @param connection
     * @throws IOException
     * @throws WASException
     */
    public LeapRequestHandler(LeapHttpServer httpServer, Path rootPath, String welcome, Socket connection) throws IOException {
        this.serviceManager = httpServer.getServiceManager();
        this.rootPath = rootPath;
        if (welcome != null) {
            this.welcome = welcome;
        }
        this.connection = connection;
        in = connection.getInputStream();    
        out = connection.getOutputStream();
    }

    @Override
    public void run() {
        this.requestedHost = Context.getDefaultHost();
        try {
            request = HttpParser.getRequestParser().parseRequest(in);
            response = HttpBuilder.buildHttpResponse(request);
            //request.printURLInfo();
            //Put client address to request header Map for ip filter
            requestedHost = request.getRequestedHost();
            request.getReqHeader().put("@Client", requestedHost);
            //LoggerFactory.getLogger(requestedHost).debug("Request host: "+requestedHost);
            
            //Log request information
            Path resourcePath = ResourceHelper.getResourcePath(request);
            ServiceHolder serviceHolder = this.serviceManager.getMappingServiceHolder(request.getContextPath());
            response.addHeader("Date", new Date()); 
            response.addHeader("Server", "Leap? "+Context.getVersion());

            //If client request servlet path
            if (serviceHolder != null) {
                // request method validation
                if(serviceHolder.getRequestType() != request.getRequestType()) {
                    throw new WASException(MSG_TYPE.HTTP, 405, "<br>Requested method not supported: "+request.getRequestType().name());
                }
                if (this.serviceManager.vaildateRequestMethod(request.getRequestType(), request.getContextPath())) {
                    ServiceInvoker.invokeService(serviceHolder, request, response);
                    response.setResponseCode(200);
                } else {
                    String message = Context.getHttpMsg(405);
                    String body = ResourceHelper.getResponsePage(requestedHost, Map.of("@code", 405, "@message", message));
                    response.setResponseBody(body.getBytes());
                    response.setResponseCode(405);
                }
            } else { // When client request static resources
                if(request.getContextPath().equals("/")) {
                    String body = ResourceHelper.getWelcomePage(requestedHost, Map.of("@serverName", requestedHost));
                    response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType()+"; charset=" + Context.charset().name().toLowerCase());
                    response.setResponseBody(body.getBytes());
                    response.setResponseCode(200);
                } else {
                    if (resourcePath.toFile().exists()) {
                        File resourceFile = resourcePath.toFile();
                        String resourceName = resourceFile.getName();
                        String mimeType = UtilBox.probeContentType(resourcePath);
                        LoggerFactory.getLogger(requestedHost).debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);
                        //Condition of requeset resource in forbidden list
                        if(Context.getResourceForbidden().stream().anyMatch(f -> !f.trim().equals("") && resourceName.matches(Arrays.asList(f.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*"))) {
                            String message = Context.getHttpMsg(403);
                            String body = ResourceHelper.getResponsePage(requestedHost, Map.of("@code", 403, "@type", "HTTP", "@message", message));
                            response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType()+"; charset=" + Context.charset().name().toLowerCase());
                            response.setResponseBody(body.getBytes());
                            response.setResponseCode(403);
                        } else 
                        //Condition of request resource in allowed list
                        if(Context.getResourceAllowed().stream().anyMatch(a -> !a.trim().equals("") && resourceName.matches(Arrays.asList(a.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*"))) {
                            response.addHeader("Content-Type", mimeType);
                            response.setResponseBody(resourcePath);
                            response.setResponseCode(200);
                        } else {
                            LoggerFactory.getLogger(requestedHost).debug("Not allowed resource requested: "+resourceName);
                        }
                    } else {
                        //When requested resource is not found
                        throw new WASException(MSG_TYPE.HTTP, 404, request.getContextPath());
                    }
                }
            }
            sendResponse(out, response);
            close();
        } catch(Throwable e) {
            String responseMessage = null;
            if(e instanceof WASException) {
                WASException we = (WASException)e;
                if(e.getCause() instanceof SocketTimeoutException == false) {
                    LoggerFactory.getLogger(requestedHost).error(we.toString(), we);
                }
                LoggerFactory.getLogger(requestedHost).warn("Type: "+we.getMessageType()+" Code: "+we.getCode()+" Exception: "+we.toString());
                responseMessage = createHttpResponsePage(requestedHost, we.getMessageType(), we.getCode(), we.toString());
                if(response == null) {
                    response = HttpBuilder.buildHttpResponse(request);
                }
                String remote = (connection != null) ? connection.getRemoteSocketAddress().toString() : "Unknown";
                String server = (connection != null) ? connection.getLocalSocketAddress().toString() : requestedHost;
                //LoggerFactory.getLogger(requestedHost).info("Error client request "+remote+" --- "+server);
                //LoggerFactory.getLogger(requestedHost).info("Close client request on error "+remote+" --- "+server);
            } else {
                LoggerFactory.getLogger(requestedHost).error(e.getMessage(), e);
                responseMessage = createHttpResponsePage(requestedHost, MSG_TYPE.HTTP, 503, e.toString());
                if(response == null) {
                    response = HttpBuilder.buildHttpResponse(request);
                }
            }
            if(responseMessage == null) {
                responseMessage = "Unknown error: something went wrong in server......";
            }
            response.setResponseBody(responseMessage.getBytes(Context.charset()));                
            sendResponse(out, response);
            close();
        }
    }

    /**
     * Send response to client
     * @param out
     * @param response
     * @throws IOException
     */
    public void sendResponse(OutputStream out, HttpResponseDescriptor response) {
        try {
            String res = Context.getHttpVersion()+" "+response.getResponseCode()+" "+Context.getHttpMsg(response.getResponseCode())+"\r\n"; 
            out.write(res.getBytes());
            Object body = response.getResponseBody();
            if(body == null) {
                throw new IllegalArgumentException("Response body not set. Something wrong in Respose process!!!");
            }
            if(response.getResponseHeader().get("Content-Length") == null) {
                int contentLength = body instanceof byte[] ? ((byte[])body).length : (int)((Path)body).toFile().length();
                response.addHeader("Content-Length", contentLength);
            }
            //LoggerFactory.getLogger(response.getRequestedHost()).debug(response.toString());
            for(Map.Entry<String, Object> e : response.getResponseHeader().entrySet()) {
                out.write((e.getKey()+": "+e.getValue()+"\r\n").getBytes()); 
            } 
            out.write("\r\n".getBytes());
            out.flush(); 
            if(body instanceof byte[]) {
                out.write((byte[]) body);
            } else {
                if(body instanceof File) {
                    writeToStream((File)body, out, Context.getFileBufferSize());
                } else if(body instanceof Path) {
                    writeToStream(((Path)body).toFile(), out, Context.getFileBufferSize());
                } else {
                    throw new IllegalArgumentException("Not supported response body type: "+body.getClass().getName());
                }
            }
            out.flush();
        } catch(Exception e) {
            LoggerFactory.getLogger(response.getRequestedHost()).error(e.getMessage(), e);
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
     * Close client connection
     */
    public void close() {
        try {
            if(in != null) {
                in.close();
                //LoggerFactory.getLogger(this.requestedHost).info("close input stream......");
            }
            if(out != null) {
                out.close();
                //LoggerFactory.getLogger(this.requestedHost).info("close output stream......");
            }
            if(connection != null) {                
                LoggerFactory.getLogger(this.requestedHost).info("Client close......"+connection.getInetAddress().toString());
                connection.close();
            }
        } catch (IOException e) {
            LoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    /**
     * Create http response page
     * @param host
     * @param type
     * @param code
     * @param message
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws WASException
     */
    public String createHttpResponsePage(String host, MSG_TYPE type, int code, String message) {
        Map<String, Object> map;
        if(type != null) {
            map = Map.of("@code", code, "@type", type.name(), "@message", message);
        } else {
            map = Map.of("@code", 0, "@type", "EXCEPTION", "@message", message);
        }
        try {
            return ResourceHelper.getResponsePage(host, map);
        } catch (WASException e) {
            e.printStackTrace();
            return null;
        }
    }
}