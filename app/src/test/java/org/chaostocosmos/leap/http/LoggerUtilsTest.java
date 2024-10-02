package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.URISyntaxException;

import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.exception.LeapException;

public class LoggerUtilsTest {

    public LoggerUtilsTest() throws LeapException, IOException, URISyntaxException {
        Logger logger = LoggerFactory.getLogger("127.0.0.1");
        logger.error("This is error !!!");
    }

    public static void main(String[] args) throws Exception {
        new LoggerUtilsTest(); 
    }
}
