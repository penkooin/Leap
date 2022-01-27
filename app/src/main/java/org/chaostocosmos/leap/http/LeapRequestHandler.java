package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.ResourceHelper;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.services.ServiceHolder;
import org.chaostocosmos.leap.http.services.ServiceInvoker;
import org.chaostocosmos.leap.http.services.ServiceManager;

/**
 * Client request processor object
 * 
 * @author 9ins
 * @since 2021.09.16
 */
public class LeapRequestHandler implements Runnable {
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
     * Constructor with root direcotry, index.html file, client socket
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
            LoggerFactory.getLogger(requestedHost).debug("Request host: "+requestedHost);
            
            // log request information
            LoggerFactory.getLogger(request.getRequestedHost()).debug(request.getReqHeader().entrySet().stream().map(e -> e.getKey()+": "+e.getValue()).collect(Collectors.joining(System.lineSeparator())));
            Path resourcePath = ResourceHelper.getResourcePath(request);

            ServiceHolder serviceHolder = this.serviceManager.getMappingServiceHolder(request.getContextPath());
            
            response.addHeader("Date", new Date()); 
            response.addHeader("Server", "LeapWAS "+Context.getVersion());

            //if client request servlet path
            if (serviceHolder != null) {
                // request method validation
                if(serviceHolder.getRequestType() == request.getRequestType()) {
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
                    response.setResponseBody(body.getBytes());
                    response.setResponseCode(200);
                } else {
                    if (resourcePath.toFile().exists()) {
                        response.setResponseCode(200);
                        String mimeType = UtilBox.probeContentType(resourcePath);
                        response.addHeader("Content-Type", mimeType);
                        if(resourcePath.toFile().getName().endsWith(".exe")) {
                            String message = Context.getHttpMsg(405);
                            String body = ResourceHelper.getResponsePage(requestedHost, Map.of("@code", 405, "@message", message));
                            response.setResponseBody(body.getBytes());
                            response.setResponseCode(405);
                        } else if(mimeType == null || mimeType.equals("application/x-msdownload")) {
                            byte[] rawData = ResourceHelper.getBinaryResource(resourcePath);
                            response.setResponseBody(rawData);
                            response.setResponseCode(200);
                        } else {
                            byte[] body = ResourceHelper.getResourceContent(requestedHost, request.getContextPath());
                            response.setResponseBody(body);
                            response.setResponseCode(200);;
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
            if(e instanceof WASException) {
                WASException we = (WASException)e;
                LoggerFactory.getLogger(requestedHost).error(we.getMessage(), we);
                String responseMessage = "Response";
                try {
                    responseMessage = createHttpResponsePage(requestedHost, we.getMessageType(), we.getCode(), we.getMessage());
                } catch (IOException | URISyntaxException | WASException e1) {
                    LoggerFactory.getLogger(requestedHost).error(e1.getMessage(), e1);
                }
                if(response == null) {
                    response = HttpBuilder.buildHttpResponse(request);
                }
                String remote = (connection != null) ? connection.getRemoteSocketAddress().toString() : "Unknown";
                String server = (connection != null) ? connection.getLocalSocketAddress().toString() : requestedHost;
                LoggerFactory.getLogger(requestedHost).info("Error client request "+remote+" --- "+server);
                response.setResponseBody(responseMessage.getBytes());                
                sendResponse(out, response);
                close();
                LoggerFactory.getLogger(requestedHost).info("Close client request on error "+remote+" --- "+server);
            } else {
                LoggerFactory.getLogger(requestedHost).error(e.getMessage(), e);
            }
        }
    }

    /**
     * Close client connection
     */
    public void close() {
        try {
            if(in != null) {
                in.close();
                //LoggerFactory.getLogger(this.requestedHost).debug("close input stream......");
            }
            if(out != null) {
                out.close();
                //LoggerFactory.getLogger(this.requestedHost).debug("close output stream......");
            }
            if(connection != null) {                
                connection.close();
                //LoggerFactory.getLogger(this.requestedHost).debug("socket close......");
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
    public String createHttpResponsePage(String host, MSG_TYPE type, int code, String message) throws IOException, URISyntaxException, WASException {
        Map<String, Object> map;
        if(type != null) {
            map = Map.of("@code", code, "@type", type.name(), "@message", message);
        } else {
            map = Map.of("@code", 0, "@type", "EXCEPTION", "@message", message);
        }
        return ResourceHelper.getResponsePage(host, map);
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
            LoggerFactory.getLogger(response.getRequestedHost()).debug(response.toString());
            out.write(res.getBytes()); 
            response.addHeader("Content-length", response.getResponseBody().length);
            for(Map.Entry<String, Object> e : response.getResponseHeader().entrySet()) {
               out.write((e.getKey()+": "+e.getValue()+"\r\n").getBytes()); 
            } 
            out.write("\r\n".getBytes());
            out.flush(); 
            out.write(response.getResponseBody()); 
            out.flush();
        } catch(IOException e) {
            LoggerFactory.getLogger(response.getRequestedHost()).error(e.getMessage(), e);
        }
    }
}