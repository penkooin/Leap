package org.chaostocosmos.leap.http.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * CertificateHandler
 * 
 * @author 9ins
 */
public class CertificateHandler {
    /**
     * List up X509Certificate in KeyStore
     * @param storeFile
     * @param storePassword
     * @param storeType
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     * @throws InvalidAlgorithmParameterException
     */
    public static List<X509Certificate> listCertificates(File storeFile, 
                                                         String storePassword, 
                                                         String storeType) throws KeyStoreException, 
                                                                                  NoSuchAlgorithmException, 
                                                                                  CertificateException, 
                                                                                  IOException, 
                                                                                  InvalidAlgorithmParameterException {
        KeyStore keyStore = getKeyStore(storeFile, storePassword, storeType);
        PKIXParameters params = new PKIXParameters(keyStore);
        Iterator<TrustAnchor> it = params.getTrustAnchors().iterator();
        List<X509Certificate> list = new ArrayList<>();
        while( it.hasNext() ) {
            TrustAnchor ta = it.next();
            // Get certificate
            X509Certificate cert = ta.getTrustedCert();
            list.add(cert);
        }
        return list;
    }    

    /**
     * Add certification to KeyStore
     * @param certFile
     * @param certAlias
     * @param storeFile
     * @param storePassword
     * @param storeType
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void addX509CertificateToTrustStore(File certFile, 
                                                      String certAlias, 
                                                      File storeFile, 
                                                      String storePassword, 
                                                      String storeType) throws FileNotFoundException, 
                                                                               KeyStoreException, 
                                                                               CertificateException, 
                                                                               IOException, 
                                                                               NoSuchAlgorithmException {
        try (FileInputStream certInputStream = new FileInputStream(certFile)) {
            addX509CertificateToTrustStore(certInputStream, certAlias, storeFile, storePassword, storeType);
        } finally {
        }
    }

    public static void addX509CertificateToTrustStore(InputStream certInputStream, 
                                                      String certAlias, 
                                                      File storeFile, 
                                                      String storePassword, 
                                                      String storeType) throws FileNotFoundException, 
                                                                               KeyStoreException, 
                                                                               CertificateException, 
                                                                               IOException, 
                                                                               NoSuchAlgorithmException {
        KeyStore keystore = getKeyStore(storeFile, storePassword, storeType);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(certInputStream);
        if(keystore.containsAlias(certAlias)) {
            keystore.deleteEntry(certAlias);
        }
        keystore.setCertificateEntry(certAlias, certificate);
        try(FileOutputStream storeOutputStream = new FileOutputStream(storeFile)) {
            keystore.store(storeOutputStream, storePassword.toCharArray());
        } finally {
        }
    }    

    /**
     * Delete Certification by cert alias
     * @param certAlias
     * @param storeFile
     * @param storePassword
     * @param storeType
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws IOException
     */
    public static void deleteCertificate(String certAlias, 
                                         File storeFile, 
                                         String storePassword, 
                                         String storeType) throws FileNotFoundException, 
                                                                  NoSuchAlgorithmException, 
                                                                  CertificateException, 
                                                                  KeyStoreException, 
                                                                  IOException {
        KeyStore keystore = getKeyStore(storeFile, storePassword, storeType);
        keystore.deleteEntry(certAlias);    
    }

    /**
     * Get KeyStore object
     * @param storeFile
     * @param storePassword
     * @param storeType
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    public static KeyStore getKeyStore(File storeFile, String storePassword, String storeType) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {                                                                                               
        try(FileInputStream storeInputStream = new FileInputStream(storeFile)) {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(storeInputStream, storePassword.toCharArray());
            return keyStore;    
        } finally {
        }
    }

    /**
     * Load PEM to KeyStore
     * @param certificateString
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    public static KeyStore loadPEMTrustStore(File certificateString) throws FileNotFoundException, 
                                                                            IOException, 
                                                                            CertificateException, 
                                                                            KeyStoreException, 
                                                                            NoSuchAlgorithmException {
        byte[] der = loadPemCertificate(new FileInputStream(certificateString));
        ByteArrayInputStream derInputStream = new ByteArrayInputStream(der);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);
        return trustStore;
    }    

    /**
     * Load PEM certificate to bytes
     * @param certificateStream
     * @return
     * @throws IOException
     */
    public static byte[] loadPemCertificate(InputStream certificateStream) throws IOException {
        byte[] der = null;
        BufferedReader br = null;
        try {
            StringBuilder buf = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(certificateStream));
            String line = br.readLine();
            while(line != null) {
                if(!line.startsWith("--")){
                    buf.append(line);
                }
                line = br.readLine();
            }
            String pem = buf.toString();
            der = Base64.getDecoder().decode(pem);
        } finally {
           if(br != null) {
               br.close();
           }
        }
        return der;
    } 


    public static void main(String[] args) throws FileNotFoundException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Path ssl = Paths.get("D:\\0.github\\Leap\\app\\src\\main\\resources\\config\\ssl\\");
        //addX509CertificateToTrustStore(ssl.resolve("leap.pem").toFile(), "leap", ssl.resolve("leap-keystore.jks").toFile(), "939393", "JKS");
        listCertificates(ssl.resolve("leap-keystore.jks").toFile(), "939393", "JKS").stream().forEach(System.out::println);
    }
}
