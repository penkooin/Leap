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
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.transaction.NotSupportedException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.chaostocosmos.leap.common.log.LEVEL;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.common.thread.ThreadPoolManager;
import org.chaostocosmos.leap.common.utils.ClassUtils;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.context.META;
import org.chaostocosmos.leap.context.MetaEvent;
import org.chaostocosmos.leap.context.MetaListener;
import org.chaostocosmos.leap.context.Metadata;
import org.chaostocosmos.leap.enums.SERVER_EVENT;
import org.chaostocosmos.leap.enums.STATUS;
import org.chaostocosmos.leap.enums.WAR_PATH;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.resource.ResourceHelper;
import org.chaostocosmos.leap.resource.ResourceMonitor;
import org.chaostocosmos.leap.resource.ResourceProvider;
import org.chaostocosmos.leap.spring.SpringJPAManager;
import org.hibernate.internal.util.config.ConfigurationException;

/**
 * Leap Application Main
 * 
 * @author 9ins
 */
public class LeapApp implements MetaListener {

    /**
     * Standard IO
     */
    public static PrintStream systemOut;

    /**
     * Home path
     */
    public static Path HOME_PATH;

    /**
     * Static resource manager
     */
    public static ResourceProvider resourceProvider;

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
            HOME_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize(); 
        }
        if(!HOME_PATH.toFile().isDirectory() || !HOME_PATH.toFile().exists()) {
            throw new FileNotFoundException("Resource path must be directory and exists in : "+HOME_PATH.toAbsolutePath().toString());
        }
        //initialize environment and context
        ResourceHelper.extractResource(WAR_PATH.CONFIG.path(), HOME_PATH, null); 
        Map<String, Path> hostMap = Context.get().server().getHosts();
        for(Map.Entry<String, Path> e : hostMap.entrySet()) {
            ResourceHelper.extractResource(WAR_PATH.WEBAPP.path(), e.getValue(), null);
            Path classes = Context.get().host(e.getKey()).getClasses();
            ResourceHelper.extractResource("org/chaostocosmos/leap/service", e.getValue().resolve(classes), null);
            ClassUtils.getClassLoader().addPath(classes);
        }
        Context.get().addContextListener(this);

        //set log level
        String optionL = cmdLine.getOptionValue("l");        
        Logger logger = LoggerFactory.getLogger(Context.get().server().getId());
        if(optionL != null) {
            LEVEL level = LEVEL.valueOf(optionL); 
            logger.setLevel(level);
        }
        logger.info("Leap Server starting......"+new Date());
        //print trade mark
        trademark();
        logger.info("============================================================");
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
     * @throws Exception
     */
    public void start() throws Exception {
        if(Context.get().server().getHosts().size() < 1) {
            throw new ConfigurationException("Leap server must have one or more host!!!");
        }
        //NetworkInterfaceManager.getAllNetworkAddresses().stream().forEach(i -> this.logger.info(i.getHostName())); 
        // Spring JPA 
        if(Context.get().server().isSupportSpringJPA()) {
            jpaManager = SpringJPAManager.get();
        }
        // initialize resource manager                
        resourceProvider = ResourceProvider.get();

        // initialize Leap hosts
        for(Host<?> host : Context.get().allHost()) {
            // set host server status to NONE
            host.setHostStatus(STATUS.NONE);
            InetAddress hostAddress = InetAddress.getByName(host.getHost());
            String hostName = hostAddress.getHostAddress()+":"+host.getPort();
            if(leapServerMap.containsKey(hostName)) {
                String key = leapServerMap.keySet().stream().filter(k -> k.equals(hostName)).findAny().get();
                throw new IllegalArgumentException("Mapping host address is collapse on network interace: "+hostAddress.toString()+":"+host.getPort()+" with "+key);
            }
            LeapServer server = new LeapServer(Context.get().getHome(), host);
            leapServerMap.put(hostAddress.getHostAddress()+":"+host.getPort(), server);
            LoggerFactory.getLogger().info("[HOST:"+hostAddress.getHostAddress()+":"+host.getPort()+"] - Protocol: "+host.getProtocol()+"   Server: "+host.getId()+"   Host: "+host.getHost()+"   Port: "+host.getPort()+"   Home: "+host.getDocroot()+"   Logging path: "+host.getLogPath()+"   Level: "+host.getLogLevel().toString());                
        }
        LoggerFactory.getLogger().info("----------------------------------------------------------------------------------------------------");        
        for(LeapServer server : leapServerMap.values()) {
            server.setDaemon(false);
            server.start();
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
    public void shutdown() { 
        LoggerFactory.getLogger().info("[TERMINATED] Shutdown Leap !!!");        
        try {
            ThreadPoolManager.get().terminate();
            ResourceProvider.get().terminates();
            ResourceMonitor.get().terminate();                
            for(LeapServer server : leapServerMap.values()) {
                server.stopServer();
                server.interrupt();
                server.join();
            }    
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get resource manager
     * @return
     */
    public static ResourceProvider getResourceProvider() {
        return resourceProvider;
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

    /**
     * Receive context event
     * @param ce
     */
    @Override
    public void receiveContextEvent(MetaEvent<Metadata<?>> ce) throws Exception {
        if(ce.getEventType() == SERVER_EVENT.CHANGED) {
            LoggerFactory.getLogger().debug(ce.getPathExpression()+"  "+ce.getEventType());
            if(ce.getValue().equals(STATUS.TERMINATED.name())) {
                shutdown();                
            } else if(ce.getValue().equals(STATUS.STARTED.name())) {                
                //initialize resource monitor
                if(Context.get().server().isSupportMonitoring()) {
                    LoggerFactory.getLogger().info("[MONITOR ON] Leap system monitor turned on.");
                    resourceMonitor = ResourceMonitor.get();
                    resourceMonitor.start();
                } else {
                    LoggerFactory.getLogger().info("[MONITOR OFF] Leap system monitor turned off.");
                }        
            }            
        }
    }

    public static void main(String[] args) throws Exception {
        LeapApp leap = new LeapApp(args);
        leap.start();
    }
}

