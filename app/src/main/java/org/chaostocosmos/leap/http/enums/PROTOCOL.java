package org.chaostocosmos.leap.http.enums;

/**
 * Enum protocol of Leap
 */
public enum PROTOCOL {
    HTTP_1_0(false),
    HTTPS_1_0(true),
    HTTP_1_1(false),
    HTTPS_1_1(true),
    HTTP_2(false),
    HTTPS_2(true),
    HTTP_3(false),
    HTTPS_3(true);

    boolean isSSL;

    PROTOCOL(boolean isSSL) {
        this.isSSL = isSSL;
    }

    public boolean isSSL() {
        return isSSL;
    }
}
