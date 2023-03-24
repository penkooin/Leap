package org.chaostocosmos.leap.enums;

/**
 * Enum protocol of Leap
 * 
 * @author 9ins
 */
public enum PROTOCOL {
    
    HTTP(false, "HTTP"),
    HTTPS(true, "HTTPS"),
    WSS(true, "WSS");

    /**
     * SSL flag
     */
    boolean isSecured;
    /**
     * protocol
     */
    String protocol;

    PROTOCOL(boolean isSSL, String protocol) {
        this.isSecured = isSSL;
        this.protocol = protocol;
    }
    /**
     * Whether SSL
     * @return
     */
    public boolean isSecured() {
        return isSecured;
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
