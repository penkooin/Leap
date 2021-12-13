package org.chaostocosmos.http.server;

import java.io.IOException;
import java.net.URISyntaxException;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.LoggerUtils;
import org.slf4j.Logger;

public class LoggerUtilsTest {

    public LoggerUtilsTest() throws WASException, IOException, URISyntaxException {
        Logger logger = LoggerUtils.getLogger("127.0.0.1");
    }

    public static void main(String[] args) throws Exception {
        new LoggerUtilsTest(); 
    }
}
