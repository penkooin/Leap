package org.chaostocosmos.leap.http.commons;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Context;
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
     * Get logger object
     * @param hostName
     * @return
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Logger getLogger(String hostName) {
        if(loggerMap == null) {            
            loggerMap = Context.getAllHosts()
            .entrySet()
            .stream()
            .map(e -> {
                String host = e.getKey();
                String path = e.getValue().getDocroot().resolve(e.getValue().getLogPath()).toAbsolutePath().toString();
                List<Level> level = e.getValue().getLogLevel();
                return new Object[]{host, createLoggerFor(host, path, level)};
            }).collect(Collectors.toMap(k -> (String)k[0], v -> (Logger)v[1]));    
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
    public static Logger createLoggerFor(String loggerName, String loggerFile, List<Level> level) {
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

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(loggerName);
        logger.addAppender(logConsoleAppender);
        logger.addAppender(logFileAppender);
        level.stream().forEach(l -> logger.setLevel(l));
        logger.setAdditive(false); 
        return logger;
    }   
}
 