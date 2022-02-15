package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.Before;
import org.junit.Test;    
    
public class HttpsServerSocketFactoryTest {

    String host;
    int port;
    ThreadPoolExecutor threadPoolExecutor;

    public static void main(String[] args) throws Exception {
        HttpsServerSocketFactoryTest test = new HttpsServerSocketFactoryTest();
        test.test();
    }

    @Before
    public void setup(){
        this.host = "localhost";
        this.port = 8080;
        this.threadPoolExecutor = new ThreadPoolExecutor(10, 10, 3000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());        
    }
        
    @Test
    public void test() {
        try {
        ServerSocket ss = getServerSocketFactory("TLS", new File("D:\\0.github\\Leap\\app\\src\\main\\resources\\config\\ssl\\leap-keystore.jks"), "939393").createServerSocket(8080);        
        while (true) { 
            Socket connection = ss.accept();
            System.out.println(connection.toString());
            System.out.println("Local port: "+connection.getLocalPort()+"  Client request accepted...... "+connection.getLocalAddress().toString()+"  ---  "+connection.getPort());
            connection.close();
        }    
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ServerSocket getServerSocket(String protocol) {
        //if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            try {
                // set up key manager to do server authentication
                char[] passphrase = "939393".toCharArray();
                //KeyStore keyStore = KeyStore.getInstance("JKS");
                //keyStore.load(new FileInputStream(Context.getKeyStore().toFile()), passphrase);
                //SSLContext ctx = SSLContext.getInstance(type);
                //final KeyStore keyStore = KeyStore.getInstance(Context.getKeyStore().toFile(), passphrase);
                final KeyStore keyStore = KeyStore.getInstance(new File("D:\\0.github\\Leap\\app\\src\\main\\resources\\config\\ssl\\leap-keystore.jks"), passphrase);
                final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(keyStore, passphrase);

                final SSLContext context = SSLContext.getInstance(protocol);//"SSL" "TLS"
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
                
                final SSLServerSocketFactory factory = context.getServerSocketFactory();
                SSLServerSocket ss = (SSLServerSocket)factory.createServerSocket(8080);
                ss.setNeedClientAuth(true);                
                return ss;
            } catch (Exception e) {
                e.printStackTrace();
            }
        // } else {
        //     return ServerSocketFactory.getDefault();
        // }
        return null;
    }        

    /**
     * Get server socket 
     * @param keyStore
     * @param passphrase
     * @param type
     * @param host
     * @param port
     * @param backlog
     * @return
     * @throws Exception
     */
    public static ServerSocket getSocketServer(String protocol, File keyStore, String passphrase, String host, int port, int backlog) throws Exception {
        ServerSocketFactory serverSocketFactory = getServerSocketFactory(protocol, keyStore, passphrase);
        return serverSocketFactory.createServerSocket(port, backlog, new InetSocketAddress(host, port).getAddress());
    }

    /**
     * Get ServerSocketFactory
     * @param protocol
     * @param keyStore
     * @param passphrase
     * @return
     * @throws Exception
     */
    public static ServerSocketFactory getServerSocketFactory(String protocol, File keyStore, String passphrase) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        InputStream in = new FileInputStream(keyStore);
        trustStore.load(in, passphrase.toCharArray());
        trustManagerFactory.init(trustStore);
        List<X509TrustManager> trustList = new ArrayList<>();        
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                trustList.add((X509TrustManager)trustManager);
                break;                
            }
        }
        X509TrustManager[] trustManagers = trustList.stream().toArray(X509TrustManager[]::new);
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, trustManagers, new SecureRandom());    
        return sslContext.getServerSocketFactory();
    }        
}
    