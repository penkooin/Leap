package org.chaostocosmos.http;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.chaostocosmos.http.servlet.ServletManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpServer object
 * 
 * Created by cybaek on 15. 5. 22..
 * @author 9ins modified from cybaek
 * @since 2021.09.15
 */
public class HttpServer {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    /**
     * WAS home
     */
    public static final Path WAS_HOME = Paths.get(".");
    private static String INDEX_FILE = "index.html";
    private static Path rootDirectory;
    private static int port;
    /**
     * servlet loading & managing
     */
    private ServletManager servletLoader;
    /**
     * thread pool
     */
    ThreadPoolExecutor threadpool;
    /**
     * WAS Context object
     */
    private final Context context;
    /**
     * Constructor 
     * 
     * @throws WASException
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpServer() throws WASException, URISyntaxException, IOException {
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
    public HttpServer(Path docroot) throws WASException, URISyntaxException, IOException {
        rootDirectory = docroot.toAbsolutePath();        
        UtilBox.extractEnvironment(rootDirectory);
        this.context = Context.getInstance(docroot);        
        port = this.context.getServerPort();
        this.servletLoader = new ServletManager(this.context.getServletBeanList());
    }
    /**
     * Get servlet loader object
     * @return
     */
    protected ServletManager getServletLoader() {
        return this.servletLoader;
    }
    /**
     * Get root directory
     * @return
     */
    public static Path rootDirectory() {
        return rootDirectory;
    }
    /**
     * Start server
     * @throws IOException
     * @throws URISyntaxException
     */
    public void start() throws IOException, URISyntaxException {
        //print trademark
        trademark();

        logger.info("WAS server starting... port: "+this.port+" ThreadPool CORE: "+context.getThreadPoolCoreSize()+" MAX: "+context.getThreadPoolMaxSize()+" KEEP-ALIVE WHEN IDLE(seconds): "+context.getThreadPoolKeepAlive());
        this.threadpool = new ThreadPoolExecutor(context.getThreadPoolCoreSize(), context.getThreadPoolMaxSize(), context.getThreadPoolKeepAlive(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            
        InetAddress bindAddress = InetAddress.getByName(context.getBindAddress());
        int backlog = context.getBackLog();
        try (ServerSocket server = new ServerSocket(port, backlog, bindAddress)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootDirectory.toFile().getAbsolutePath());
            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable r = new HttpRequestHandler(this, rootDirectory.toFile(), INDEX_FILE, request);
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
     * 
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        this.threadpool.awaitTermination(10, TimeUnit.MINUTES);
    }
    /**
     * Print trademark
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    private void trademark() throws IOException, URISyntaxException {
        String mark = UtilBox.getResourceContent("/webapp/trademark");
        System.out.println(mark);
        System.out.println();
    }

    public static void main(String[] args) {        
        new File("./config.josn").delete();        
        if(args.length == 0) {
            try {
                HttpServer webserver = new HttpServer();
                webserver.start();
            } catch (Exception e) {
                logger.error("Server could not start", e);
            }            
        } else if(args.length == 1) {
            Path docroot = Paths.get(args[0]);
            if(!docroot.toFile().isDirectory() || !docroot.toFile().exists()) {
                logger.error("May Doc-root isn't directory or not exist: {}", docroot.toString());
                logger.error("Usage: java -jar wars.jar [DOC-ROOT]");
                return;
            }            
            try {
                HttpServer webserver = new HttpServer(docroot);
                webserver.start();
            } catch (Exception e) {
                logger.error("Server could not start", e);
            }
        }
    }
}