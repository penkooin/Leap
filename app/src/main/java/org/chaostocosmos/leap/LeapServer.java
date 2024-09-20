package org.chaostocosmos.leap;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.ClassUtils;
import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.common.DateUtils;
import org.chaostocosmos.leap.common.Filtering;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.common.RedirectHostSelection;
import org.chaostocosmos.leap.common.TIME;
import org.chaostocosmos.leap.common.ThreadPoolManager;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.REQUEST_LINE;
import org.chaostocosmos.leap.enums.STATUS;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.http.HttpsServerSocketFactory;
import org.chaostocosmos.leap.http.RedirectException;
import org.chaostocosmos.leap.manager.ResourceManager;
import org.chaostocosmos.leap.manager.SecurityManager;
import org.chaostocosmos.leap.manager.ServiceManager;
import org.chaostocosmos.leap.manager.SessionManager;
import org.chaostocosmos.leap.resource.LeapURLClassLoader;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.session.Session;

import ch.qos.logback.classic.Logger;

/**
 * HttpServer object
 * 
 * @author 9ins
 * @since 2021.09.15
 */
public class LeapServer extends Thread {

    /**
     * logger
     */
    Logger logger = LoggerFactory.getLogger(Context.get().hosts().getDefaultHost().getHostId());

    /**
     * Whether default host
     */
    boolean isDefaultHost;

    /**
     * Protocol
     */
    PROTOCOL protocol;

    /**
     * InetSocketAddress
     */
    InetSocketAddress inetSocketAddress;

    /**
     * backlog
     */
    int backlog;

    /**
     * document root
     */
    Path docroot;

    /**
     * leap home path
     */
    Path homePath;

    /**
     * Hosts
     */
    Host<?> host;

    /**
     * Server socket
     */
    ServerSocket server;

    /**
     * Redirect host object
     */
    RedirectHostSelection redirectHostSelection;

    /**
     * IP filtering object
     */
    Filtering ipAllowedFilters, ipForbiddenFilters; 

    /**
     * Server started flag
     */
    boolean started;

    /**
     * SessionManager object
     */
    SessionManager sessionManager;

    /**
     * servlet loading & managing
     */
    ServiceManager serviceManager;

    /**
     * User manager object
     */
    SecurityManager securityManager;

    /**
     * ResourcesModel object
     */
    ResourcesModel resourcesModel;

    /**
     * Default constructor 
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public LeapServer() throws IOException, InterruptedException, URISyntaxException, NotSupportedException {
        this(Constants.DEFAULT_HOME_PATH);
    }

    /**
     * Construct with home path
     * 
     * @param homePath
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public LeapServer(Path homePath) throws IOException, InterruptedException, URISyntaxException, NotSupportedException {
        this(homePath, Context.get().hosts().getHost(Context.get().hosts().getDefaultHost().getHostId()), ClassUtils.getClassLoader());
    }

    /**
     * Construct with Context object
     * 
     * @param homePath
     * @param host
     * @param classLoader
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public LeapServer(Path homePath, Host<?> host, LeapURLClassLoader classLoader) throws IOException, InterruptedException, URISyntaxException, NotSupportedException {
        this(true, Context.get().getHome(), host.getDocroot(), PROTOCOL.valueOf(host.getProtocol()), new InetSocketAddress(InetAddress.getByName(host.getHost()), host.getPort()), host.getBackLog(), host, classLoader);
    }

    /**
     * Constructor with configuration file Path
     * 
     * @param isDefaultHost
     * @param homePath
     * @param docroot
     * @param protocol
     * @param inetSocketAddress
     * @param backlog
     * @param host
     * @param classLoader
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     * @throws NotSupportedException
     */
    public LeapServer(boolean isDefaultHost, Path homePath, Path docroot, PROTOCOL protocol, InetSocketAddress inetSocketAddress, int backlog, Host<?> host, LeapURLClassLoader classLoader) throws IOException, URISyntaxException, NotSupportedException {
        this.host = host;
        this.host.setHostStatus(STATUS.SETUP);
        this.isDefaultHost = true;
        this.homePath = homePath;
        this.protocol = protocol;
        this.backlog = backlog;
        this.docroot = docroot;
        this.inetSocketAddress = inetSocketAddress;
        this.ipAllowedFilters = host.getIpAllowedFiltering();
        this.ipForbiddenFilters = host.getIpForbiddenFiltering();
        this.redirectHostSelection = new RedirectHostSelection(this.host.getTrafficRedirects());
        this.sessionManager = new SessionManager(host);
        this.securityManager = new SecurityManager(host);
        this.serviceManager = new ServiceManager(host, this.securityManager, this.sessionManager, this.resourcesModel, classLoader);
        this.resourcesModel = ResourceManager.get(host.getHostId());
    }

    /**
     * Get service host ID
     * @return
     */
    public String getHostId() {
        return this.host.getHostId();
    }

    /**
     * Get service port
     * @return
     */
    public int getPort() {
        return this.inetSocketAddress.getPort();
    }

    /**
     * Get servlet loader object
     * @return
     */
    protected ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    /**
     * Get session manager
     * @return
     */
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    /**
     * Get user manager object
     * @return
     */
    protected SecurityManager getSecurityManager() {
        return this.securityManager;
    }

    /**
     * Get root directory
     * @return
     */
    public Path getDocroot() {
        return this.docroot;
    }

    @Override
    public void run() {        
        try {
            this.host.setHostStatus(STATUS.STARTING);
            if(!this.protocol.isSecured()) {
                this.server = new ServerSocket();
                System.out.println(this.inetSocketAddress.toString());
                this.server.bind(this.inetSocketAddress, this.backlog);
                this.logger.info("[HTTP SERVER START] Address: " + this.inetSocketAddress.toString());
            } else {
                File keyStore = new File(this.host.<String> getKeyStore());
                String passphrase = this.host.getPassphrase();
                String sslProtocol = this.host.getEncryptionMethod();
                this.server = HttpsServerSocketFactory.getSSLServerSocket(keyStore, passphrase, sslProtocol, this.inetSocketAddress, this.backlog);
                this.logger.info("[HTTPS SERVER START] Address: "+this.inetSocketAddress.toString()+"  Protocol: "+sslProtocol+"  KeyStore: "+keyStore.getName()+"  Supported Protocol: "+Arrays.toString(((SSLServerSocket)server).getSupportedProtocols())+"  KeyStore: "+keyStore.getName());
            }
            this.host.setHostStatus(STATUS.STARTED);
            while (this.host.getHostStatus() == STATUS.STARTED || this.host.getHostStatus() == STATUS.RUNNING) {                                 
                //Waiting for client connection
                Socket client = null;
                HttpTransfer httpTransfer = null;
                try {
                    client = this.server.accept();
                    client.setSoTimeout(this.host.getConnectionTimeout());
                    this.logger.info("[CONNECTED] CLIENT CONNECTED: "+client.getInetAddress().toString());
                    //Create HttpTransfer object
                    httpTransfer = new HttpTransfer(this.host, client);
                    Map<REQUEST_LINE, Object> requestLine = httpTransfer.getRequestLine();                    
                    Map<String, Object> headers = httpTransfer.getRequestHeaders();
                    Map<String, String> cookies = httpTransfer.getRequestCookies();
                    client.setSoTimeout(this.host.getConnectionTimeout());
                    //connection.setSoLinger(false, 1);
                    String hostName = headers.get("Host") != null ? headers.get("Host").toString() : "unknown";
                    String url = requestLine.get(REQUEST_LINE.PROTOCOL) +"://"+ hostName + requestLine.get(REQUEST_LINE.PATH);
                    if(!headers.containsKey("Range") && !this.securityManager.checkRequestAttack(client.getInetAddress().getHostAddress(), url)) {
                        this.host.getLogger().warn("[CLIENT BLOCKED] Too many requested client blocking: "+client.getInetAddress().getHostAddress());
                        throw new LeapException(HTTP.RES429, hostName+" requested too many on short period!!!");
                    }
                    String ipAddress = client.getInetAddress().getHostAddress();
                    if(this.ipAllowedFilters.include(ipAddress) || this.ipForbiddenFilters.exclude(ipAddress)) {
                        int queueSize = ThreadPoolManager.get().getQueuedTaskCount();
                        if(queueSize < Context.get().server().<Integer> getThreadQueueSize()) {
                            Session session = this.sessionManager.getSessionCreateIfNotExists(cookies.get(Constants.SESSION_ID_KEY));
                            if(host.<Boolean>isSessionApply()) {                
                                host.getLogger().debug("[SESSION] ID: "+session.getId()+"  LOGIN: "+session.isAuthenticated()+"  NEW SESSION: "+session.isNew()+"  CREATION TIME: "+new Date(session.getCreationTime())+"  LAST ACCESS: "+new Date(session.getLastAccessedTime()));
                                try {
                                    if(!session.isNew() && DateUtils.getMillis() > session.getLastAccessedTime() + TIME.SECOND.duration(this.host.<Integer> getSessionTimeoutSeconds(), TimeUnit.MILLISECONDS)) {
                                        host.getLogger().info("[SESSION] SESSION TIMEOUT: "+host.getSessionTimeoutSeconds()+" SEC.  DATE: "+new Date(DateUtils.getMillis())+"  TIMEOUT: "+new Date(session.getLastAccessedTime() + session.getMaxInactiveIntervalSecond() * 1000L));
                                        throw new LeapException(HTTP.RES500, "Session timeout is occurred.");
                                    }
                                    session.setNew(false);
                                    session.setLastAccessedTime(DateUtils.getMillis());
                                    session.setSessionToResponse(httpTransfer.getResponse());
                                } catch(LeapException e) {
                                    this.sessionManager.removeSession(session);
                                    httpTransfer.getResponse().addSetCookie("Cache-Control", "no-store");
                                    httpTransfer.getResponse().addSetCookie("X-Custom-Header", "SomeValue");
                                    throw e;
                                }
                            }
                            httpTransfer.setSession(session);
                            this.logger.info("[CLIENT DETECTED] REQUEST ACCEPTED. SUBMIT TO THREADPOOL. "+ipAddress+":"+client.getPort()+"  QUEUE SIZE: "+queueSize);                        
                            ThreadPoolManager.get().execute(new LeapHandler(this, this.docroot, this.host, httpTransfer));
                        } else {
                            String redirectHost = redirectHostSelection.getSelectedHost();
                            String redirectUrl = redirectHost + requestLine.get(REQUEST_LINE.PATH);
                            this.logger.info("[CLIENT DETECTED] THREADPOOL LIMIT REACHED: "+queueSize+".  REDIRECT TO URL: "+redirectUrl);
                            throw new RedirectException(redirectUrl);
                        }
                    } else {
                        throw new LeapException(HTTP.RES406, "[CLIENT CANCELED] REQUEST CLIENT IP ADDRESS IS NO ALLOWED: "+ipAddress.toString());
                    }
                } catch(SocketTimeoutException | NegativeArraySizeException e) {
                    host.getLogger().error("[SOCKET TIME OUT] SOCKET TIMEOUT OCCURED.");
                    if(client != null) {
                        client.close();
                    }
                } catch(LeapException le) {
                    if(httpTransfer != null) {
                        httpTransfer.processError(le);
                    }                    
                }                
            }
        } catch(Exception e) {
            this.logger.error(e.getMessage(), e);
        } finally {
            stopServer();
        }
    }

    /**
     * Stop server 
     */
    public void stopServer() {
        try {
            this.server.close();
        } catch (IOException e) {
            this.host.getLogger().error(e.getMessage(), e);
        }
        this.host.setHostStatus(STATUS.TERMINATED);
        this.host.getLogger().info("[SERVER TERMINATED] "+this.host.getHost()+" Server is terminated...");
    }

    /**
     * Whether server started
     * @return
     */
    public boolean isClosed() {
        return this.server == null ? true : this.server.isClosed();
    }
}