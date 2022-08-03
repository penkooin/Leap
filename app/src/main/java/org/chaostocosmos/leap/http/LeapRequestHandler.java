package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.Resource;
import org.chaostocosmos.leap.http.resources.ResourceHelper;
import org.chaostocosmos.leap.http.resources.TemplateBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Client request processor object
 * 
 * @author 9ins
 * @since 2021.09.16
 */
@State(Scope.Thread)
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
    Host<?> host;
    /**
     * Constructor with HeapHttpServer, root direcotry, index.html file, client socket
     * @param httpServer
     * @param rootPath
     * @param client
     * @param host
     */
    public LeapRequestHandler(LeapHttpServer httpServer, Path LEAP_HOME, Socket client, Host<?> host) {
        this.httpServer = httpServer;
        this.LEAP_HOME = LEAP_HOME;
        this.client = client;
        this.host = host;
    }

    @Override
    @Benchmark
    public void run() {
        Request request = null;
        Response response = null;
        HttpTransfer httpTransfer = null;
        try {
            httpTransfer = HttpTransferBuilder.buildHttpTransfer(httpServer.getHostId(), this.client);
            request = httpTransfer.getRequest();
            response = httpTransfer.getResponse();
            Host<?> host = httpTransfer.getHost();

            //Put requested host to request header Map for ip filter
            request.getReqHeader().put("@Client", host.getHost());
            ServiceManager serviceManager = httpServer.getServiceManager();
            ServiceHolder serviceHolder = serviceManager.createServiceHolder(request.getContextPath());

            //If client request context path in Services.
            if (serviceHolder != null) {
                // Request method validation
                if(serviceHolder.getRequestType() != request.getRequestType()) {
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES405.code(), "Not supported: "+request.getRequestType().name());
                } else {
                    // Do requested service to execute by cloned service of request
                    response = ServiceInvoker.invokeServiceMethod(serviceHolder, httpTransfer);
                }
            } else { // When client request static resources
                if(request.getRequestType() != REQUEST_TYPE.GET) {
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES405.code(), "Static content can't be provided by "+request.getRequestType().name());
                }
                Path resourcePath = ResourceHelper.getResourcePath(request);
                if(request.getContextPath().equals("/")) {
                    String body = TemplateBuilder.buildWelcomeResourceHtml(request.getContextPath(), host);                    
                    response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.mimeType()+"; charset="+host.<String> charset());
                    response.setBody(body.getBytes());
                    response.setResponseCode(RES_CODE.RES200.code());
                } else {
                    if (host.getResource().exists(resourcePath)) {
                        //Get requested resource data
                        Resource resource = host.getResource().getResource(resourcePath);
                        if(resource != null) {
                            if(resource.isNode()) {
                                String body = TemplateBuilder.buildResourceHtml(request.getContextPath(), host);
                                String mimeType = MIME_TYPE.TEXT_HTML.mimeType();
                                response.setResponseCode(RES_CODE.RES200.code());
                                response.addHeader("Content-Type", mimeType+"; charset="+host.charset());
                                response.setBody(body);
                                //LoggerFactory.getLogger(hosts.getHost()).debug("RESOURCE LIST REQUESTED: "+body);
                            } else {
                                String mimeType = UtilBox.probeContentType(resource.getPath());
                                if(mimeType == null) {
                                    mimeType = MIME_TYPE.APPLICATION_OCTET_STREAM.mimeType();
                                }
                                response.setResponseCode(RES_CODE.RES200.code());
                                response.addHeader("Content-Type", mimeType);
                                response.setBody(resource.getBytes());
                                LoggerFactory.getLogger(host.getHost()).debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);    
                            }
                        } else {
                            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES403.code(), request.getContextPath());
                        }
                    } else {
                        throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), request.getContextPath());
                    }
                }
            }                 
            if(!httpTransfer.isClosed()) {
                //Send response to client
                httpTransfer.sendResponse(response);
            }
        } catch(SocketTimeoutException e) {
            LoggerFactory.getLogger(httpTransfer.getHost().getHostId()).error("[SOCKET TIME OUT] Client socket timeout occurred.");
        } catch(Throwable e) {
            LoggerFactory.getLogger(httpTransfer.getHost().getHost()).error(e.getMessage(), e);
            try {
                if(!httpTransfer.isClosed()) {
                    processError(httpTransfer, e);
                }
            } catch (Exception ex) {                
                LoggerFactory.getLogger(httpTransfer.getHost().getHost()).error(e.getMessage(), ex);
            }
        } finally {
            if(httpTransfer != null) {
                httpTransfer.close();
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
    public void processError(HttpTransfer httpTransfer, Throwable error) throws Exception {        
        Throwable t = getCaused(httpTransfer.getHost().getHostId(), error);
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
        String hostId = httpTransfer.getHost().getHostId();
        if(!Context.getHosts().isExistHostname(hostId)) {
            hostId = Context.getHosts().getDefaultHost().getHostId();
        }
        if(msgType == MSG_TYPE.ERROR) {
            msgType = MSG_TYPE.HTTP;
            resCode = 500;
        }
        Map<String, List<Object>> headers = new HashMap<String, List<Object>>();
        headers = HttpTransferBuilder.addHeader(headers, "Content-Type", "text/html; charset="+Context.getHost(hostId).charset());
        Object body = t != null ? TemplateBuilder.buildErrorHtml(Context.getHost(hostId), msgType, resCode, t.getMessage()) : Context.getMessages().getHttpMsg(resCode);
        httpTransfer.sendResponse(hostId, resCode, headers, body);            
    }

    /**
     * Get caused error
     * @param e
     * @return
     */
    public Throwable getCaused(String host, Throwable e) {        
        Throwable top = e;
        int level = 0;
        do {
            if(e instanceof WASException || e == null) {
                break;
            }
            e = e.getCause();
            level++;
        } while(level < 5);
        LoggerFactory.getLogger(host).debug("[FOUND CAUSED] "+e);
        return e;   
    }
}