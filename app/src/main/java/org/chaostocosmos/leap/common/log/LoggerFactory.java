package org.chaostocosmos.leap.common.log; 

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;

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
    private static Map<String, Logger> loggerMap = new HashMap<>();

    public static Logger getLogger() {
        return getLogger(Context.get().server().getId());
    }

    /**
     * Get logger object
     * @param loggerId
     * @return
     */
    public static Logger getLogger(String loggerId) {
        if(!loggerMap.containsKey(loggerId)) {            
            if(loggerId.equals(Context.get().server().getId())) {
                loggerMap.put(Context.get().server().getId(), createLoggerFor(Context.get().server().getLogs(), Context.get().server().getLogsLevel()));
            } else {
                Host<?> host = Context.get().hosts().getHost(loggerId);
                loggerMap.put(host.getId(), createLoggerFor(host.getLogPath(), host.getLogLevel()));                            
            }
        } 
        return loggerMap.get(loggerId);
    }

    /**
     * Creeate logger 
     * @param loggerPath
     * @param level
     * @return
     */
    public static Logger createLoggerFor(Path loggerPath, LEVEL level) {
        return new Logger(loggerPath, level);
    }

    /**
     * Create Logger for specific logger name
     * @param loggerName
     * @param loggerFile
     * @param level
     * @return
    public static Logger createLoggerFor(String loggerName, String loggerFile, List<Level> level) {
        System.out.println(loggerName+"  "+loggerFile+"  "+level.toString());
        LoggerContext loggerContext = new LoggerContext();
        loggerContext.start(); // Ensure context is started
    
        // Encoder for logging pattern
        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
        logEncoder.start();
    
        // Console appender setup
        ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
        logConsoleAppender.setContext(loggerContext);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(logEncoder);
        logConsoleAppender.start();
    
        // File appender setup
        RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
        logFileAppender.setContext(loggerContext);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(logEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(loggerFile);
    
        // Rolling policy for log file
        TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
        logFilePolicy.setContext(loggerContext);
        logFilePolicy.setParent(logFileAppender);
        logFilePolicy.setFileNamePattern(loggerFile + "-%d{yyyy-MM-dd_HH}.log");
        logFilePolicy.setMaxHistory(7);
        logFilePolicy.start();
    
        logFileAppender.setRollingPolicy(logFilePolicy);
        logFileAppender.start();
    
        // Create and set up the logger
        Logger logger = loggerContext.getLogger(loggerName);
        logger.addAppender(logConsoleAppender);
        logger.addAppender(logFileAppender);
        level.forEach(logger::setLevel);
        logger.setAdditive(false);
    
        return logger;
    }    
     */
}
 