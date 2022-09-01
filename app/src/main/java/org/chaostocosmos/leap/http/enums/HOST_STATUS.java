package org.chaostocosmos.leap.http.enums;

/**
 * Host status enum
 */
public enum HOST_STATUS {

    NONE(0),
    SETUP(1),
    STARTING(2),
    STARTED(3),
    RUNNING(4),
    TERMINATED(5);

    int code;

    HOST_STATUS(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }
}
