package org.chaostocosmos.http.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.chaostocosmos.http.HttpServer;
import org.chaostocosmos.http.WASException;
import org.junit.jupiter.api.Test;

public class HttpServerTest {

    HttpServer server;

    @Test
    public void testHttpServer() throws WASException, URISyntaxException, IOException {
        this.server = new HttpServer();
        this.server.start();
    }

    @Test
    public void testHttpServer2() throws IOException, URISyntaxException, WASException {
        this.server = new HttpServer(Paths.get("."));
        this.server.start();
    }

    public static void main(String[] args) throws URISyntaxException, IOException, WASException {
        HttpServer server = new HttpServer();
        server.start();
    }
    
}
