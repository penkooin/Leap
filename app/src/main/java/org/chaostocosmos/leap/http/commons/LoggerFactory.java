package org.chaostocosmos.leap.http.commons;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.Hosts;
import org.chaostocosmos.leap.http.VirtualHostManager;
import org.chaostocosmos.leap.http.WASException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**  
 * LoggerUtils object
 * Description : 
 * Logging for debug information
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
     * Constructor
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    private LoggerFactory() throws WASException, IOException, URISyntaxException {
        loggerMap = new HashMap<>();
        String defaultHost = Context.getInstance().getDefaultHost();
        String defaultLogPath = Context.getInstance().getDefaultDocroot().resolve(Context.getInstance().getDefaultLogPath()).toAbsolutePath().toString();
        Level defaultLogLevel = Context.getInstance().getDefaultLogLevel();
        loggerMap.put(defaultHost, createLoggerFor(defaultHost, defaultLogPath, defaultLogLevel));
        List<Hosts> vHosts = VirtualHostManager.getInstance().getVirtualHosts();
        for(Hosts vHost : vHosts) {
            String loggerName = vHost.getHost();
            String logPath = vHost.getDocroot().resolve(vHost.getLogPath()).toAbsolutePath().toString();
            Level logLevel = vHost.getLogLevel();
            loggerMap.put(loggerName, createLoggerFor(loggerName, logPath, logLevel));
        }
    }

    /**
     * Get logger object
     * @param hostName
     * @return
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Logger getLogger(String hostName) {
        if(loggerMap == null) {
            try {
                new LoggerFactory();
            } catch (WASException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return loggerMap.get(hostName);
    }

    /**
     * Create Logger for specific logger name
     * @param loggerName
     * @param loggerFile
     * @param level
     * @return
     */
    public static Logger createLoggerFor(String loggerName, String loggerFile, Level level) {
        LoggerContext logCtx = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(logCtx);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();
    
        ConsoleAppender logConsoleAppender = new ConsoleAppender();
        logConsoleAppender.setContext(logCtx);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(logEncoder);
        logConsoleAppender.start();
    
        logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(logCtx);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();
    
        RollingFileAppender logFileAppender = new RollingFileAppender();
        logFileAppender.setContext(logCtx);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(logEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(loggerFile);
    
        TimeBasedRollingPolicy logFilePolicy = new TimeBasedRollingPolicy();
        logFilePolicy.setContext(logCtx);
        logFilePolicy.setParent(logFileAppender);
        logFilePolicy.setFileNamePattern(loggerFile+"-%d{yyyy-MM-dd_HH}.log");
        logFilePolicy.setMaxHistory(7);
        logFilePolicy.start();
    
        logFileAppender.setRollingPolicy(logFilePolicy);
        logFileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.addAppender(logConsoleAppender);
        logger.addAppender(logFileAppender);
        logger.setLevel(level);
        logger.setAdditive(false); 
        return logger;
    }   
}
 