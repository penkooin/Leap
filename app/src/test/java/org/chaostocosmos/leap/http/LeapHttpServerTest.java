package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

public class LeapHttpServerTest {

    LeapHttpServer server;

    public LeapHttpServerTest() throws Exception {
        this.server = new LeapHttpServer();
        this.server.start();;
    }

    @Test
    public void testHttpServer() throws WASException, URISyntaxException, IOException {
        this.server.start();
    }

    public void testServiceHost() throws IOException {
        InetAddress inet = InetAddress.getByName("www.leap.org");
        ServerSocket ss = new ServerSocket(8080, 50, inet);
        ss.accept();
    }

    public static void main(String[] args) throws Exception {
        LeapHttpServerTest server = new LeapHttpServerTest();
    }    
}
