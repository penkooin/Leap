package org.chaostocosmos.leap;

import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.common.NetworkInterfaceManager;
import org.chaostocosmos.leap.common.data.Filtering;
import org.chaostocosmos.leap.common.utils.UtilBox;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.enums.TEMPLATE;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.resource.Resource;
import org.chaostocosmos.leap.resource.ResourceHelper;
import org.chaostocosmos.leap.security.UserCredentials;
import org.chaostocosmos.leap.service.mgmt.ServiceHolder;
import org.chaostocosmos.leap.service.mgmt.ServiceInvoker;
import org.chaostocosmos.leap.service.mgmt.ServiceManager;
import org.chaostocosmos.leap.session.Session;
import org.chaostocosmos.leap.session.SessionManager;
import org.chaostocosmos.leap.spring.SpringJPAManager;

/**
 * Client request handing object
 * This object is main process of HTTP request from client.
 * It has server, session, service, security managing and authenticating credential information
 * And processing various attributes of web request.
 * 
 * @author 9ins
 * @since 2021.09.16
 */
public class LeapHandler<T, R> implements Runnable {

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
    ServiceManager<?, ?> serviceManager;

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
    HttpTransfer<T, R> httpTransfer;

    /**
     * Hosts
     */
    Host<?> host;

    /**
     * Allowed context filtering
     */
    Filtering allowedFiltering;

    /**
     * Constructor with HeapHttpServer, root direcotry, client socket and host object
     * @param httpServer
     * @param rootPath
     * @param host
     * @param httpTransfer
     */
    public LeapHandler(LeapServer httpServer, Path LEAP_HOME, Host<?> host, HttpTransfer<T, R> httpTransfer) {
        this.httpServer = httpServer;
        this.LEAP_HOME = LEAP_HOME;
        this.httpTransfer = httpTransfer;
        this.host = host;
        this.allowedFiltering = host.getAllowedPathFiltering();
        this.serviceManager = httpServer.getServiceManager(); 
        this.sessionManager = httpServer.getSessionManager(); 
        this.securityManager = httpServer.getSecurityManager();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            //Create HttpRequest object
            HttpRequest<T> request = this.httpTransfer.getRequest();
            //Create HttpResponse object
            HttpResponse<R> response = this.httpTransfer.getResponse();
            //Get Host object from HttpTransfer object
            Host<?> host = this.httpTransfer.getHost();
            
            //Put requested host to request header Map for ip filter
            request.getHeaders().put("@Client", host.getHost());
            Session session = this.httpTransfer.getSession();
            boolean isLocalRequest = false;
            if(request.getHeader("mac-address") != null) {
                String mac = request.getHeader("mac-address").toString();
                if(NetworkInterfaceManager.isExistMacAddress(mac)) {
                    isLocalRequest = true;
                }
            }
            System.out.println(this.allowedFiltering.exclude(request.getContextPath()));
            if(this.allowedFiltering.exclude(request.getContextPath())) {
                throw new LeapException(HTTP.RES403, "Requested resource is restricted: "+request.getContextPath());
            }
            if(((host.isAuthentication() || !isLocalRequest)) && (this.allowedFiltering.exclude(request.getContextPath()))) {                
                try {
                    //if((session != null && !session.isAuthenticated()) && request.getCookie("__auth-trial") == null || !request.getCookie("__auth-trial").equals("1")) {
                    if((session != null && !session.isAuthenticated())) {
                        UserCredentials userCredentials = null;                        
                        if(request.getParameter("username") != null && request.getParameter("password") != null) {
                            String username = request.getParameter("username").toString();
                            String password = request.getParameter("password").toString();
                            userCredentials = this.securityManager.login(username, password);
                            if(userCredentials == null) {
                                response.addHeader("Location", "/login");                                
                            }
                        } else {
                            String authorization = request.getHeaders().get("Authorization");
                            userCredentials = this.securityManager.authenticate(authorization);
                            if(userCredentials == null) {
                                response.addSetCookie("__auth-trial", "1");
                                response.addHeader("WWW-Authenticate", "Basic");
                            }
                        }
                        if(userCredentials == null) {
                            throw new LeapException(HTTP.RES401, "[AUTH] AUTHENTICATION FAIL ");
                        }
                        if(session != null) {
                            session.setAuthenticated(true);
                            userCredentials.setSession(session);
                        }
                    }
                } catch(Exception e) {
                    if(session != null) {
                        session.setAuthenticated(false);
                    }
                    this.sessionManager.removeSession(session);
                    throw e;
                }
            }
            //Create service holder
            ServiceHolder<?, ?> serviceHolder = serviceManager.getServiceHolder(request.getContextPath());
            //If client request context path in Services.
            if (serviceHolder != null) {
                if(Context.get().server().isSupportSpringJPA()) {
                    SpringJPAManager.get().injectToAutoWired(serviceHolder.getServiceModel());
                }
                // Do requested service to execute by cloned service of request
                response = ServiceInvoker.invokeServiceMethod(serviceHolder, this.httpTransfer);
            } else { 
                // When client request static resources
                if(request.getRequestType() != REQUEST.GET) {
                    throw new LeapException(HTTP.RES405, "Static contents can't be provided by "+request.getRequestType().name());
                }
                Path resourcePath = ResourceHelper.getResourcePath(request);
                if(request.getContextPath().equals("/")) {
                    String body = httpTransfer.resolvePlaceHolder(TEMPLATE.INDEX.loadTemplatePage(host.getId()), Map.of("serverName", host.getHost()));
                    response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType()+"; charset="+host.charset());
                    response.setBody((R) body.getBytes());
                    response.setResponseCode(HTTP.RES200.code());
                } else {
                    if(resourcePath.toFile().exists() && !host.getResource().exists(resourcePath)) {
                        host.getResource().addResource(resourcePath);
                    }
                    // Get requested resource
                    Resource resource = host.getResource().getResource(resourcePath);
                    if(resource != null) {
                        if(resource.isNode()) {
                            String body = httpTransfer.resolvePlaceHolder(TEMPLATE.DIRECTORY.loadTemplatePage(host.getId()), Map.of("@serverName", host.getHost(), "@directory", host.buildDirectoryJson(request.getContextPath())));
                            String mimeType = MIME.TEXT_HTML.mimeType();
                            response.setResponseCode(HTTP.RES200.code());
                            response.addHeader("Content-Type", mimeType+"; Charset="+host.charset());
                            response.setBody((R) body.getBytes());
                        } else {
                            String mimeType = UtilBox.probeContentType(resourcePath);
                            if(mimeType == null) {
                                mimeType = MIME.APPLICATION_OCTET_STREAM.mimeType();
                            }
                            response.setResponseCode(HTTP.RES200.code());
                            response.addHeader("Content-Type", mimeType);                            
                            response.setBody((R) resource.getBytes());
                            this.host.getLogger().debug("DOWNLOAD RESOURCE MIME-TYPE: "+mimeType);
                        }
                    } else {
                        throw new LeapException(HTTP.RES404, "Specified resource not found: "+request.getContextPath());
                    }
                }
            } 
            // Send response to client
            this.httpTransfer.sendResponse(); 
        } catch(LeapException e) {        
            this.host.getLogger().throwable(e);
            if(e.getHTTP() == HTTP.LEAP900) {
                this.host.getLogger().info("[CONNECTION CLOSED BY CLIENT] Host: "+this.host.getId()+"  Client: "+this.httpTransfer.getSocket().getInetAddress().toString());
            } else {
                try {                
                    if(this.httpTransfer != null) {
                        this.httpTransfer.processError(e);
                    }
                } catch (Exception ex) {                
                    this.host.getLogger().throwable(ex);
                }
            }
        } catch(Exception e) {
            this.host.getLogger().throwable(e);
        } finally {
            this.httpTransfer.close();
        }
    }
}

