package org.chaostocosmos.leap.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;

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
    public static Logger logger = (Logger)LoggerFactory.getLogger(LeapWAS.class);

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
    public Map<String, LeapHttpServer> serverMap;

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
        applyOptions(args);
        //build default environment
        ResourceHelper.extractResource("webapp", HOME_PATH); 
        //print trademark
        trademark();
        start(HOME_PATH);
    }

    /** 
     * Apply command line options
     * @param args
     */
    private void applyOptions(String[] args) throws ParseException, FileNotFoundException, IOException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(getOptions(), args);
        String optionD = cmdLine.getOptionValue("d");
        if(optionD != null && optionD.equals("true")) {
            logger.setLevel(Level.DEBUG);
        }
        String optionV = cmdLine.getOptionValue("v");
        if(optionV != null && optionV.equals("false")) {
            systemOut = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            }));
        }
        String optionH = cmdLine.getOptionValue("h");
        if(optionH != null) {
            HOME_PATH = Paths.get(optionH);
            Files.createDirectories(HOME_PATH);
        } else {
            HOME_PATH = Paths.get("./"); 
        }
    }

    /**
     * Start environment
     * @param HomePath
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void start(Path HomePath) throws WASException, IOException, URISyntaxException {
        this.context = Context.getInstance();
        this.virtualHostManager = VirtualHostManager.getInstance();
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
        new LeapWAS(args);  
    }
}
