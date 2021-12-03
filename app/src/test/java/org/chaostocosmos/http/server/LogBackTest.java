package org.chaostocosmos.http.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogBackTest {

    Logger logger = LoggerFactory.getLogger(LogBackTest.class);

    public LogBackTest() throws IOException {
        // this.loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        // this.loggerContext.putProperty("log.config.filesizezip", "1KB");
        // this.loggerContext.putProperty("log.config.path", "./");
        // this.loggerContext.putProperty("log.config.filename", "logbacktest");
        
        // Map map = this.loggerContext.getCopyOfPropertyMap();
        // Iterator<String> iter = map.keySet().iterator();
        // while(iter.hasNext()) {
        //     String key = iter.next();
        //     System.out.println(key+"   "+this.loggerContext.getProperty(key));
        // }
        String str = Files.readString(Paths.get("D:\\Projects\\SimpleWAS\\app\\build.gradle"));
        this.logger.info(str);
        this.logger.debug("This is debug...");
        this.logger.error("This is error ...");
        this.logger.warn("This is warning");
        System.out.println(new File(".").getAbsolutePath())                ;
    }

    public static void main(String[] args) throws IOException {
        new LogBackTest();
    }
    
}
