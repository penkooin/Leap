package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
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
import org.chaostocosmos.leap.http.commons.Hosts;
import org.chaostocosmos.leap.http.commons.HostsManager;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.ResourceHelper;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * LeapWAS main
 * 
 * @author 9ins
 */
public class LeapWAS {
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
     * Server Map
     */
    public Map<String, LeapHttpServer> leapServerMap;

    /**
     * Thread pool
     */
    ThreadPoolExecutor threadpool;

    /**
     * Constructor 
     * @param args 
     * @throws IOException
     * @throws URISyntaxException
     * @throws WASException
     * @throws ParseException
     */
    public LeapWAS(String[] args) throws IOException, URISyntaxException, WASException, ParseException {
        //set commend line options
        this.leapServerMap = new HashMap<>();
        setup(args);
    }

    /** 
     * Apply command line options
     * @param args
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     * @throws WASException
     */
    private void setup(String[] args) throws WASException {
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
        //print trade mark
        trademark();
        logger.info("Leap WAS server starting......");
        //initialize thread pool
        this.threadpool = new ThreadPoolExecutor(Context.getThreadPoolCoreSize(), 
                                                 Context.getThreadPoolMaxSize(), 
                                                 Context.getThreadPoolKeepAlive(), 
                                                 TimeUnit.SECONDS, 
                                                 new LinkedBlockingQueue<Runnable>());
        logger.info("--------------------------------------------------------------------------");
        logger.info("ThreadPool initialized - CORE: "+Context.getThreadPoolCoreSize()+" MAX: "+Context.getThreadPoolMaxSize()+" KEEP-ALIVE WHEN IDLE(seconds): "+Context.getThreadPoolKeepAlive());

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
        hostsManager = HostsManager.getInstance(); 
    }

    /**
     * Start environment
     *  
     * @throws WASException
     */
    public void start() throws WASException {
        Hosts difault = Context.getDefaultHosts();
        LeapHttpServer difaultHost = new LeapHttpServer(difault.isDefaultHost(), Context.getHomePath(), difault.getHost(), difault.getPort(), Context.getBackLog(), difault.getDocroot(), this.threadpool);
        logger.info("Default host added - Server: "+difault.getServerName()+"   Host: "+difault.getHost()+"   Port: "+difault.getPort()+"   Doc-Root: "+difault.getDocroot()+"   Logging path: "+difault.getLogPath()+"   Level: "+difault.getLogLevel().toString());
        this.leapServerMap.put(difault.getHost(), difaultHost);
        for(Hosts host : Context.getVirtualHosts().values()) {
            if(this.leapServerMap.values().stream().allMatch(h -> h.getPort() != difault.getPort())) {
                LeapHttpServer virtual = new LeapHttpServer(host.isDefaultHost(), Context.getHomePath(), host.getHost(), host.getPort(), Context.getBackLog(), host.getDocroot(), this.threadpool);
                this.leapServerMap.put(host.getHost(), virtual);
            }
            logger.info("Virtual host added - Server: "+host.getServerName()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Doc-Root: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());
        }        
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
            throw new WASException(MSG_TYPE.ERROR, 22);
        }
    }
    
    public static void main(String[] args) throws InstantiationException, 
                                                  IllegalAccessException, 
                                                  IllegalArgumentException, 
                                                  InvocationTargetException, 
                                                  NoSuchMethodException, 
                                                  SecurityException, 
                                                  ClassNotFoundException, 
                                                  WASException, 
                                                  IOException, 
                                                  URISyntaxException, 
                                                  ParseException {        
        LeapWAS leap = new LeapWAS(args);
        leap.start();
    }
}
