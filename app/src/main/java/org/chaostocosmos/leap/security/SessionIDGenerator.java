package org.chaostocosmos.leap.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;

/**
 * Session ID generator
 * 
 * @author 9ins
 */
public class SessionIDGenerator {

    /**
     * Default Algorithm
     */
    protected static final String DEFAULT_ALGORITHM = "MD5";

    /**
     * Algorithm in this object
     */
    private String algorithm = DEFAULT_ALGORITHM;

    /**
     * Random object
     */
    private Random random = new SecureRandom();

    /**
     * Session ID generator
     */
    private static SessionIDGenerator sessionIDGenerator;

    /**
     * Constructs with Host
     * @param host
     */
    private SessionIDGenerator(Host<?> host) {        
        char[] entropy = host.getHost().toCharArray();
        long seed = System.currentTimeMillis();
        for(int i=0; i<entropy.length; i++) {
            long update = ((byte) entropy[i]) << ((i % 8) * 8);
            seed ^= update;
        }
        this.random.setSeed(seed);
        this.algorithm = host.<String> getValue("global.session.encryption");
    }

    /**
     * Get SessionIDGenerator by host ID
     * @param hostId
     * @return
     */
    public static SessionIDGenerator get(String hostId) {
        if(sessionIDGenerator == null) {
            sessionIDGenerator = new SessionIDGenerator(Context.get().hosts().getHost(hostId));
        }
        return sessionIDGenerator;
    }

    /**
     * Generate session ID of sepecified length
     * @param length
     * @return
     */
    public String generateSessionId(int length) {
        return generateSessionId(length, this.algorithm);
    }

    /**
     * Get session ID of length and algorithm(MD5, SHA-256.....)
     * @param length
     * @param algorithm
     * @return
     */
    public String generateSessionId(int length, String algorithm) {
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
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            try {
                messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
            } catch (NoSuchAlgorithmException e1) {
                throw new IllegalStateException("Cannot create MessageDigest for specified algorithm");
            }
        } catch (Exception e) {
            throw new RuntimeException("There is something wrong in getting MessageDigest", e);
        }
        return messageDigest;
    }

    /**
     * Set MessageDigest algorithm
     * @param algorithm
     */
    public void setAlgorithm(String algorithm)  {
        this.algorithm = algorithm;
    }
}
