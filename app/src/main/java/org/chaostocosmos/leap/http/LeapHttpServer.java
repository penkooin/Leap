package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.chaostocosmos.leap.http.servlet.ServletManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

/**
 * HttpServer object
 * 
 * @author 9ins
 * @since 2021.09.15
 */
public class LeapHttpServer {
    /**
     * logger
     */
    Logger logger = LoggerFactory.getLogger(LeapHttpServer.class);

    /**
     * Index file
     */
    public static String INDEX_FILE = Context.getWelcome();

    /**
     * servlet loading & managing
     */
    ServletManager servletManager;

    /**
     * thread pool
     */
    ThreadPoolExecutor threadpool;

    /**
     * InetAddress object
     */
    InetAddress inetAddress;

    /**
     * port
     */
    int port;

    /**
     * server backlog
     */
    int backlog;

    /**
     * Default constructor 
     * @throws WASException
     * @throws UnknownHostException
     */
    public LeapHttpServer() throws UnknownHostException, WASException {
        this(InetAddress.getByName(Context.getDefaultHost()),
             Context.getDefaultPort(),
             Context.getBackLog());
    }

    /**
     * Constructor with configuration file Path
     * @param inetAddress
     * @param port
     * @param backlog
     * @throws WASException
     * @throws URISyntaxException
     * @throws IOException
     */
    public LeapHttpServer(InetAddress inetAddress, int port, int backlog) throws WASException {
        this.inetAddress = inetAddress;
        this.port = port;
        this.backlog = backlog;
        try {
            this.servletManager = new ServletManager(Context.getServletBeanList());
        } catch (
                InstantiationException | 
                IllegalAccessException | 
                IllegalArgumentException | 
                InvocationTargetException | 
                NoSuchMethodException | 
                SecurityException | 
                ClassNotFoundException | 
                WASException e) {
                logger.error( Context.getErrorMsg("error022", new Object[]{e.getMessage()}) );
        }        
        start();
    }

    /**
     * Get servlet loader object
     * @return
     */
    protected ServletManager getServletManager() {
        return this.servletManager;
    }

    /**
     * Get root directory
     * @return
     */
    public static Path getVirtualHostDocroot(String host) {
        return null;
    }

    /**
     * Start server
     * @throws WASException
     */
    public void start() throws WASException {
        logger.info("WAS server starting... port: "
                    +this.port+" ThreadPool CORE: "
                    +Context.getThreadPoolCoreSize()+" MAX: "
                    +Context.getThreadPoolMaxSize()+" KEEP-ALIVE WHEN IDLE(seconds): "
                    +Context.getThreadPoolKeepAlive());
                    
        this.threadpool = new ThreadPoolExecutor(Context.getThreadPoolCoreSize(), 
                                                 Context.getThreadPoolMaxSize(),                                                  
                                                 Context.getThreadPoolKeepAlive(), 
                                                 TimeUnit.SECONDS, 
                                                 new LinkedBlockingQueue<Runnable>());

        try (ServerSocket server = new ServerSocket(this.port, this.backlog, this.inetAddress)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + Context.getDefaultDocroot().toFile().getAbsolutePath());
            while (true) {
                Socket request = server.accept();
                //List<String> hosts = ResourceHelper.get
                Runnable r = new LeapRequestHandler(this, Context.getDefaultDocroot(), INDEX_FILE, request); 
                logger.info("Client request accepted... : "+request.getLocalAddress().toString());
                this.threadpool.submit(r);
            }
        } catch(IOException e) {
            throw new WASException(MSG_TYPE.ERROR, "error021");
        }
    }

    /**
     * Shutdown server 
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        this.threadpool.awaitTermination(10, TimeUnit.MINUTES);
    }
}