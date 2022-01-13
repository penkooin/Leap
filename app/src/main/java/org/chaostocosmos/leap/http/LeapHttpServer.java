package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.chaostocosmos.leap.http.services.ServiceManager;
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
     * Index file
     */
    public static String INDEX_FILE = Context.getWelcome();

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
     * Whether default host
     */
    boolean isDefaultHost;

    /**
     * host 
     */
    String host;

    /**
     * port
     */
    int port;

    /**
     * server backlog
     */
    int backlog;

    /**
     * document root
     */
    Path docroot;

    /**
     * Default constructor 
     * @throws WASException
     */
    public LeapHttpServer() throws WASException {
        this(Constants.DEFAULT_HOME_PATH);
    }

    /**
     * Construct with home path
     * @throws WASException
     */
    public LeapHttpServer(Path homePath) throws WASException {
        this(Context.initialize(homePath));
    }

    /**
     * Construct with Context object
     * @param context
     * @throws WASException
     */
    public LeapHttpServer(Context context) throws WASException {
        this(
            true,
            Context.getHomePath(),
            Context.getDefaultHost(),
            Context.getDefaultPort(),
            Context.getBackLog(),
            Context.getDefaultDocroot(),
            new ThreadPoolExecutor(Context.getThreadPoolCoreSize(), 
                                   Context.getThreadPoolMaxSize(),                                                  
                                   Context.getThreadPoolKeepAlive(), 
                                   TimeUnit.SECONDS, 
                                   new LinkedBlockingQueue<Runnable>())
        );
    }

    /**
     * Constructor with configuration file Path
     * @param isDefaultHost
     * @param homePath
     * @param host
     * @param port
     * @param backlog
     * @param docroot
     * @param threadpool
     * @throws WASException
     */
    public LeapHttpServer(boolean isDefaultHost, Path homePath, String host, int port, int backlog, Path docroot, ThreadPoolExecutor threadpool) throws WASException {
        this.isDefaultHost = true;
        this.homePath = homePath;
        this.host = host;
        this.port = port;
        this.backlog = backlog;
        this.docroot = docroot;
        this.threadpool = threadpool;
        this.serviceManager = new ServiceManager();
    }

    /**
     * Get service host
     * @return
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get service port
     * @return
     */
    public int getPort() {
        return this.port;
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
    public static Path getDocroot(String host) {
        return Context.getDocroot(host);
    }

    @Override
    public void run() {        
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(this.host), this.port);
            this.server = new ServerSocket();
            this.server.bind(inetSocketAddress, this.backlog);
            logger.info("Accepting connections on port " + server.getLocalPort());
            while (true) { 
                Socket request = server.accept();
                logger.info("Host: "+this.host+":"+this.port+"  Client request accepted... : "+request.getLocalAddress().toString());
                this.threadpool.submit(new LeapRequestHandler(this, this.docroot, INDEX_FILE, request));
            }
        } catch(IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * stop server 
     * @throws InterruptedException
     * @throws IOException
     */
    public void close() throws InterruptedException, IOException {
        this.server.close();
    }
}