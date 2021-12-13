package org.chaostocosmos.leap.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
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
 * Leap WAS
 */
public class LeapWAS {
    /**
     * logger
     */
    public static Logger logger = (Logger)LoggerFactory.getLogger(LeapWAS.class);

    /**
     * standard IO
     */
    public static PrintStream systemOut;

    /**
     * context Map
     */
    public Map<String, Context> contextMap = new HashMap<>();

    /**
     * constructor 
     * @param args 
     * @throws ParseException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public LeapWAS(String[] args) throws ParseException, FileNotFoundException, IOException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(getOptions(), args);
        
        if(cmdLine.getOptionValue("d").equals("false")) {
            logger.setLevel(Level.DEBUG);
        }

        if(cmdLine.getOptionValue("v").equals("true")) {
            systemOut = System.out;
            System.setOut(null);
        }

        //build run environment
        Path homePath = Paths.get(cmdLine.getOptionValue("h"));
        //setUpEnvironment(homePath); 
        //print trademark
        trademark();
    }

    /**
     * Set up environment
     * @param HomePath
     * @throws IOException
     * @throws URISyntaxException
     */
    public void setUpEnvironment(Path HomePath) throws IOException, URISyntaxException {
        //build default environment
        ResourceHelper.buildEnv(HomePath);
        Context defaultContext = Context.getInstance();
        this.contextMap.put(defaultContext.getDefaultHost(), defaultContext);
        List<Object> vHosts = (List<Object>)defaultContext.getConfigValue("server.virtual-host");
        for(Object vHost : vHosts) {
            Map<String, Object> vMap = (Map<String, Object>)vHost;
        }
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
        options.addOption(new Option("l", "logPrefix", true, "set log file prefix"));
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
    
    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {      
        new LeapWAS(args);  
    }
}
