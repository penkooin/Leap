package org.chaostocosmos.leap.http.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class CertificateHandler {

    public static Certificate[] loadCertificates(File pem, File key, String passphrase) throws CertificateException, FileNotFoundException {
        X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new FileInputStream(pem));
        PKCS8EncodedKeySpec a;
        return null;
    }

    private static KeyStore loadPEMTrustStore(File certificateString) throws Exception {
        byte[] der = Files.readAllBytes(certificateString.toPath());
        ByteArrayInputStream derInputStream = new ByteArrayInputStream(der);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);
        return trustStore;
    }    

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
}
