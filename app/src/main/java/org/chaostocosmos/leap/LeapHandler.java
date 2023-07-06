package org.chaostocosmos.leap;

import java.nio.file.Path;
import java.util.List;

import org.chaostocosmos.leap.common.UtilBox;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.ServiceHolder;
import org.chaostocosmos.leap.http.ServiceInvoker;
import org.chaostocosmos.leap.http.ServiceManager;
import org.chaostocosmos.leap.resource.Resource;
import org.chaostocosmos.leap.resource.ResourceHelper;
import org.chaostocosmos.leap.resource.TemplateBuilder;
import org.chaostocosmos.leap.security.UserCredentials;
import org.chaostocosmos.leap.session.Session;
import org.chaostocosmos.leap.session.SessionManager;

/**
 * Client request handing object
 * This object is main process of HTTP request from client.
 * It has server, session, service, security managing and authenticating credential information
 * And processing various attributes of web request.
 * 
 * @author 9ins
 * @since 2021.09.16
 */
public class LeapHandler implements Runnable {
    /**
     * Leap server home path
     */
    Path LEAP_HOME;

    /**
     * Server
     */
    LeapServer httpServer;

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
    org.chaostocosmos.leap.security.SecurityManager securityManager;

    /**
     * HttpTransfer instance
     */
    HttpTransfer httpTransfer;

    /**
     * Hosts
     */
    Host<?> host;

    /**
     * Constructor with HeapHttpServer, root direcotry, client socket and host object
     * @param httpServer
     * @param rootPath
     * @param host
     * @param httpTransfer
     */
    public LeapHandler(LeapServer httpServer, Path LEAP_HOME, Host<?> host, HttpTransfer httpTransfer) {
        this.httpServer = httpServer;
        this.LEAP_HOME = LEAP_HOME;
        this.httpTransfer = httpTransfer;
        this.host = host;
        this.serviceManager = httpServer.getServiceManager(); 
        this.sessionManager = httpServer.getSessionManager(); 
        this.securityManager = httpServer.getSecurityManager();
    }

    @Override
    public void run() {
        try {
            Request request = this.httpTransfer.getRequest();
            Response response = this.httpTransfer.getResponse();
            Host<?> host = this.httpTransfer.getHost();
            
            //Put requested host to request header Map for ip filter
            request.getReqHeader().put("@Client", host.getHost());
            System.out.println(Context.get().host("leap").getAuthentication()+"++++++++++++++++++++++++++++++++++++++++++++++++");

            Session session = this.httpTransfer.getSession();
            // if(host.isAuthentication()) {
            //     try {
            //         if((session != null && !session.isAuthenticated()) && request.getCookie("__auth-trial") == null || !request.getCookie("__auth-trial").equals("1")) {
            //             String authorization = request.getReqHeader().get("Authorization");
            //             UserCredentials userCredentials = this.securityManager.authenticate(authorization);
            //             if(userCredentials == null) {
            //                 response.addSetCookie("__auth-trial", "1");
            //                 throw new LeapException(HTTP.RES401, "[AUTH] AUTHENTICATION FAIL "+authorization);
            //             }
            //             session.setAuthenticated(true);
            //             userCredentials.setSession(session);
            //         }
            //     } catch(Exception e) {
            //         if(session != null) {
            //             session.setAuthenticated(false);
            //         }
            //         this.sessionManager.removeSession(session);
            //         throw e;
            //     }
            // }
            //Create service holder
            ServiceHolder serviceHolder = serviceManager.createServiceHolder(request.getContextPath());
            //If client request context path in Services.
            if (serviceHolder != null) {
                // Request method validation
                if(serviceHolder.getRequestType() != request.getRequestType()) {
                    throw new LeapException(HTTP.RES405, "Not supported: "+request.getRequestType().name());
                } else {
                    // Do requested service to execute by cloned service of request
                    response = ServiceInvoker.invokeServiceMethod(serviceHolder, this.httpTransfer);
                }
            } else { // When client request static resources
                if(request.getRequestType() != REQUEST.GET) {
                    throw new LeapException(HTTP.RES405, "Static content can't be provided by "+request.getRequestType().name());
                }
                Path resourcePath = ResourceHelper.getResourcePath(request);
                if(request.getContextPath().equals("/")) {
                    String body = TemplateBuilder.buildWelcomeResourceHtml("/", host);
                    response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType()+"; charset="+host.<String> charset());
                    response.setBody(body.getBytes(host.<String> charset()));
                    response.setResponseCode(HTTP.RES200.code());
                } else {
                    if(resourcePath.toFile().exists() && !host.getResource().exists(resourcePath)) {
                        host.getResource().addResource(resourcePath);
                    }
                    //Get requested resource
                    Resource resource = host.getResource().getResource(resourcePath);
                    if(resource != null) {
                        if(resource.isNode()) {
                            String body = TemplateBuilder.buildResourceHtml(request.getContextPath(), host);
                            String mimeType = MIME.TEXT_HTML.mimeType();
                            response.setResponseCode(HTTP.RES200.code());
                            response.addHeader("Content-Type", mimeType+"; Charset="+host.<String> charset());
                            response.setBody(body);
                        } else {
                            String mimeType = UtilBox.probeContentType(resourcePath);
                            if(mimeType == null) {
                                mimeType = MIME.APPLICATION_OCTET_STREAM.mimeType();
                            }
                            response.setResponseCode(HTTP.RES200.code());
                            response.addHeader("Content-Type", mimeType);
                            response.setBody(resource.getBytes());
                            this.host.getLogger().debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);
                        }
                    } else {
                        throw new LeapException(HTTP.RES403, "Specified resource not found: "+request.getContextPath());
                    }
                }
            } 
            // Send response to client
            this.httpTransfer.sendResponse();
        } catch(Exception le) {           
            try {                
                if(this.httpTransfer != null) {
                    this.httpTransfer.processError(le);
                }                
            } catch (Exception ex) {                
                this.host.getLogger().error(le.getMessage(), ex);
            }            
        }
    }
}



