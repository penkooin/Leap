package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.chaostocosmos.leap.http.client.LeapClient;

/**
 * Leap request test
 */
public class LeapRequestTest {

    final String[] urls = {
        "/",
        "/video/video.html",
        "/img/logo100.png",
        "/script/genDir.js",
        "/templates/default.html",
        "/templates/error.html",
        "/templates/monitor.html",
        "/templates/resource.html",
    };

    ExecutorService threadPool;
    LeapClient leapClient;
    String host;
    int port;
    int requestCnt;
    int threadCnt;

    public LeapRequestTest(String host, int port, int requestCnt, int threadCnt) throws UnknownHostException, IOException {
        this.threadPool = Executors.newFixedThreadPool(threadCnt);
        this.host = host;
        this.port = port;
        this.requestCnt = requestCnt;
        this.threadCnt = threadCnt;
    }

    public void request() throws Exception {
        Random random = new Random();
        for(int i=0; i<this.requestCnt; i++) {            
            final int r = random.nextInt(urls.length -1);
            this.threadPool.submit(new Runnable() {
                @Override
                public void run() {                    
                    String url = urls[ r ];
                    System.out.println("[REQUEST] "+host+":"+port+""+url);
                    try {
                        Thread.sleep(r * 3);
                        LeapClient.build(host, port).get(url, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        this.threadPool.shutdown();
    }    

    public static void main(String[] args) throws Exception {
        LeapRequestTest requestTest = new LeapRequestTest("localhost", 8080, 100000, 100);
        requestTest.request();
    }
}
