package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.URISyntaxException;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.slf4j.Logger;

public class LoggerUtilsTest {

    public LoggerUtilsTest() throws HTTPException, IOException, URISyntaxException {
        Logger logger = LoggerFactory.getLogger("127.0.0.1");
        logger.error("This is error !!!");
    }

    public static void main(String[] args) throws Exception {
        new LoggerUtilsTest(); 
    }
}
