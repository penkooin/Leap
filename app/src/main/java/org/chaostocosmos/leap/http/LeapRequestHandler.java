package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.commons.Hosts;
import org.chaostocosmos.leap.http.commons.HostsManager;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.ResourceHelper;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
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
     * Server
     */
    LeapHttpServer httpServer;
    
    /**
     * Client socket
     */
    Socket client;

    /**
     * Hosts
     */
    Hosts hosts;

    /**
     * Constructor with HeapHttpServer, root direcotry, index.html file, client socket 
     * @param httpServer
     * @param rootPath
     * @param client
     * @param hosts
     */
    public LeapRequestHandler(LeapHttpServer httpServer, Path rootPath, Socket client, Hosts hosts) {
        this.httpServer = httpServer;
        this.client = client;
        this.rootPath = rootPath;
        this.hosts = hosts;
    }

    @Override
    public void run() {
        HttpRequestDescriptor request = null;
        HttpResponseDescriptor response = null;
        HttpTransfer httpTransfer = null;
        String requestedHost = null;
        try {            
            httpTransfer = HttpTransferBuilder.buildHttpTransfer(httpServer.getHost(), this.client);
            request = httpTransfer.getRequest();
            response = httpTransfer.getResponse();
            //if(request != null && response != null) {
                requestedHost = request.getRequestedHost();            
                //Put requested host to request header Map for ip filter
                request.getReqHeader().put("@Client", requestedHost);
                //LoggerFactory.getLogger(requestedHost).debug("Request host: "+requestedHost); 
                Path resourcePath = ResourceHelper.getResourcePath(request);
                ServiceManager serviceManager = httpServer.getServiceManager();
                ServiceHolder serviceHolder = serviceManager.getMappingServiceHolder(request.getContextPath());
    
                //If client request context path in Services.
                if (serviceHolder != null) {
                    // Request method validation
                    if(serviceHolder.getRequestType() != request.getRequestType()) {
                        throw new WASException(MSG_TYPE.HTTP, 405, "Not supported: "+request.getRequestType().name());
                    } else if (serviceManager.vaildateRequestMethod(request.getRequestType(), request.getContextPath())) {
                        // Do requested service to execute by cloned service of request
                        response = ServiceInvoker.invokeService(serviceHolder, httpTransfer, true);
                    } else {
                        String message = Context.getHttpMsg(405);
                        String body = ResourceHelper.getResponsePage(requestedHost, Map.of("@code", 405, "@message", message));
                        response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType());
                        response.setBody(body.getBytes());
                        response.setStatusCode(405);                    
                    }
                } else { // When client request static resources
                    if(request.getContextPath().equals("/")) {
                        String body = ResourceHelper.getWelcomePage(requestedHost, Map.of("@serverName", requestedHost));
                        response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType());
                        response.setBody(body.getBytes());
                        response.setStatusCode(200);
                    } else {
                        if (resourcePath.toFile().exists()) {
                            File resourceFile = resourcePath.toFile();
                            String resourceName = resourceFile.getName();
                            String mimeType = UtilBox.probeContentType(resourcePath);
                            LoggerFactory.getLogger(requestedHost).debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);
                            //Condition of requeset resource in forbidden list
                            if(this.hosts.getForbiddenResourceFilters().stream().anyMatch(f -> !f.trim().equals("") && resourceName.matches(Arrays.asList(f.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*"))) {
                                String message = Context.getHttpMsg(403);
                                String body = ResourceHelper.getResponsePage(requestedHost, Map.of("@code", 403, "@type", "HTTP", "@message", message));
                                response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType());
                                response.setBody(body.getBytes());
                                response.setStatusCode(403);
                            } else if(this.hosts.getAllowedResourceFilters().stream().anyMatch(a -> !a.trim().equals("") && resourceName.matches(Arrays.asList(a.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*"))) {
                                //Condition of request resource in allowed list
                                response.addHeader("Content-Type", mimeType);
                                response.setBody(resourcePath);
                                response.setStatusCode(200);
                            } else {
                                LoggerFactory.getLogger(requestedHost).debug("Not allowed resource requested: "+resourceName);
                            }
                        } else {
                            //When requested resource is not found
                            throw new WASException(MSG_TYPE.HTTP, 404, request.getContextPath());
                        }
                    }
                }                    
            //} else {
            //    LoggerFactory.getLogger(requestedHost).warn(Context.getErrorMsg(50, this.client.getRemoteSocketAddress().toString()));
            //}
            if(response != null) {
                //Send response to client
                httpTransfer.sendResponse(response);
            }
            if(httpTransfer != null) {
                httpTransfer.close();
            }                
        } catch(Throwable e) {
            e.printStackTrace();
            try {
                processError(httpTransfer, e);
            } catch(Exception e1) {
                LoggerFactory.getLogger(requestedHost).error(e.getMessage(), e);
            }
        }
    }

    /**
     * Process error
     * @param error
     * @return
     * @throws WASException
     * @throws IOException
     */
    public void processError(HttpTransfer httpTransfer, Throwable error) throws WASException, IOException {
        String requestedHost = httpTransfer.getRequestedHost();
        Throwable t = getCaused(error);
                
        int resCode = -1;
        MSG_TYPE msgType = null;
        if(t instanceof WASException) {
            WASException w = (WASException)t;
            resCode = w.getCode();
            msgType = w.getMessageType();
        } else {
            resCode = 500;
            msgType = MSG_TYPE.HTTP;
        }
        if(!HostsManager.get().isExistHost(requestedHost)) {
            requestedHost = Context.getDefaultHost();
        }
        Map<String, List<Object>> headers = HttpTransferBuilder.addHeader(new HashMap<String, List<Object>>(), "Content-Type", "text/html");
        Object body = t != null ? HttpTransferBuilder.buildErrorResponse(requestedHost, msgType, resCode, t.getMessage()) : error.getMessage();
        httpTransfer.sendResponse(requestedHost, resCode, headers, body);
    }

    /**
     * Get caused error
     * @param e
     * @return
     */
    public Throwable getCaused(Throwable e) {
        Throwable throwable = e;
        do {
            if(throwable instanceof WASException || throwable == null) {
                break;
            }
            throwable = e.getCause();
        } while(true);
        return throwable;   
    }
}