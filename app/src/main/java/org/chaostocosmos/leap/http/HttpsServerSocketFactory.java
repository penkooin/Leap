package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.services.security.SecurityHandler;

import ch.qos.logback.classic.Logger;

/**
 * LHttpsSecurityManager
 * 
 * @author 9ins
 */
public class HttpsServerSocketFactory {
    /**
     * Logger
     */
    Logger logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId());

    /**
     * Get SSL ServerSocket or if type isn't TLS, return normal ServerSocket
     * @param keyStoreFile
     * @param passphrase
     * @param protocol
     * @param address
     * @param backlog
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnknownHostException
     * @throws IOException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws UnrecoverableKeyException
     */
    public static ServerSocket getSSLServerSocket(File keyStoreFile, 
                                                  String passphrase, 
                                                  String protocol, 
                                                  InetSocketAddress address,
                                                  int backlog) throws NoSuchAlgorithmException,                                                                                                                                 
                                                                      KeyManagementException, 
                                                                      UnknownHostException, 
                                                                      IOException, 
                                                                      UnrecoverableKeyException, 
                                                                      NoSuchProviderException, 
                                                                      KeyStoreException, 
                                                                      CertificateException {        
        KeyStore keyStore = SecurityHandler.loadKeyStore(keyStoreFile, passphrase);
        //KeyStore keyStore = CertificateHandler.loadCertificates(new File("D:\\0.github\\Leap\\app\\src\\main\\resources\\config\\ssl\\localhost+1.pem"), new File("D:\\0.github\\Leap\\app\\src\\main\\resources\\config\\ssl\\localhost+1-key.pem"), passphrase);
        KeyManager[] keyStores = SecurityHandler.getKeyManagers(keyStore, passphrase);
        TrustManager[] trustManagers = SecurityHandler.getTrustManagers(keyStore); 
        SSLContext sslContext = SSLContext.getInstance(protocol);  
        sslContext.init(keyStores, trustManagers, null);
        SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket)serverSocketFactory.createServerSocket(address.getPort(), backlog, address.getAddress());
        serverSocket.setNeedClientAuth(true);                
        return serverSocket;        
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
