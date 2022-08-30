package org.chaostocosmos.leap.http.security;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SecurityHandler {
    /**
     * Load KeyStore
     * @param keyStoreFile
     * @param passphrase
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    public static KeyStore loadKeyStore(File keyStoreFile, String passphrase) throws KeyStoreException, 
                                                                 NoSuchAlgorithmException, 
                                                                 CertificateException, 
                                                                 IOException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreFile, passphrase.toCharArray());
        return keyStore;
    }

    /**
     * Get trust manager
     * @param keyStore
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyStoreException
     */
    public static TrustManager[] getTrustManagers(KeyStore keyStore) throws NoSuchAlgorithmException, 
                                                                            NoSuchProviderException, 
                                                                            KeyStoreException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    /**
     * Get key store
     * @param keyStore
     * @param passphrase
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     */
    public static KeyManager[] getKeyManagers(KeyStore keyStore, String passphrase) throws NoSuchAlgorithmException, 
                                                                                             NoSuchProviderException, 
                                                                                             UnrecoverableKeyException, 
                                                                                             KeyStoreException {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, passphrase.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }    
}
