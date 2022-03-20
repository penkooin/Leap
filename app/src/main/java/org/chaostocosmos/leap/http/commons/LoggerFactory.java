package org.chaostocosmos.leap.http.commons; 

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.resources.Context;
import org.chaostocosmos.leap.http.resources.Hosts;
import org.chaostocosmos.leap.http.resources.HostsManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**  
 * LoggerUtils object
 * 
 * Description : This object provide logger for different with each host and virtual-host
 * Each logger is logback Logger object to write log data. 
 *  
 * @author 9ins
 * @version 1.0
 */
public class LoggerFactory {
    /**
     * logger map 
     */    
    private static Map<String, Logger> loggerMap = null;

    /**
     * Get default logger
     * @return
     */
    public static Logger getLogger() {
        return getLogger(Context.getDefaultHost());
    }

    /**
     * Get logger object
     * @param host
     * @return
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Logger getLogger(String host) {
        if(loggerMap == null) {            
            loggerMap = new HashMap<String, Logger>();
        }
        if(!loggerMap.containsKey(host)) {
            HostsManager manager = HostsManager.get();
            Hosts hosts = manager.getHosts(host.trim());
            Logger logger = createLoggerFor(hosts.getHost(), hosts.getDocroot().resolve(hosts.getLogPath()).toAbsolutePath().toString(), hosts.getLogLevel());
            loggerMap.put(hosts.getHost(), logger);
            return logger;
        }
        return loggerMap.get(host);
    }

    /**
     * Create Logger for specific logger name
     * @param loggerName
     * @param loggerFile
     * @param level
     * @return
     */
    public static Logger createLoggerFor(String loggerName, String loggerFile, List<Level> level) {
        LoggerContext loggerContext = new LoggerContext();
        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();    
        ConsoleAppender logConsoleAppender = new ConsoleAppender();
        logConsoleAppender.setContext(loggerContext);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(logEncoder);
        logConsoleAppender.start();    
        logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();    
        RollingFileAppender logFileAppender = new RollingFileAppender();
        logFileAppender.setContext(loggerContext);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(logEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(loggerFile);    
        TimeBasedRollingPolicy logFilePolicy = new TimeBasedRollingPolicy();
        logFilePolicy.setContext(loggerContext);
        logFilePolicy.setParent(logFileAppender);
        logFilePolicy.setFileNamePattern(loggerFile+"-%d{yyyy-MM-dd_HH}.log");
        logFilePolicy.setMaxHistory(7);
        logFilePolicy.start();    
        logFileAppender.setRollingPolicy(logFilePolicy);
        logFileAppender.start();        

        Logger logger = loggerContext.getLogger(loggerName);
        logger.addAppender(logConsoleAppender);
        logger.addAppender(logFileAppender);
        level.stream().forEach(l -> logger.setLevel(l));
        logger.setAdditive(false); 
        return logger;
    }   
}
 