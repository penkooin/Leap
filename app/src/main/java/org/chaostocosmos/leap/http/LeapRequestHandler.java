package org.chaostocosmos.leap.http;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.common.DateUtils;
import org.chaostocosmos.leap.http.common.UtilBox;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.context.User;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.enums.HOST_STATUS;
import org.chaostocosmos.leap.http.resource.Resource;
import org.chaostocosmos.leap.http.resource.ResourceHelper;
import org.chaostocosmos.leap.http.resource.TemplateBuilder;
import org.chaostocosmos.leap.http.security.SecurityManager;
import org.chaostocosmos.leap.http.session.Session;
import org.chaostocosmos.leap.http.session.SessionManager;

/**
 * Client request handing object
 * This object is main process of HTTP request from client.
 * It has server, session, service, security managing and authenticating credential information
 * And processing various attributes of web request.
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
     * Service manager object
     */
    ServiceManager serviceManager;

    /**
     * Session manager object
     */
    SessionManager sessionManager;

    /**
     * Security manager object
     */
    SecurityManager securityManager;

    /**
     * Client socket
     */
    Socket client;

    /**
     * Hosts
     */
    Host<?> host;

    /**
     * Constructor with HeapHttpServer, root direcotry, client socket and host object
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
        this.serviceManager = httpServer.getServiceManager(); 
        this.sessionManager = httpServer.getSessionManager();
        this.securityManager = httpServer.getSecurityManager();
    }

    @Override
    public void run() {
        HttpTransfer httpTransfer = null;
        try {
            httpTransfer = HttpTransferBuilder.buildHttpTransfer(httpServer.getHostId(), this.client);
            Request request = httpTransfer.getRequest();
            Response response = httpTransfer.getResponse();
            Host<?> host = httpTransfer.getHost();
            
            //Put requested host to request header Map for ip filter
            request.getReqHeader().put("@Client", host.getHost());

            if(this.host.getHostStatus() == HOST_STATUS.STARTED) {
                this.host.setHostStatus(HOST_STATUS.RUNNING);
                HTTPException httpe = new HTTPException(RES_CODE.RES401, "Server host started for the first time.");
                httpe.addHeader("WWW-Authenticate", "Basic");
                throw httpe;
            }    
            final Session session = this.sessionManager.getSessionCreateIfNotExists(request);
            this.host.getLogger().debug("[SESSION] ID: "+session.getId()+"  Authenticated: "+session.isAuthenticated()+"  New Session: "+session.isNew()+"  Creation Time: "+session.getCreationTime()+"  Last Access Date: "+new Date(session.getLastAccessedTime()));
            if(DateUtils.getMillis() > session.getLastAccessedTime() + session.getMaxInactiveIntervalSecond() * 1000L) {
                this.sessionManager.removeSession(session);
                HTTPException httpe = new HTTPException(RES_CODE.RES401, "  Session timeout: "+host.getSessionTimeout()+" sec.  Current Date: "+new Date(DateUtils.getMillis())+"  Timeout Date: "+new Date(session.getLastAccessedTime() + session.getMaxInactiveIntervalSecond() * 1000L));
                httpe.addHeader("WWW-Authenticate", "Basic");
                throw httpe;
            }
            final String authorization = request.getReqHeader().get("Authorization");
            if(!session.isAuthenticated()) {
                User user = null;                
                if(authorization != null) {
                    user = this.securityManager.loginBasicAuth(authorization);
                    if(user != null) {
                        session.setAuthenticated(true);                        
                        session.setNew(false);
                        user.setSession(session);
                        response.addSetCookie(Constants.SESSION_ID_KEY, session.getId());
                        //response.addSetCookie("Max-Age", String.valueOf(host.<Integer> getMaxAge()));
                        //response.addSetCookie("Expires", DateUtils.getDateLocalAddedOffset(System.currentTimeMillis() + this.host.<Integer>getExpires() * (int) UNIT.DY.getUnit()));
                    } else {
                        HTTPException httpe = new HTTPException(RES_CODE.RES401, "Authorization is fail. Please login.");
                        httpe.addHeader("WWW-Authenticate", "Basic");
                        throw httpe;    
                    }                    
                }
            }
            //Create service holder
            ServiceHolder serviceHolder = serviceManager.createServiceHolder(request.getContextPath());
            //If client request context path in Services.
            if (serviceHolder != null) {
                // Request method validation
                if(serviceHolder.getRequestType() != request.getRequestType()) {
                    throw new HTTPException(RES_CODE.RES405, "Not supported: "+request.getRequestType().name());
                } else {
                    // Do requested service to execute by cloned service of request
                    response = ServiceInvoker.invokeServiceMethod(serviceHolder, httpTransfer);
                }
            } else { // When client request static resources
                if(request.getRequestType() != REQUEST_TYPE.GET) {
                    throw new HTTPException(RES_CODE.RES405, "Static content can't be provided by "+request.getRequestType().name());
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
                        if(resource == null) {
                            host.getResource().addResource(resourcePath);
                            resource = host.getResource().getResource(resourcePath);
                        }
                        if(resource != null) {
                            if(resource.isNode()) {
                                String body = TemplateBuilder.buildResourceHtml(request.getContextPath(), host);
                                String mimeType = MIME_TYPE.TEXT_HTML.mimeType();
                                response.setResponseCode(RES_CODE.RES200.code());
                                response.addHeader("Content-Type", mimeType+"; charset="+host.charset());
                                response.setBody(body);
                            } else {
                                String mimeType = UtilBox.probeContentType(resourcePath);
                                if(mimeType == null) {
                                    mimeType = MIME_TYPE.APPLICATION_OCTET_STREAM.mimeType();
                                }
                                response.setResponseCode(RES_CODE.RES200.code());
                                response.addHeader("Content-Type", mimeType);
                                response.setBody(resource.getBytes());
                                this.host.getLogger().debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);    
                            }
                        } else {
                            throw new HTTPException(RES_CODE.RES403, request.getContextPath());
                        }
                    } else {
                        throw new HTTPException(RES_CODE.RES404, request.getContextPath());
                    }
                }
            }                 
            if(!httpTransfer.isClosed()) {
                //Send response to client
                httpTransfer.sendResponse(response);
            }
        } catch(SocketTimeoutException e) {
            this.host.getLogger().error("[SOCKET TIME OUT] Client socket timeout occurred.");
        } catch(Exception e) {           
            e.printStackTrace(); 
            try {
                if(!httpTransfer.isClosed()) {                   
                    processError(httpTransfer, e);
                }
            } catch (Exception ex) {                
                ex.initCause(e);
                this.host.getLogger().error(e.getMessage(), ex);
            }            
        } finally {
            if(httpTransfer != null) {
                httpTransfer.close();
            }            
        }
    }

    /**
     * Process error
     * @param httpTransfer
     * @param redirectionURL
     * @param error
     * @return
     * @throws Exception
     */
    public void processError(HttpTransfer httpTransfer, Exception error) throws Exception {        
        Throwable throwable = getCaused(httpTransfer.getHost().getHostId(), error);
        throwable = throwable == null ? error : throwable;
        int resCode = -1;
        MSG_TYPE msgType = null;
        String message = throwable.getMessage();
        String hostId = httpTransfer.getHost().getHostId();
        Map<String, List<String>> headers = new HashMap<>();
        if(throwable instanceof HTTPException) {
            HTTPException e = (HTTPException)throwable;
            headers.putAll(e.getHeaders());
            resCode = e.code();
            msgType = e.getMessageType();
            if(Context.getHost(hostId).<Boolean> getErrorDetails()) {
                message += e.getMessage();
            }
        } else {
            resCode = RES_CODE.RES500.code();
            
        }
        if(!Context.getHosts().isExistHostname(hostId)) {
            hostId = Context.getHosts().getDefaultHost().getHostId();
        }
        //throwable.printStackTrace();
        System.out.println(msgType+"  "+resCode+"  "+message+"  "+throwable);
        String body = TemplateBuilder.buildErrorHtml(Context.getHost(hostId), msgType, resCode, message);
        httpTransfer.sendResponse(hostId, resCode, headers, body);
    }

    /**
     * Get caused error
     * @param e
     * @return
     */
    public Throwable getCaused(String host, Throwable e) {        
        int level = 0;
        do {
            if(e instanceof HTTPException || e == null) {
                break;
            }
            e = e.getCause();
            level++;
        } while(level < 5);
        this.host.getLogger().debug("[FOUND CAUSED] "+e);
        return e;   
    }
}



