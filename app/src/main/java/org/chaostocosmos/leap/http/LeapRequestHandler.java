package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.commons.Hosts;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
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
     * Leap server home path
     */
    Path LEAP_HOME;

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
    public LeapRequestHandler(LeapHttpServer httpServer, Path LEAP_HOME, Socket client, Hosts hosts) {
        this.httpServer = httpServer;
        this.LEAP_HOME = LEAP_HOME;
        this.client = client;
        this.hosts = hosts;
    }

    @Override
    public void run() {
        HttpRequestDescriptor request = null;
        HttpResponseDescriptor response = null;
        HttpTransfer httpTransfer = null;        
        try {            
            httpTransfer = HttpTransferBuilder.buildHttpTransfer(httpServer.getHost(), this.client);
            request = httpTransfer.getRequest();
            response = httpTransfer.getResponse();
            Hosts hosts = httpTransfer.getHosts();

            //Put requested host to request header Map for ip filter
            request.getReqHeader().put("@Client", hosts.getHost());

            ServiceManager serviceManager = httpServer.getServiceManager();
            ServiceHolder serviceHolder = serviceManager.getMappingServiceHolder(request.getContextPath());    
            //If client request context path in Services.
            if (serviceHolder != null) {
                // Request method validation
                if(serviceHolder.getRequestType() != request.getRequestType()) {
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES405.getCode(), "Not supported: "+request.getRequestType().name());
                } else if (serviceManager.vaildateRequestMethod(request.getRequestType(), request.getContextPath())) {
                    // Do requested service to execute by cloned service of request
                    response = ServiceInvoker.invokeService(serviceHolder, httpTransfer, true);
                } else {
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES405.getCode(), Context.getHttpMsg(405));
                }
            } else { // When client request static resources
                Path resourcePath = ResourceHelper.getResourcePath(request);
                if(request.getContextPath().equals("/")) {
                    String body = hosts.getResource().getWelcomePage(Map.of("@serverName", hosts.getHost()));
                    response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.getMimeType());
                    response.setBody(body.getBytes());
                    response.setStatusCode(RES_CODE.RES200.getCode());
                } else {
                    if (resourcePath.toFile().exists()) {
                        File resourceFile = resourcePath.toFile();
                        String resourceName = resourceFile.getName();
                        int responseCode = RES_CODE.RES200.getCode();
                        //Get requested resource data
                        Object resource = hosts.getResource().getResource(resourcePath);
                        if(resource != null) {
                            String mimeType = UtilBox.probeContentType(resourcePath);
                            if(mimeType == null) {
                                mimeType = MIME_TYPE.APPLICATION_OCTET_STREAM.getMimeType();
                            }
                            response.setStatusCode(responseCode);
                            response.addHeader("Content-Type", mimeType);
                            response.setBody(resource);
                            LoggerFactory.getLogger(hosts.getHost()).debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);
                        } else {
                            throw new WASException(MSG_TYPE.HTTP, 403, request.getContextPath());
                        }
                    } else {
                        throw new WASException(MSG_TYPE.HTTP, 404, request.getContextPath());
                    }
                }   
            }                 
            if(response != null) {
                //Send response to client
                httpTransfer.sendResponse(response);
            }
            if(httpTransfer != null) {
                httpTransfer.close();
            }                
        } catch(Throwable e) {
            LoggerFactory.getLogger(httpTransfer.getHosts().getHost()).error(e.getMessage(), e);
            processError(httpTransfer, e);
        }
    }

    /**
     * Process error
     * @param error
     * @return
     * @throws WASException
     * @throws IOException
     */
    public void processError(HttpTransfer httpTransfer, Throwable error) {
        try {
            String requestedHost = httpTransfer.getHosts().getHost();
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
        } catch(Exception e) {
            LoggerFactory.getLogger(httpTransfer.getHosts().getHost()).error(e.getMessage(), e);
        }
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