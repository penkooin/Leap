package org.chaostocosmos.leap.enums;

/**
 * Host status enum
 * 
 * @author 9ins
 */
public enum LEAP_STATUS {
    NONE(0),
    SETUP(1),
    STARTING(2),
    STARTED(3),
    RUNNING(4),
    TERMINATED(5);

    int code;

    /**
     * Initializer
     * @param code
     */
    LEAP_STATUS(int code) {
        this.code = code;
    }
    
    /**
     * Get status code
     * @return
     */
    public int code() {
        return this.code;
    }
}
