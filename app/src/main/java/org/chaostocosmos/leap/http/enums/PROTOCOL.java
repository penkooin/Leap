package org.chaostocosmos.leap.http.enums;

/**
 * Enum protocol of Leap
 * 
 * @author 9ins
 */
public enum PROTOCOL {
    
    HTTP(false, "HTTP"),
    HTTPS(true, "HTTPS");

    /**
     * SSL flag
     */
    boolean isSSL;
    /**
     * protocol
     */
    String protocol;

    PROTOCOL(boolean isSSL, String protocol) {
        this.isSSL = isSSL;
        this.protocol = protocol;
    }
    /**
     * Whether SSL
     * @return
     */
    public boolean isSSL() {
        return isSSL;
    }
    /**
     * Get protocol
     * @return
     */
    public String protocol() {
        return protocol;
    }
    /**
     * Get PROTOCOL by String
     * @param protocol
     * @return
     */
    public static PROTOCOL protocol(String protocol) {
        String proto = protocol.replaceAll("[/]|[.]", "_");
        return PROTOCOL.valueOf(proto);
    }
}
