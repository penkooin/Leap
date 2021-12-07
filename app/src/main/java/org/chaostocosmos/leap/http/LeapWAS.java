package org.chaostocosmos.leap.http;

import java.io.FileNotFoundException;
import java.io.IOException;
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

/**
 * Leap WAS
 */
public class LeapWAS {
    /**
     * context Map
     */
    Map<String, Context> contextMap = new HashMap<>();

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
        // System.out.println(cmdLine.getOptionValue("h"));
        // System.out.println(cmdLine.getOptionValue("v"));
        // System.out.println(cmdLine.getOptionValue("d"));
        // System.out.println(cmdLine.getOptionValue("l"));

        //build run environment
        Path homePath = Paths.get(cmdLine.getOptionValue("h"));
        setUpEnvironment(homePath);
        //print trademark
        trademark();
    }

    /**
     * Set up environment
     * @param HomePath
     * @throws IOException
     */
    public void setUpEnvironment(Path HomePath) throws IOException {
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
     * Get options
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
        String mark = ResourceHelper.getInstance().getTrademark();
        System.out.println(mark);
        System.out.println();
    }
    
    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {      
        new LeapWAS(args);  
    }
}
