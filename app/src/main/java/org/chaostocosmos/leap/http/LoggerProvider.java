package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.VirtualHostManager.VirtualHost;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;

//Implementation spec #4

/**
 * LoggerProvider object for vhost logging
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class LoggerProvider {   

    /**
     * LoggerProvider instance
     */
    private static LoggerProvider loggerProvider = null;

    /**
     * context
     */
    private static Context context = Context.getInstance();

    /**
     * logger map for each vhost 
     */
    private static Map<String, Logger> loggerMap = new HashMap<>();

    /**
     * Logger created milliseconds is stored this Map
     */
    private static Map<String, Long> loggerMillisMap = new HashMap<>();

    /**
     * Mliiseconds of day
     */
    public static final long DAY = 1000*60*60*24;

    /**
     * Get logger for each vhost
     * 
     * @param serverName
     * @return
     * @throws IOException
     * @throws WASException
     */
    public static Logger getLogger(String serverName) throws WASException, IOException {
        Logger logger = loggerMap.get(serverName);
        if(logger == null || System.currentTimeMillis() - loggerMillisMap.get(serverName) > DAY ){
            VirtualHostManager virtualHostManager = VirtualHostManager.getInstance();
            VirtualHost vhost = virtualHostManager.getVirtualHost(serverName);
            if(vhost != null) {
                loggerMap.put(serverName, createLogger(serverName, virtualHostManager.getVirtualHost(serverName).getDocroot().toString(), makeLogFilename(serverName)));
                loggerMillisMap.put(serverName, System.currentTimeMillis());    
            }
        }
        return logger;
    }

    /**
     * Get LoggerProvider instance
     * @return
     */
    public static LoggerProvider getLoggerProvider() {
        if(loggerProvider == null) {
            loggerProvider = new LoggerProvider();
        }
        return loggerProvider;
    }

    private static String makeLogFilename(String serverName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return serverName+"-"+LocalDate.now().format(formatter)+".log";
    }
    
    /**
     * Create Logger 
     * @param serverName
     * @return
     */
    public static Logger createLogger(String serverName, String docroot, String filename) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();

        ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
        logConsoleAppender.setContext(lc);
        logConsoleAppender.setName(serverName);
        logConsoleAppender.setEncoder(ple);
        logConsoleAppender.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setFile(docroot+"/logs/"+filename+".log");
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();

        //  RollingFileAppender can't be created multiple. Why?

        /*
        RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
        logFileAppender.setContext(lc);
        logFileAppender.setName(serverName+"__");
        logFileAppender.setEncoder(ple);
        logFileAppender.setAppend(true);
        logFileAppender.setFile("logs/"+serverName+".log");
    
        TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
        logFilePolicy.setParent(logFileAppender);
        logFilePolicy.setFileNamePattern("logs/logfile-%d{yyyy-MM-dd_HH}.log");
        logFilePolicy.setMaxHistory(7);
        logFilePolicy.setContext(lc);
        logFilePolicy.start();
    
        logFileAppender.setRollingPolicy(logFilePolicy);
        logFileAppender.start();
        */

        Logger logger = (Logger) LoggerFactory.getLogger(serverName);
        logger.setAdditive(false);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(logConsoleAppender);
        logger.addAppender(fileAppender);
        return logger;    
    }
}
