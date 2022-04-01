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
import java.util.List;
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
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.resources.ClassUtils;
import org.chaostocosmos.leap.http.resources.Context;
import org.chaostocosmos.leap.http.resources.Hosts;
import org.chaostocosmos.leap.http.resources.HostsManager;
import org.chaostocosmos.leap.http.resources.LeapURLClassLoader;
import org.chaostocosmos.leap.http.resources.ResourceHelper;
import org.chaostocosmos.leap.http.resources.SpringJPAManager;
import org.chaostocosmos.leap.http.resources.StaticResourceManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * LeapWAS main
 * 
 * @author 9ins
 */
public class LeapApplication {
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
    public Context context;

    /**
     * Virtual host manager
     */
    public HostsManager hostsManager;

    /**
     * Static resource manager
     */
    public StaticResourceManager staticResourceManager;

    /**
     * Server Map
     */
    public Map<String, LeapHttpServer> leapServerMap;

    /**
     * Thread pool
     */
    ThreadPoolExecutor threadpool;

    /**
     * Constructor with arguments
     * @param args 
     * @throws IOException
     * @throws URISyntaxException
     * @throws WASException
     * @throws ParseException
     */
    public LeapApplication(String[] args) throws Exception {
        //set commend line options
        this.leapServerMap = new HashMap<>();
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
        this.context = Context.initialize(HOME_PATH);

        //set log level
        String optionL = cmdLine.getOptionValue("l");
        logger = LoggerFactory.getLogger(Context.getDefaultHost());
        if(optionL != null) {
            Level level = Level.toLevel(optionL); 
            logger.setLevel(level);
        }
        logger.info("Leap starting......");

        //print trade mark
        trademark();

        //build webapp environment
        List<Hosts> hosts = HostsManager.get().getAllHosts();
        for(Hosts host : hosts) {
            ResourceHelper.extractResource("webapp", host.getDocroot());
        }

        //initialize static resource manager
        this.staticResourceManager = StaticResourceManager.initialize();

        //initialize thread pool
        this.threadpool = new ThreadPoolExecutor(Context.getThreadPoolCoreSize(), 
                                                 Context.getThreadPoolMaxSize(), 
                                                 Context.getThreadPoolKeepAlive(), 
                                                 TimeUnit.SECONDS, 
                                                 new LinkedBlockingQueue<Runnable>());

        logger.info("--------------------------------------------------------------------------");
        logger.info("ThreadPool initialized - CORE: "+Context.getThreadPoolCoreSize()
                   +" MAX: "+Context.getThreadPoolMaxSize()
                   +" KEEP-ALIVE WHEN IDLE(seconds): "+Context.getThreadPoolKeepAlive());

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
        //instantiate virtual host object
        hostsManager = HostsManager.get(); 
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
        SpringJPAManager jpaManager = SpringJPAManager.get();

        for(Hosts host : HostsManager.get().getAllHosts()) {
            InetAddress hostAddress = InetAddress.getByName(host.getHost());
            String hostName = hostAddress.getHostAddress()+":"+host.getPort();
            if(this.leapServerMap.containsKey(hostName)) {
                String key = this.leapServerMap.keySet().stream().filter(k -> k.equals(hostName)).findAny().get();
                throw new IllegalArgumentException("Mapping host address is collapse on network interace: "+hostAddress.toString()+":"+host.getPort()+" with "+key);
            }
            LeapHttpServer server = new LeapHttpServer(Context.getLeapHomePath(), host, this.threadpool, classLoader);
            this.leapServerMap.put(hostAddress.getHostAddress()+":"+host.getPort(), server);
            if(host.isDefaultHost()) {
                logger.info("[DEFAULT HOST] - Protocol: "+host.getProtocol().name()+"   Server: "+host.getServerName()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Doc-Root: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());
            } else {
                logger.info("[VIRTUAL HOST] - Protocol: "+host.getProtocol().name()+"   Server: "+host.getServerName()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Doc-Root: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());
            }
        }
        logger.info("--------------------------------------------------------------------------");
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
        for(LeapHttpServer server : this.leapServerMap.values()) {
            server.close();
            server.join();
        }
        this.threadpool.shutdown();
        while(!this.threadpool.awaitTermination(3, TimeUnit.SECONDS)) {
            logger.info("Waiting for termination server...");
        }
        logger.info("Leap server terminated...");
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
        LeapApplication leap = new LeapApplication(args);
        leap.start();
    }
}
