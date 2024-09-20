package org.chaostocosmos.leap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.NotSupportedException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.chaostocosmos.leap.common.ClassUtils;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.common.ThreadPoolManager;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.context.MetaEvent;
import org.chaostocosmos.leap.context.MetaListener;
import org.chaostocosmos.leap.context.Metadata;
import org.chaostocosmos.leap.enums.STATUS;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.manager.ResourceManager;
import org.chaostocosmos.leap.manager.SpringJPAManager;
import org.chaostocosmos.leap.resource.ResourceHelper;
import org.chaostocosmos.leap.resource.ResourceMonitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Leap Application Main
 * 
 * @author 9ins
 */
public class LeapApp implements MetaListener {

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
    public static ResourceManager resourceManager;

    /**
     * Resource Monitor
     */
    public static ResourceMonitor resourceMonitor;

    /**
     * SpringJPAManager object
     */
    public static SpringJPAManager jpaManager;

    /**
     * Server Map
     */
    public static Map<String, LeapServer> leapServerMap;

    /**
     * Constructor with arguments
     * @param args 
     * @throws Exception
     */
    public LeapApp(String[] args) throws Exception {
        // set commend line options        
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
        CommandLine cmdLine = parser.parse(getOptions(), args);
        //set HOME directory
        String optionH = cmdLine.getOptionValue("h");
        if(optionH != null) {
            HOME_PATH = Paths.get(optionH);
            Files.createDirectories( HOME_PATH ); 
        } else {
            HOME_PATH = Paths.get("./").toAbsolutePath().normalize(); 
        }
        if(!HOME_PATH.toFile().isDirectory() || !HOME_PATH.toFile().exists()) {
            throw new FileNotFoundException("Resource path must be directory and exists in : "+HOME_PATH.toAbsolutePath().toString());
        }
        //initialize environment and context
        context = Context.get(HOME_PATH);        
        context.addContextListener(this);
        //set log level
        String optionL = cmdLine.getOptionValue("l");
        logger = LoggerFactory.getLogger(context.hosts().getDefaultHost().getHostId());
        if(optionL != null) {
            Level level = Level.toLevel(optionL); 
            logger.setLevel(level);
        }
        logger.info("Leap Server starting......"+new Date());
        //print trade mark
        trademark();
        logger.info("====================================================================================================");
        //set verbose option to STD IO
        String optionV = cmdLine.getOptionValue("v");
        if(optionV != null && optionV.equals("false")) {
            systemOut = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {}
            }));
            logger.info("verbose mode off");
        } else {
            logger.info("verbose mode on");
        }
    }

    /**
     * Start environment
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        //NetworkInterfaces.get().getAllNetworkAddresses().stream().forEach(i -> System.out.println(i.getHostName())); 
        //Spring JPA 
        if((boolean) context.server().isSupportSpringJPA()) {
            jpaManager = SpringJPAManager.get();
        }
        // initialize resource manager
        resourceManager = ResourceManager.initialize();
        //initialize Leap hosts
        for(Host<?> host : context.hosts().getAllHost()) {
            // set host server status to NONE
            host.setHostStatus(STATUS.NONE);
            InetAddress hostAddress = InetAddress.getByName(host.getHost());
            String hostName = hostAddress.getHostAddress()+":"+host.getPort();
            if(leapServerMap.containsKey(hostName)) {
                String key = leapServerMap.keySet().stream().filter(k -> k.equals(hostName)).findAny().get();
                throw new IllegalArgumentException("Mapping host address is collapse on network interace: "+hostAddress.toString()+":"+host.getPort()+" with "+key);
            }
            LeapServer server = new LeapServer(Context.get().getHome(), host, ClassUtils.getClassLoader());
            leapServerMap.put(hostAddress.getHostAddress()+":"+host.getPort(), server);
            if(host.<Boolean> isDefaultHost()) {
                logger.info("[DEFAULT HOST] - Protocol: "+host.getProtocol()+"   Server: "+host.getHostId()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Home: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());                
            } else {
                logger.info("[VIRTUAL HOST] - Protocol: "+host.getProtocol()+"   Server: "+host.getHostId()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Home: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());
            }
        }
        logger.info("----------------------------------------------------------------------------------------------------");        
        for(LeapServer server : leapServerMap.values()) {
            server.setDaemon(false);
            server.start();
        }
        // Waiting for all host be started.
        if(leapServerMap.values().stream().allMatch(s -> !s.isClosed())) {
            Thread.sleep(100);
        }
        //initialize resource monitor
        if((boolean) context.server().isSupportMonitoring()) {
            resourceMonitor = ResourceMonitor.get();
        } else {
            logger.info("[MONITOR OFF] Leap system monitor turned off.");
        }        
    }

    /**
     * Get home path
     * @return
     */
    public static Path getHomePath() {
        return HOME_PATH;
    }

    /**
     * Shut down Leap WAS
     * @throws IOException
     * @throws InterruptedException
     * @throws NotSupportedException
     */
    public void shutdown() throws InterruptedException, IOException, NotSupportedException { 
        ThreadPoolManager.get().shutdown();
        ResourceMonitor.get().stop();        
        for(LeapServer server : leapServerMap.values()) {
            server.stopServer();
            server.join();
        }
        logger.info("Leap server terminated......");
    }

    /**
     * Get resource manager
     * @return
     */
    public static ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Get resource monitor
     * @return
     */
    public static ResourceMonitor getResourceMonitor() {
        return resourceMonitor;
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
     * @throws LeapException
     */
    private void trademark() throws Exception {
        System.out.println(ResourceHelper.getInstance().getTrademark());
        System.out.println();
    }

    @Override
    public void receiveContextEvent(MetaEvent<Metadata<?>> ce) throws Exception {
        System.out.println(ce.getPathExpression()+"  "+ce.getEventType()+"  ");
        
    }

    public static void main(String[] args) throws Exception {
        LeapApp leap = new LeapApp(args);
        leap.start();
    }
}
