package org.chaostocosmos.http.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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

    @Test
    public void testHttpServer2() throws IOException, URISyntaxException, WASException {
        this.server = new LeapHttpServer(Paths.get("."));
        this.server.start();
    }

    public static void main(String[] args) throws URISyntaxException, IOException, WASException {
        LeapHttpServer server = new LeapHttpServer();
        server.start();
    }    
}
