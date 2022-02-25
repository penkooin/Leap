package org.chaostocosmos.leap.http.enums;

/**
 * Enum protocol of Leap
 */
public enum PROTOCOL {
    HTTP_1_0(false, "HTTP/1.0"),
    HTTPS_1_0(true, "HTTPS/1.0"),
    HTTP_1_1(false, "HTTP/1.1"),
    HTTPS_1_1(true, "HTTPS/1.1"),
    HTTP_2(false, "HTTP/2.0"),
    HTTPS_2(true, "HTTPS/2.0"),
    HTTP_3(false, "HTTP/3.0"),
    HTTPS_3(true, "HTTPS/3.0");

    boolean isSSL;
    String protocol;

    PROTOCOL(boolean isSSL, String protocol) {
        this.isSSL = isSSL;
        this.protocol = protocol;
    }

    public boolean isSSL() {
        return isSSL;
    }

    public String getProtocol() {
        return protocol;
    }

    public static PROTOCOL getProtocol(String protocol) {
        String proto = protocol.replaceAll("[/]|[.]", "_");
        return PROTOCOL.valueOf(proto);
    }
}
