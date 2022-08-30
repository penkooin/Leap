package org.chaostocosmos.leap.http.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;

/**
 * Session ID generator
 * 
 * @author 9ins
 */
public class SessionIDGenerator {
    
    protected static final String DEFAULT_ALGORITHM = "MD5";

    private String algorithm = DEFAULT_ALGORITHM;

    private Random random = new SecureRandom();

    private MessageDigest messageDigest;

    private static SessionIDGenerator sessionIDGenerator;

    /**
     * Constructs with Host
     * @param host
     */
    private SessionIDGenerator(Host<?> host) {        
        char[] entropy = host.<String>getHost().toCharArray();
        long seed = System.currentTimeMillis();
        for(int i=0; i<entropy.length; i++) {
            long update = ((byte) entropy[i]) << ((i % 8) * 8);
            seed ^= update;
        }
        this.random.setSeed(seed);
        this.algorithm = host.<String> getSessionIDEncryption();
    }

    /**
     * Get SessionIDGenerator by host ID
     * @param hostId
     * @return
     */
    public static SessionIDGenerator get(String hostId) {
        if(sessionIDGenerator == null) {
            sessionIDGenerator = new SessionIDGenerator(Context.getHosts().getHost(hostId));
        }
        return sessionIDGenerator;
    }

    /**
     * Generate session ID of sepecified length
     * @param length
     * @return
     */
    public synchronized String generateSessionId(int length) {
        return generateSessionId(length, this.algorithm);
    }

    /**
     * Get session ID of length and algorithm(MD5, SHA-256.....)
     * @param length
     * @param algorithm
     * @return
     */
    public synchronized String generateSessionId(int length, String algorithm) {
        byte[] buffer = new byte[length];
        StringBuffer genId = new StringBuffer();
        int resultLenBytes = 0;
        while (resultLenBytes < length) {
            random.nextBytes(buffer);
            buffer = getMessageDigest(algorithm).digest(buffer);
            for (int j = 0; j < buffer.length && resultLenBytes < length; j++) {
                byte b1 = (byte) ((buffer[j] & 0xf0) >> 4);
                if (b1 < 10) {
                    genId.append((char) ('0' + b1));
                } else {
                    genId.append((char) ('A' + (b1 - 10)));
                }
                byte b2 = (byte) (buffer[j] & 0x0f);
                if (b2 < 10) {
                    genId.append((char) ('0' + b2));
                } else {
                    genId.append((char) ('A' + (b2 - 10)));
                }
                resultLenBytes++;
            }
        }
        return genId.toString();
    }

    /**
     * Get MessageDigest object if exist or create if not exist
     * @param algorithm
     * @return
     */
    private MessageDigest getMessageDigest(String algorithm) {
        if(this.messageDigest == null) {
            try {
                this.messageDigest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                try {
                    this.messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
                } catch (NoSuchAlgorithmException e1) {
                    throw new IllegalStateException("Cannot create MessageDigest for specified algorithm");
                }
            }
        }
        return this.messageDigest;
    }

    /**
     * Set MessageDigest algorithm
     * @param algorithm
     */
    public synchronized void setAlgorithm(String algorithm)  {
        this.algorithm = algorithm;
    }

    public static void main(String[] args) {
        List<String> ids = new ArrayList<>();
        for(int i=0; i<10; i++) {
            String id = SessionIDGenerator.get("leap").generateSessionId(32, "sha-256");
            if(ids.contains(id)) {
                System.out.println("Same ID exists in ID List");
            } else {
                ids.add(id);
            }
            System.out.println(id);
        }
    }
}
