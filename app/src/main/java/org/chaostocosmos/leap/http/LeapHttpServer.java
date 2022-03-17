package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;

import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.commons.DynamicURLClassLoader;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.resources.Context;
import org.chaostocosmos.leap.http.resources.Hosts;
import org.chaostocosmos.leap.http.resources.HostsManager;
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
     * Hosts
     */
    Hosts hosts;

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
                        new LinkedBlockingQueue<Runnable>() ));
    }

    /**
     * Construct with Context object
     * @param homePath
     * @param hosts
     * @param threadpool 
     * @throws UnknownHostException
     * @throws MalformedURLException
     */
    public LeapHttpServer(Path homePath, Hosts hosts, ThreadPoolExecutor threadpool) throws UnknownHostException, MalformedURLException {
        this(
            true,
            Context.getLeapHomePath(),
            hosts.getDocroot(),
            hosts.getProtocol(),
            new InetSocketAddress(InetAddress.getByName(hosts.getHost()), hosts.getPort()),
            Context.getBackLog(),
            threadpool,
            new ServiceManager(
                hosts.getDynamicClasspaths() != null ? new DynamicURLClassLoader(new URL[] {hosts.getDynamicClasspaths().toUri().toURL()}) : new DynamicURLClassLoader()
                , new UserManager(hosts.getHost())),
            hosts
        );        
    }

    /**
     * Constructor with configuration file Path
     * @param isDefaultHost
     * @param homePath
     * @param docroot
     * @param protocol1
     * @param inetSocketAddress
     * @param backlog
     * @param docroot
     * @param threadpool
     * @param serviceManager
     */
    public LeapHttpServer(boolean isDefaultHost, 
                          Path homePath, 
                          Path docroot, 
                          PROTOCOL protocol, 
                          InetSocketAddress inetSocketAddress, 
                          int backlog, 
                          ThreadPoolExecutor threadpool,
                          ServiceManager serviceManager,
                          Hosts hosts
                          ) {
        this.isDefaultHost = true;
        this.homePath = homePath;
        this.protocol = protocol;
        this.backlog = backlog;
        this.docroot = docroot;
        this.threadpool = threadpool;
        this.inetSocketAddress = inetSocketAddress;
        this.serviceManager = serviceManager;
        this.hosts = hosts;
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
                logger.info("[CLIENT DETECTED] Client request accepted......"+connection.getLocalAddress().toString()+"  ---  "+connection.getPort());
                this.threadpool.submit(new LeapRequestHandler(this, this.docroot, connection, this.hosts));
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