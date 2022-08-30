package org.chaostocosmos.leap.http.common; 

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
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
        return getLogger(Context.getHosts().getDefaultHost().getHostId());
    }

    /**
     * Get logger object
     * @param hostId
     * @return
     * @throws HTTPException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Logger getLogger(String hostId) {
        if(loggerMap == null) {            
            loggerMap = new HashMap<String, Logger>();
        }
        if(!loggerMap.containsKey(hostId)) {
            Host<?> hosts = Context.getHosts().getHost(hostId);
            Logger logger = createLoggerFor(hosts.getHostId(), hosts.getDocroot().resolve(hosts.getLogPath()).toAbsolutePath().toString(), hosts.getLogLevel());
            loggerMap.put(hosts.getHost(), logger);
            return logger;
        }
        return loggerMap.get(hostId);
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
        ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
        logConsoleAppender.setContext(loggerContext);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(logEncoder);
        logConsoleAppender.start();    
        logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();    
        RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
        logFileAppender.setContext(loggerContext);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(logEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(loggerFile);    
        TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
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
 