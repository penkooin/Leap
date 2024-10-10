package org.chaostocosmos.leap;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.RedirectHostSelection;
import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.data.Filtering;
import org.chaostocosmos.leap.common.enums.TIME;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.thread.ThreadPoolManager;
import org.chaostocosmos.leap.common.utils.DateUtils;
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
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;
import org.chaostocosmos.leap.security.SecurityManager;
import org.chaostocosmos.leap.service.mgmt.ServiceManager;
import org.chaostocosmos.leap.session.Session;
import org.chaostocosmos.leap.session.SessionManager;

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
    Logger logger;

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
    ServiceManager<?, ?> serviceManager;

    /**
     * User manager object
     */
    SecurityManager securityManager;

    /**
     * ResourcesModel object
     */
    ResourcesWatcherModel resourcesModel;

    /**
     * Construct with Host
     * @param host
     * @throws UnknownHostException 
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public LeapServer(Host<?> host) throws  UnknownHostException, IOException, URISyntaxException, NotSupportedException {
        this(host.getHomePath(), host);
    }

    /**
     * Construct with Context object
     * @param homePath
     * @param host
     * @throws UnknownHostException 
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public LeapServer(Path homePath, Host<?> host) throws UnknownHostException, IOException, URISyntaxException, NotSupportedException {
        this(
            Context.get().getHome(), 
            host.getDocroot(), 
            host.getProtocol(), 
            new InetSocketAddress(InetAddress.getByName(host.getHost()), 
            host.getPort()), 
            host
            );
    }

    /**
     * Constructor
     * @param homePath
     * @param docroot
     * @param protocol
     * @param inetSocketAddress
     * @param host
     * @throws NotSupportedException 
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public LeapServer(Path homePath, Path docroot, PROTOCOL protocol, InetSocketAddress inetSocketAddress, Host<?> host) throws IOException, URISyntaxException, NotSupportedException {
        this.host = host;
        this.logger = this.host.getLogger();
        this.host.setHostStatus(STATUS.SETUP);
        this.isDefaultHost = true;
        this.homePath = homePath;
        this.protocol = protocol;
        this.docroot = docroot;
        this.inetSocketAddress = inetSocketAddress;
        this.ipAllowedFilters = host.getIpAllowedFiltering();
        this.ipForbiddenFilters = host.getIpForbiddenFiltering();        
        this.redirectHostSelection = new RedirectHostSelection(Context.get().server().getRedirectLBRatio());
        this.sessionManager = new SessionManager(host);
        this.securityManager = new SecurityManager(host);
        this.serviceManager = new ServiceManager<>(host, this.securityManager, this.sessionManager, this.resourcesModel);
        this.resourcesModel = this.host.getResource();
    }

    /**
     * Get service host ID
     * @return
     */
    public String getHostId() {
        return this.host.getId();
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
    protected ServiceManager<?, ?> getServiceManager() {
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
            int backlog = this.host.<Integer> getValue("network.backlog");
            if(!this.protocol.isSecured()) {
                this.server = new ServerSocket();
                //System.out.println(this.inetSocketAddress.toString());
                this.logger.info("[HTTP SERVER START] Address: " + this.inetSocketAddress.toString()+" Backlog: "+backlog);
                this.server.bind(this.inetSocketAddress, backlog);
            } else {
                File keyStore = new File(this.host.getKeyStore());
                String passphrase = this.host.getPassphrase();
                String sslProtocol = this.host.getEncryptionMethod();
                this.logger.info("[HTTPS SERVER START] Address: "+this.inetSocketAddress.toString()+"  Protocol: "+sslProtocol+"  KeyStore: "+keyStore.getName()+"  Supported Protocol: "+Arrays.toString(((SSLServerSocket)server).getSupportedProtocols())+"  KeyStore: "+keyStore.getName());
                this.server = HttpsServerSocketFactory.getSSLServerSocket(keyStore, passphrase, sslProtocol, this.inetSocketAddress, backlog);
            }
            this.host.setHostStatus(STATUS.STARTED);
            while (this.host.getHostStatus() == STATUS.STARTED || this.host.getHostStatus() == STATUS.RUNNING) {
                //Waiting for client connection
                Socket socket = null;
                HttpTransfer<?, ?> httpTransfer = null;
                try {
                    int soTimeout = this.host.<Integer> getValue("network.so-timeout");
                    boolean keepAlive = this.host.<Boolean> getValue("network.keep-alive");
                    boolean oobInline = this.host.<Boolean> getValue("network.OOB-inline");
                    boolean soLinger = this.host.<Boolean> getValue("network.so-linger");
                    int soLingerTimeout = this.host.<Integer> getValue("network.so-linger-timeout");
                    boolean tcpNoDelay = this.host.<Boolean> getValue("network.tcp-no-delay");
                    int receiveBufferSize = this.host.<Integer> getValue("network.receive-buffer-size");
                    int sendBufferSize = this.host.<Integer> getValue("network.send-buffer-size");

                    //Waiting for client
                    socket = this.server.accept();
                    this.server.setReuseAddress(isDefaultHost);
                    this.host.setHostStatus(STATUS.RUNNING);

                    this.logger.info("SOCKET BUFFER INFO - so-timeout:"+soTimeout+"  receive-buffer-size: "+receiveBufferSize+"  send-buffer-size: "+sendBufferSize);
                    this.logger.info("SOCKET CONF INFO - keep-alive: "+socket.getKeepAlive()+"  OOB-inline: "+socket.getOOBInline()+"  so-linger: "+socket.getSoLinger()+"  tcp-nodelay: "+socket.getTcpNoDelay());
                    // socket.setSoTimeout(10000);
                    // socket.setKeepAlive(true);
                    // socket.setOOBInline(oobInline);
                    // socket.setSoLinger(soLinger, soLingerTimeout);
                    // socket.setTcpNoDelay(tcpNoDelay);
                    socket.setReceiveBufferSize(receiveBufferSize);
                    socket.setSendBufferSize(sendBufferSize);
                    this.logger.info("[CONNECTED] CLIENT CONNECTED: "+socket.getInetAddress().toString());

                    //Create HttpTransfer object
                    httpTransfer = new HttpTransfer<> (this.host, socket);
                    Map<REQUEST_LINE, String> requestLine = httpTransfer.getRequestLine();
                    Map<String, String> headers = httpTransfer.getRequestHeaders();
                    Map<String, String> cookies = httpTransfer.getRequestCookies();

                    //connection.setSoLinger(false, 1);
                    String hostName = headers.get("Host") != null ? headers.get("Host").toString() : "unknown";
                    String url = requestLine.get(REQUEST_LINE.PROTOCOL) +"://"+ hostName + requestLine.get(REQUEST_LINE.CONTEXT);
                    if(!headers.containsKey("Range") && !this.securityManager.checkRequestAttack(socket.getInetAddress().getHostAddress(), url)) {
                        this.host.getLogger().warn("[CLIENT BLOCKED] Too many requested client blocking: "+socket.getInetAddress().getHostAddress());
                        throw new LeapException(HTTP.RES429, hostName+" requested too many on short period !!!");
                    }

                    String ipAddress = socket.getInetAddress().getHostAddress();
                    //Check client IP address is valid
                    if(this.ipAllowedFilters.include(ipAddress) || this.ipForbiddenFilters.exclude(ipAddress)) {
                        int queueSize = ThreadPoolManager.get().getQueuedTaskCount();
                        if(queueSize < Context.get().server().getThreadQueueSize()) {
                            Session session = this.sessionManager.getSessionCreateIfNotExists(cookies.get(Constants.SESSION_ID_KEY));
                            if(host.<Boolean> getValue("global.session.apply")) {
                                host.getLogger().debug("[SESSION] ID: "+session.getId()+"  LOGIN: "+session.isAuthenticated()+"  NEW SESSION: "+session.isNew()+"  CREATION TIME: "+new Date(session.getCreationTime())+"  LAST ACCESS: "+new Date(session.getLastAccessedTime()));
                                try {
                                    if(!session.isNew() && DateUtils.getMillis() > session.getLastAccessedTime() + TIME.SECOND.duration(host.<Integer> getValue("global.session.timeout-seconds"), TimeUnit.MILLISECONDS)) {
                                        host.getLogger().info("[SESSION] SESSION TIMEOUT: "+host.<Integer> getValue("global.session.timeout-seconds")+" SEC.  DATE: "+new Date(DateUtils.getMillis())+"  TIMEOUT: "+new Date(session.getLastAccessedTime() + session.getMaxInactiveIntervalSecond() * 1000L));
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
                            this.logger.info("[CLIENT DETECTED] REQUEST ACCEPTED. SUBMIT TO THREADPOOL. "+ipAddress+":"+socket.getPort()+"  QUEUE SIZE: "+queueSize);                        
                            LeapHandler<?, ?> handler = new LeapHandler<> (this, this.docroot, this.host, httpTransfer);
                            ThreadPoolManager.get().execute(handler);
                        } else {
                            String redirectHost = redirectHostSelection.getSelectedHost();
                            String redirectUrl = redirectHost + requestLine.get(REQUEST_LINE.CONTEXT);
                            this.logger.info("[CLIENT DETECTED] THREADPOOL LIMIT REACHED: "+queueSize+".  REDIRECT TO URL: "+redirectUrl);
                            throw new RedirectException(redirectUrl);
                        }
                    } else {
                        throw new LeapException(HTTP.RES406, "[CLIENT CANCELED] REQUEST CLIENT IP ADDRESS IS NO ALLOWED: "+ipAddress.toString());
                    }
                } catch(SocketTimeoutException | NegativeArraySizeException e) {
                    host.getLogger().error("[SOCKET TIME OUT] SOCKET TIMEOUT OCCURED.");
                    if(socket != null) {
                        socket.close();
                    }
                } catch(LeapException e) {                    
                    if(httpTransfer != null) {
                        httpTransfer.processError(e);
                    } else {
                        e.printStackTrace();
                    }
                }                
            }
        } catch(Exception e) {
            this.host.setHostStatus(STATUS.TERMINATED);
            this.logger.throwable(e);
            try {
                stopServer();
            } catch (IOException | InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    /**
     * Stop server 
     * @throws IOException 
     * @throws InterruptedException 
     */
    public void stopServer() throws IOException, InterruptedException {
        this.host.getLogger().info("[SERVER TERMINATED] "+this.host.getHost()+" Server is terminated...");
        this.server.close();
        this.interrupt();
        this.join();
    }

    /**
     * Whether server started
     * @return
     */
    public boolean isClosed() {
        return this.server == null ? true : this.server.isClosed();
    }
}