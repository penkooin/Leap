package org.chaostocosmos.leap.http.commons;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.VirtualHost;
import org.chaostocosmos.leap.http.VirtualHostManager;
import org.chaostocosmos.leap.http.WASException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

/**  
 * LoggerUtils object
 * Description : 
 * Logging for debug information
 *  
 * @author 9ins
 * @version 1.0
 */
public class LoggerUtils {
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
    private LoggerUtils() throws WASException, IOException, URISyntaxException {
        loggerMap = new HashMap<>();
        List<VirtualHost> vHosts = VirtualHostManager.getInstance().getVirtualHosts();
        for(VirtualHost vHost : vHosts) {
            String loggerName = vHost.getHost();
            Level logLevel = vHost.getLogLevel();
            loggerMap.put(loggerName, createLoggerFor(loggerName, vHost.getDocroot().resolve("logs").toFile().getAbsolutePath(), logLevel));
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
                new LoggerUtils();
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
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setFile(loggerFile);
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.addAppender(fileAppender);
        logger.setLevel(level);
        logger.setAdditive(false); 
        return logger;
    }   
}
 