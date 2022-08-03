package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.RedirectHostSelection;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.ClassUtils;
import org.chaostocosmos.leap.http.resources.Html;
import org.chaostocosmos.leap.http.resources.LeapURLClassLoader;

import ch.qos.logback.classic.Logger;

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
    Logger logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId());

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
     * servlet loading & managing
     */
    ServiceManager serviceManager;

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
     * Server started flag
     */
    boolean started;

    /**
     * Default constructor 
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws IOException
     */
    public LeapHttpServer() throws NotSupportedException, IOException, URISyntaxException {
        this(Constants.DEFAULT_HOME_PATH);
    }

    /**
     * Construct with home path
     * @param homePath
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws IOException
     */
    public LeapHttpServer(Path homePath) throws NotSupportedException, IOException, URISyntaxException {
        this(homePath, 
             Context.getHosts().getHost(Context.getHosts().getDefaultHost().getHostId()), 
             new ThreadPoolExecutor(Context.getServer().getThreadPoolCoreSize(), 
                                    Context.getServer().getThreadPoolMaxSize(), 
                                    Context.getServer().getThreadPoolKeepAlive(), 
                                    TimeUnit.SECONDS, 
                                    new LinkedBlockingQueue<Runnable>())
        );
    }

    /**
     * Construct with home path, host object, thread pool
     * @param homePath
     * @param host
     * @param threadpool
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws IOException
     */
    public LeapHttpServer(Path homePath, 
                          Host<?> host, 
                          ThreadPoolExecutor threadpool
                          ) throws NotSupportedException, IOException, URISyntaxException {
        this(homePath, host, threadpool, ClassUtils.getClassLoader());
    }

    /**
     * Construct with Context object
     * @param homePath
     * @param host
     * @param threadpool 
     * @param classLoader
     * @throws URISyntaxException
     * @throws IOException
     * @throws NotSupportedException
     * @throws MalformedURLException
     */
    public LeapHttpServer(Path homePath, 
                          Host<?> host, 
                          ThreadPoolExecutor threadpool, 
                          LeapURLClassLoader classLoader
                          ) throws IOException, URISyntaxException, NotSupportedException {
        this(
            true,
            Context.getHomePath(),
            host.getDocroot(),
            PROTOCOL.valueOf(host.getProtocol()),
            new InetSocketAddress(InetAddress.getByName(host.getHost()), host.getPort()),
            Context.getServer().getBackLog(),
            threadpool,
            host,
            classLoader
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
     * @param host
     * @param classLoader
     * @throws URISyntaxException
     * @throws IOException
     * @throws NotSupportedException
     */
    public LeapHttpServer(boolean isDefaultHost, 
                          Path homePath, 
                          Path docroot, 
                          PROTOCOL protocol, 
                          InetSocketAddress inetSocketAddress, 
                          int backlog, 
                          ThreadPoolExecutor threadpool,
                          Host<?> host,
                          LeapURLClassLoader classLoader
                          ) throws IOException, URISyntaxException, NotSupportedException {
        this.isDefaultHost = true;
        this.homePath = homePath;
        this.protocol = protocol;
        this.backlog = backlog;
        this.docroot = docroot;
        this.host = host;
        this.threadpool = threadpool;
        this.inetSocketAddress = inetSocketAddress;
        this.ipAllowedFilters = host.getIpAllowedFiltering();
        this.ipForbiddenFilters = host.getIpForbiddenFiltering();
        this.redirectHostSelection = new RedirectHostSelection(Context.getServer().getLoadBalanceRedirects());
        this.serviceManager = new ServiceManager(host, new UserManager(host.getHostId()), classLoader);
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
                File keyStore = new File(Context.getServer().<String> getKeyStore());
                String passphrase = Context.getServer().getPassphrase();
                String sslProtocol = Context.getServer().getEncryptionMethod();
                this.server = HttpsServerSocketFactory.getSSLServerSocket(keyStore, passphrase, sslProtocol, this.inetSocketAddress, this.backlog);                
                logger.info("[HTTPS SERVER START] Address: "+this.inetSocketAddress.toString()+"  Protocol: "+sslProtocol+"  KeyStore: "+keyStore.getName()+"  Supported Protocol: "+Arrays.toString(((SSLServerSocket)server).getSupportedProtocols())+"  KeyStore: "+keyStore.getName());
            }
            while (true) { 
                Socket connection = this.server.accept();
                connection.setSoTimeout(Context.getServer().getConnectionTimeout());
                //connection.setSoLinger(false, 1);
                int queueSize = this.threadpool.getQueue().size();
                String ipAddress = connection.getLocalAddress().toString();
                if(this.ipAllowedFilters.include(ipAddress) || this.ipForbiddenFilters.exclude(ipAddress)) {
                    if(queueSize < Context.getServer().<Integer> getThreadQueueSize()) {
                        logger.info("[CLIENT DETECTED] Client request accepted. Submitting thread. "+ipAddress+" - "+connection.getPort()+"  Thread queue size - "+queueSize);
                        this.threadpool.submit(new LeapRequestHandler(this, this.docroot, connection, this.host));
                    } else {
                        String redirectHost = redirectHostSelection.getSelectedHost();
                        String redirectPage = Html.makeRedirect(this.protocol.protocol(), 0, redirectHost);
                        logger.info("[CLIENT DETECTED] Thread pool queue size limit reached: "+queueSize+".  Send redirect to Load-Balance URL: "+redirectHost+"\n"+redirectPage);
                        try(OutputStream out = connection.getOutputStream()) {
                            out.write(redirectPage.getBytes());
                        }
                        connection.close();
                    }
                } else {
                    logger.info("[CLIENT CANCELED] Rquested IP address is not in allowed filters or exist in forbidden filters: "+ipAddress);
                    Map<String, Object> params = new HashMap<>();
                    params.put("@code", RES_CODE.RES500);
                    params.put("@type", "Not in allowed IP or forbidden IP.");
                    params.put("@message", "Rquested IP address is not in allowed filters or exist in forbidden filters: "+ipAddress);
                    String resPage = this.host.getResource().getResourcePage(params);
                    try(OutputStream out = connection.getOutputStream()) {
                        out.write(resPage.getBytes());
                    }
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

    /**
     * Whether server started
     * @return
     */
    public boolean isClosed() {
        return this.server == null ? true : this.server.isClosed();
    }
}