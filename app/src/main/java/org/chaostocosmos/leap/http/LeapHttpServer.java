package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;

import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.RedirectHostSelection;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.Context;
import org.chaostocosmos.leap.http.resources.Hosts;
import org.chaostocosmos.leap.http.resources.HostsManager;
import org.chaostocosmos.leap.http.resources.Html;
import org.chaostocosmos.leap.http.resources.LeapURLClassLoader;
import org.chaostocosmos.leap.http.resources.ResourceMonitor;
import org.chaostocosmos.leap.http.services.ServiceManager;
import org.chaostocosmos.leap.http.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

/**
 * HttpServer object
 * 
 * @author 9ins
 * @since 2021.09.15
 */
public class LeapHttpServer extends Thread {
    /**
     * logger
     */
    Logger logger = (Logger)LoggerFactory.getLogger(Context.getDefaultHost());

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
    Hosts hosts;

    /**
     * Server socket
     */
    ServerSocket server;

    /**
     * servlet loading & managing
     */
    ServiceManager serviceManager;

    /**
     * Resource monitor
     */
    ResourceMonitor resourceMonitor;

    /**
     * thread pool
     */
    ThreadPoolExecutor threadpool;

    /**
     * Redirect host object
     */
    RedirectHostSelection redirectHostSelection;

    /**
     * IP filtering object
     */
    Filtering ipAllowedFilters, ipForbiddenFilters; 

    /**
     * Default constructor 
     * @throws UnknownHostException
     * @throws MalformedURLException
     */
    public LeapHttpServer() throws UnknownHostException, MalformedURLException {
        this(Constants.DEFAULT_HOME_PATH);
    }

    /**
     * Construct with home path
     * @param homePath
     * @throws UnknownHostException
     * @throws MalformedURLException
     */
    public LeapHttpServer(Path homePath) throws UnknownHostException, MalformedURLException {
        this(homePath, 
             HostsManager.get().getHosts(Context.getDefaultHost()), 
             new ThreadPoolExecutor(Context.getThreadPoolCoreSize(), 
                                    Context.getThreadPoolMaxSize(), 
                                    Context.getThreadPoolKeepAlive(), 
                                    TimeUnit.SECONDS, 
                                    new LinkedBlockingQueue<Runnable>()),
            new LeapURLClassLoader(HostsManager.get().getAllDynamicClasspathURLs()),
            null
        );
    }

    /**
     * Construct with Context object
     * @param homePath
     * @param hosts
     * @param threadpool 
     * @param classLoader
     * @param resourceMonitor
     * @throws UnknownHostException
     * @throws MalformedURLException
     */
    public LeapHttpServer(Path homePath, 
                          Hosts hosts, 
                          ThreadPoolExecutor threadpool, 
                          LeapURLClassLoader classLoader,
                          ResourceMonitor resourceMonitor
                          ) throws UnknownHostException {
        this(
            true,
            Context.getLeapHomePath(),
            hosts.getDocroot(),
            hosts.getProtocol(),
            new InetSocketAddress(InetAddress.getByName(hosts.getHost()), hosts.getPort()),
            Context.getBackLog(),
            threadpool,
            hosts,
            classLoader,
            resourceMonitor
        );
    }

    /**
     * Constructor with configuration file Path
     * @param isDefaultHost
     * @param homePath
     * @param docroot
     * @param protocol
     * @param inetSocketAddress
     * @param backlog
     * @param threadpool
     * @param hosts
     * @param classLoader
     * @param resourceMonitor
     */
    public LeapHttpServer(boolean isDefaultHost, 
                          Path homePath, 
                          Path docroot, 
                          PROTOCOL protocol, 
                          InetSocketAddress inetSocketAddress, 
                          int backlog, 
                          ThreadPoolExecutor threadpool,
                          Hosts hosts,
                          LeapURLClassLoader classLoader,
                          ResourceMonitor resourceMonitor
                          ) {
        this.isDefaultHost = true;
        this.homePath = homePath;
        this.protocol = protocol;
        this.backlog = backlog;
        this.docroot = docroot;
        this.hosts = hosts;
        this.threadpool = threadpool;
        this.inetSocketAddress = inetSocketAddress;
        this.resourceMonitor = resourceMonitor;
        this.ipAllowedFilters = new Filtering(Context.getAllowedIpFilter());
        this.ipForbiddenFilters = new Filtering(Context.getForbiddenIpfilter()); 
        this.redirectHostSelection = new RedirectHostSelection(Context.getLoadBalanceRedirects());
        this.serviceManager = new ServiceManager(hosts, new UserManager(hosts.getHost()), classLoader);
    }

    /**
     * Get service host
     * @return
     */
    public String getHost() {
        return this.inetSocketAddress.getHostName();
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
     * Get resource monitor
     * @return
     */
    protected ResourceMonitor getResourceMonitor() {
        return this.resourceMonitor;
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
            if(!this.protocol.isSSL()) {
                this.server = new ServerSocket();
                this.server.bind(this.inetSocketAddress, this.backlog);
                logger.info("[HTTP SERVER START] Address: " + this.inetSocketAddress.toString());
            } else {
                File keyStore = Context.getKeyStore().toFile();
                String passphrase = Context.getPassphrase();
                String sslProtocol = Context.getEncryptionMethod();
                this.server = HttpsServerSocketFactory.getSSLServerSocket(keyStore, passphrase, sslProtocol, this.inetSocketAddress, this.backlog);
                logger.info("[HTTPS SERVER START] Address: "+this.inetSocketAddress.toString()+"  Protocol: "+sslProtocol+"  KeyStore: "+keyStore.getName()+"  Supported Protocol: "+Arrays.toString(((SSLServerSocket)server).getSupportedProtocols()));
            }
            while (true) { 
                Socket connection = server.accept();
                connection.setSoTimeout(Context.getConnectionTimeout());
                //connection.setSoLinger(true, 10);
                int queueSize = this.threadpool.getQueue().size();
                String ipAddress = connection.getLocalAddress().toString();
                if(this.ipAllowedFilters.include(ipAddress) && this.ipForbiddenFilters.exclude(ipAddress)) {
                    if(queueSize < Context.getThreadQueueSize()) {
                        logger.info("[CLIENT DETECTED] Client request accepted. Submitting thread. "+ipAddress+" - "+connection.getPort()+"  Thread queue size - "+queueSize);
                        this.threadpool.submit(new LeapRequestHandler(this, this.docroot, connection, this.hosts));    
                    } else {
                        String redirectHost = redirectHostSelection.getSelectedHost();
                        String redirectPage = Html.makeRedirect(this.protocol.getProtocol(), 0, redirectHost);
                        logger.info("[CLIENT DETECTED] Thread pool queue size limit reached: "+queueSize+".  Send redirect to Load-Balance URL: "+redirectHost+"\n"+redirectPage);
                        connection.getOutputStream().write(redirectPage.getBytes());
                        connection.close();
                    }
                } else {
                    logger.info("[CLIENT CANCELED] Rquested IP address is not in allowed filters or exist in forbidden filters: "+ipAddress);
                    Map<String, Object> params = new HashMap<>();
                    params.put("@code", RES_CODE.RES500);
                    params.put("@type", "Not in allowed IP or forbidden IP.");
                    params.put("@message", "Rquested IP address is not in allowed filters or exist in forbidden filters: "+ipAddress);
                    String resPage = this.hosts.getResource().getResourcePage(params);
                    connection.getOutputStream().write(resPage.getBytes());
                    connection.close();                                    
                }
            } 
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Stop server 
     * @throws InterruptedException
     * @throws IOException
     */
    public void close() throws InterruptedException, IOException {
        this.server.close();
    }
}