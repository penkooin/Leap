package org.chaostocosmos.leap.http;

import java.io.FileNotFoundException;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.chaostocosmos.leap.http.commons.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * LeapWAS main
 * 
 * @author 9ins
 */
public class LeapWAS {
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
    public VirtualHostManager virtualHostManager;

    /**
     * Server Map
     */
    public Map<String, LeapHttpServer> leapServerMap;

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
    private void setup(String[] args) throws ParseException, IOException, WASException, URISyntaxException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(getOptions(), args);

        //set HOME directory
        String optionH = cmdLine.getOptionValue("h");
        if(optionH != null) {
            HOME_PATH = Paths.get(optionH);
            Files.createDirectories(HOME_PATH);
        } else {         
            HOME_PATH = Paths.get("./"); 
        }
        //initialize Context
        this.context = Context.initialize(HOME_PATH);
        //build config environment
        ResourceHelper.extractResource("config", HOME_PATH); 

        //set debug option to logger
        String optionD = cmdLine.getOptionValue("d");
        if(optionD != null && optionD.equals("true")) {
            LoggerFactory.getLogger(Context.getDefaultHost()).setLevel(Level.DEBUG);
        }

        //set verbose option to STD IO
        String optionV = cmdLine.getOptionValue("v");
        if(optionV != null && optionV.equals("false")) {
            systemOut = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            }));
        }
        //instantiate virtual host object
        this.virtualHostManager = VirtualHostManager.getInstance();
    }

    /**
     * Start environment
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void start() throws WASException, IOException, URISyntaxException {
        trademark();
        this.leapServerMap = new HashMap<>();
        this.leapServerMap.put(Context.getDefaultHost(), 
                               new LeapHttpServer(InetAddress.getByName(Context.getDefaultHost()), 
                                                  Context.getDefaultPort(),
                                                  Context.getBackLog())
                               );
        
    }

    /**
     * Get execution parameter options
     * @return
     */
    private Options getOptions() {
        Options options = new Options();
        options.addOption(new Option("h", "home", true, "run home path"));
        options.addOption(new Option("v", "verbose", true, "run with verbose mode"));
        options.addOption(new Option("d", "debug", true, "print debug information"));
        return options;
    }
 
    /**
     * Print trademark 
     * @throws FileNotFoundException
     * @throws IOException
     * @throws URISyntaxException
     */
    private void trademark() throws FileNotFoundException, IOException {
        System.out.println(ResourceHelper.getInstance().getTrademark());
        System.out.println();
    }
    
    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException, URISyntaxException, WASException {
        LeapWAS leap = new LeapWAS(args);  
        leap.start();
    }
}
