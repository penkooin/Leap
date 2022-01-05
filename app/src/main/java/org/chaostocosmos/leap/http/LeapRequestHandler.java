package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.service.ServiceHolder;
import org.chaostocosmos.leap.http.service.ServiceInvoker;
import org.chaostocosmos.leap.http.service.ServiceManager;

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
     * @throws WASException
     */
    public LeapRequestHandler(LeapHttpServer httpServer, Path rootPath, String welcome, Socket connection) throws WASException {
        this.serviceManager = httpServer.getServiceManager();
        this.rootPath = rootPath;
        if (welcome != null) {
            this.welcome = welcome;
        }
        this.connection = connection;
    }

    @Override
    public void run() {
        HttpRequestDescriptor request = null;
        HttpResponseDescriptor response = null;
        InputStream in = null; 
        OutputStream out = null;
        String requestedHost = Context.getDefaultHost();
        try {
            try {
                in = connection.getInputStream();    
                out = connection.getOutputStream();
            } catch(IOException ioe){
                throw new WASException(ioe);
            }    
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

            String contextPath = request.getContextPath();
            ServiceHolder serviceHolder = this.serviceManager.getMappingServiceHolder(request.getContextPath());
            
            response.addHeader("Date", new Date()); 
            response.addHeader("Server", "LeapWAS 1.0.0");

            //if client request servlet path
            if (serviceHolder != null) {
                // request method validation
                if (this.serviceManager.vaildateRequestMethod(request.getRequestType(), request.getContextPath())) {
                    ServiceInvoker.invokeService(serviceHolder, request, response);
                    response.setResponseCode(200);
                } else {
                    String message = Context.getHttpMsg(405);
                    String body = ResourceHelper.getResourceContent(requestedHost, "response.html", Map.of("@code", 405, "@message", message));
                    response.setResponseBody(body.getBytes());
                    response.setResponseCode(405);
                }
            } else { // When client request static resources
                if(request.getContextPath().equals("/")) {
                    String body = ResourceHelper.getResourceContent(requestedHost, "index.html", Map.of("@serverName", requestedHost));
                    response.setResponseBody(body.getBytes());
                    response.setResponseCode(200);
                } else {
                    if (resourcePath.toFile().exists()) {
                        response.setResponseCode(200);
                        String mimeType = UtilBox.probeContentType(resourcePath);
                        response.addHeader("Content-Type", mimeType);
                        if(resourcePath.toFile().getName().endsWith(".exe")) {
                            String message = Context.getHttpMsg(405);
                            String body = ResourceHelper.getResourceContent(requestedHost, "response.html", Map.of("@code", 405, "@message", message));
                            response.setResponseBody(body.getBytes());
                            response.setResponseCode(405);
                        } else if(mimeType == null || mimeType.equals("application/x-msdownload")) {
                            byte[] rawData = ResourceHelper.getBinaryResource(resourcePath);
                            response.setResponseBody(rawData);
                            response.setResponseCode(200);
                        } else {
                            String body = ResourceHelper.getResourceContent(requestedHost, contextPath, null);
                            response.setResponseBody(body.getBytes());
                            response.setResponseCode(200);;
                        }
                    } else {
                        //When requested resource is not found
                        throw new WASException(MSG_TYPE.HTTP, 404);
                    }
                }
            }
            sendResponse(out, response);
        } catch(Exception e) {        
            Throwable throwable = null;
            while((throwable = e.getCause()) != null) {
                e = (Exception)throwable;
            } 
            e.printStackTrace();
            WASException we = (WASException) e;
            String responseMessage = createHttpResponsePage(requestedHost, we.getMessageType(), we.getCode(), we.getMessage());
            if(response == null) {
                response = HttpBuilder.buildHttpResponse(request);
            }
            response.setResponseBody(responseMessage.getBytes());
            sendResponse(out, response);
        } finally {
            try {                
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create http response page
     * @param host
     * @param type
     * @param code
     * @param message
     * @return
     * @throws WASException
     */
    public String createHttpResponsePage(String host, MSG_TYPE type, int code, String message) {
        try {
            return ResourceHelper.getResourceContent(host, "response.html", Map.of("@code", code, "@type", type.name(), "@message", message));
        } catch (WASException e) {
            e.printStackTrace();
            LoggerFactory.getLogger(host).error(e.getMessage(), e);
        }
        return null;
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
            e.printStackTrace();
            LoggerFactory.getLogger(response.getRequestedHost()).error(e.getMessage(), e);
        }
    }
}