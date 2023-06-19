package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.LeapServer;
import org.junit.jupiter.api.Test;

public class LeapHttpServerTest {

    LeapServer server;

    public LeapHttpServerTest() throws Exception {
        this.server = new LeapServer();
        this.server.start();;
    }

    @Test
    public void testHttpServer() throws LeapException, URISyntaxException, IOException {
        this.server.start();
    }

    public void testServiceHost() throws IOException {
        InetAddress inet = InetAddress.getByName("www.leap.org");
        try(ServerSocket ss = new ServerSocket(8080, 50, inet)) {
            ss.accept();
        }
    }

    public static void main(String[] args) throws Exception {
        LeapHttpServerTest server = new LeapHttpServerTest();
        server.testHttpServer();
    }    
}
