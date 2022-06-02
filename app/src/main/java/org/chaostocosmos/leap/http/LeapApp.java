package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.resources.ClassUtils;
import org.chaostocosmos.leap.http.resources.LeapURLClassLoader;
import org.chaostocosmos.leap.http.resources.ResourceHelper;
import org.chaostocosmos.leap.http.resources.ResourceManager;
import org.chaostocosmos.leap.http.resources.ResourceMonitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * LeapWAS main
 * 
 * @author 9ins
 */
public class LeapApp {
    /**
     * Logger
     */
    public static Logger logger;

    /**
     * Standard IO
     */
    public static PrintStream systemOut;

    /**
     * Home path
     */
    public static Path HOME_PATH;

    /**
     * Context
     */
    public static Context context;

    /**
     * Static resource manager
     */
    public static ResourceManager staticResourceManager;

    /**
     * Server Map
     */
    public static Map<String, LeapHttpServer> leapServerMap;

    /**
     * Thread pool
     */
    public static ThreadPoolExecutor threadpool;

    /**
     * Thread Queue
     */
    public static LinkedBlockingQueue<Runnable> threadQueue;

    /**
     * Resource monitor
     */
    public static ResourceMonitor resourceMonitor;

    /**
     * Constructor with arguments
     * @param args 
     * @throws IOException
     * @throws URISyntaxException
     * @throws WASException
     * @throws ParseException
     */
    public LeapApp(String[] args) throws Exception {
        //set commend line options
        leapServerMap = new HashMap<>();
        setup(args);
    }

    /** 
     * Apply command line options
     * @param args
     * @throws Exception
     */
    private void setup(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine;
        try {
            cmdLine = parser.parse(getOptions(), args);
        } catch (ParseException e) {
            throw new WASException(e);
        }
        //set HOME directory
        String optionH = cmdLine.getOptionValue("h");
        if(optionH != null) {
            HOME_PATH = Paths.get(optionH);
            try {
                Files.createDirectories(HOME_PATH);
            } catch (IOException e) {
                throw new WASException(e);
            }
        } else {
            HOME_PATH = Paths.get("./"); 
        }

        //initialize environment and context
        context = Context.initialize(HOME_PATH);

        //set log level
        String optionL = cmdLine.getOptionValue("l");
        logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId());
        if(optionL != null) {
            Level level = Level.toLevel(optionL); 
            logger.setLevel(level);
        }
        logger.info("Leap starting.");

        //print trade mark
        trademark();

        //build webapp environment
        for(Host<?> host : Context.getHostMap().values()) {
            ResourceHelper.extractResource("webapp", host.getDocroot());
        }

        //initialize static resource manager
        staticResourceManager = ResourceManager.initialize();

        //initialize thread queue
        threadQueue = new LinkedBlockingQueue<Runnable>();

        //initialize thread pool
        threadpool = new ThreadPoolExecutor(Context.getServer().getThreadPoolCoreSize(), Context.getServer().getThreadPoolMaxSize(), Context.getServer().getThreadPoolKeepAlive(), TimeUnit.SECONDS, this.threadQueue);        

        logger.info("====================================================================================================");
        logger.info("ThreadPool initialized - CORE: "+Context.getServer().getThreadPoolCoreSize()+"   MAX: "+Context.getServer().getThreadPoolMaxSize()+"   KEEP-ALIVE WHEN IDLE(seconds): "+Context.getServer().getThreadPoolKeepAlive());

        //resourceMonitor = ResourceMonitor.get();
        //resourceMonitor.start();

        //set verbose option to STD IO
        String optionV = cmdLine.getOptionValue("v");
        if(optionV != null && optionV.equals("false")) {
            systemOut = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            }));
            logger.info("verbose mode off");
        } else {
            logger.info("verbose mode on");
        }
    }

    /**
     * Start environment
     *  
     * @throws WASException
     */
    public void start() throws Exception {
        //NetworkInterfaces.getAllNetworkAddresses().stream().forEach(i -> System.out.println(i.getHostName()));
        //LeapClassLoader
        LeapURLClassLoader classLoader = ClassUtils.getClassLoader();
        //Spring JPA 
        //SpringJPAManager jpaManager = SpringJPAManager.get();

        for(Host<?> host : Context.getHostMap().values()) {
            InetAddress hostAddress = InetAddress.getByName(host.getHost());
            String hostName = hostAddress.getHostAddress()+":"+host.getPort();
            if(this.leapServerMap.containsKey(hostName)) {
                String key = this.leapServerMap.keySet().stream().filter(k -> k.equals(hostName)).findAny().get();
                throw new IllegalArgumentException("Mapping host address is collapse on network interace: "+hostAddress.toString()+":"+host.getPort()+" with "+key);
            }
            //if(hosts.getHost().equals(Context.getDefaultHost())) {
                LeapHttpServer server = new LeapHttpServer(Context.getHomePath(), host, this.threadpool, classLoader, this.resourceMonitor);
                this.leapServerMap.put(hostAddress.getHostAddress()+":"+host.getPort(), server);
            //} else {
            //    ThreadPoolExecutor threadpool = new ThreadPoolExecutor(20, 20, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()); 
            //    LeapHttpServer server = new LeapHttpServer(Context.getLeapHomePath(), hosts, this.threadpool, classLoader, this.resourceMonitor);
            //    this.leapServerMap.put(hostAddress.getHostAddress()+":"+hosts.getPort(), server); 
            //}
            if(host.isDefaultHost()) {
                logger.info("[DEFAULT HOST] - Protocol: "+host.getProtocol().name()+"   Server: "+host.getHostId()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Doc-Root: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());                
            } else {
                logger.info("[VIRTUAL HOST] - Protocol: "+host.getProtocol().name()+"   Server: "+host.getHostId()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Doc-Root: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());
            }
        }
        logger.info("----------------------------------------------------------------------------------------------------");
        for(LeapHttpServer server : this.leapServerMap.values()) {
            server.setDaemon(false);
            server.start();
        }
    }

    /**
     * Shut down Leap WAS
     * @throws IOException
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException, IOException { 
        resourceMonitor.stop();
        for(LeapHttpServer server : leapServerMap.values()) {
            server.close();
            server.join();
        }
        threadpool.shutdown();
        while(!threadpool.awaitTermination(3, TimeUnit.SECONDS)) {
            logger.info("Waiting for termination server...");
        }
        logger.info("Leap server terminated...");
    }

    /**
     * Get home path
     * @return
     */
    public static Path getHomePath() {
        return HOME_PATH;
    }

    /**
     * Get thread pool
     * @return
     */
    public static ThreadPoolExecutor getThreadPool() {
        return threadpool;
    }

    /**
     * Get execution parameter options
     * @return
     */
    private Options getOptions() {
        Options options = new Options();
        options.addOption(new Option("h", "home", true, "run home path")); 
        options.addOption(new Option("v", "verbose", true, "run with verbose mode"));
        options.addOption(new Option("l", "logLevel", true, "log level setting"));
        return options;
    }
 
    /**
     * Print trademark 
     * @throws WASException
     */
    private void trademark() throws WASException {
        try {
            System.out.println(ResourceHelper.getInstance().getTrademark());
            System.out.println();
        } catch (IOException e) {
            throw new WASException(MSG_TYPE.ERROR, 5);
        }
    }

    public static void main(String[] args) throws Exception {        
        LeapApp leap = new LeapApp(args);
        leap.start();
    }
}
