package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.URISyntaxException;

import org.chaostocosmos.leap.http.LeapHttpServer;
import org.chaostocosmos.leap.http.WASException;
import org.junit.jupiter.api.Test;

public class HttpServerTest {

    LeapHttpServer server;

    @Test
    public void testHttpServer() throws WASException, URISyntaxException, IOException {
        this.server = new LeapHttpServer();
        this.server.start();
    }

    public static void main(String[] args) throws URISyntaxException, IOException, WASException {
        LeapHttpServer server = new LeapHttpServer();
        server.start();
    }    
}
