package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * WAS home
     */
    public final static Path WAS_HOME = Paths.get(".");

    /**
     * Index file
     */
    public static String INDEX_FILE = "index.html";

    /**
     * Root path
     */
    public static Path rootPath;

    /**
     * Port path
     */
    public static int port;

    /**
     * servlet loading & managing
     */
    ServletManager servletManager;

    /**
     * thread pool
     */
    ThreadPoolExecutor threadpool;

    /**
     * WAS Context object
     */
    Context context;

    /**
     * Constructor 
     * 
     * @throws WASException
     * @throws URISyntaxException
     * @throws IOException
     */
    public LeapHttpServer() {
        this(WAS_HOME);
    }

    /**
     * Constructor with configuration file Path
     * 
     * @param docroot
     * @throws WASException
     * @throws URISyntaxException
     * @throws IOException
     */
    public LeapHttpServer(Path docroot)  {
        try {
            rootPath = docroot.toAbsolutePath();
            this.context = Context.getInstance();
            this.servletManager = new ServletManager(this.context.getServletBeanList());
        } catch (InstantiationException | 
                 IllegalAccessException | 
                 IllegalArgumentException | 
                 InvocationTargetException | 
                 NoSuchMethodException | 
                 SecurityException | 
                 ClassNotFoundException | 
                 WASException e) {
                 logger.error( Context.getInstance().getErrorMsg("error022", new Object[]{e.getMessage()}) );
        }
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
    public static Path rootPath() {
        return rootPath;
    }

    /**
     * Start server
     * @throws IOException
     * @throws URISyntaxException
     * @throws WASException
     */
    public void start() throws IOException, URISyntaxException, WASException {
        logger.info("WAS server starting... port: "
                    +this.port+" ThreadPool CORE: "
                    +context.getThreadPoolCoreSize()+" MAX: "
                    +context.getThreadPoolMaxSize()+" KEEP-ALIVE WHEN IDLE(seconds): "
                    +context.getThreadPoolKeepAlive());
                    
        this.threadpool = new ThreadPoolExecutor(context.getThreadPoolCoreSize(), 
                                                 context.getThreadPoolMaxSize(), 
                                                 context.getThreadPoolKeepAlive(), 
                                                 TimeUnit.SECONDS, 
                                                 new LinkedBlockingQueue<Runnable>());                                                 

        InetAddress bindAddress = InetAddress.getByName(context.getDefaultHost()+":"+context.getDefaultPort());
        int backlog = context.getBackLog();
        try (ServerSocket server = new ServerSocket(port, backlog, bindAddress)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootPath.toFile().getAbsolutePath());
            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable r = new LeapRequestHandler(this, rootPath, INDEX_FILE, request);
                    logger.info("Client request accepted... : "+request.getLocalAddress().toString());
                    this.threadpool.submit(r);
                } catch (IOException ex) {
                    logger.info("Error accepting connection", ex);
                }
            }
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