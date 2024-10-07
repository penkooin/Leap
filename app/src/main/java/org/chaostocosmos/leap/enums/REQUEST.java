package org.chaostocosmos.leap.enums;

/**
 * Http request type
 * 
 * @author 9ins 
 */
public enum REQUEST {    
    /**
     * Get type
     */
    GET("GET"),

    /**
     * Post type
     */
    POST("POST"),

    /**
     * Put type
     */
    PUT("PUT"),

    /**
     * Delete type
     */
    DELETE("DELETE"),

    /**
     * Patch type
     */
    PATCH("PATCH"),

    /**
     * Options type
     */
    OPTIONS("OPTIONS"),

    /**
     * Head type
     */
    HEAD("HEAD");

    /**
     * Request type mananging class
     */
    String requestType;

    /**
     * Request type initializer
     * @param requestType
     */
    REQUEST(String requestType) {
        this.requestType = requestType;
    }

    /**
     * Get type
     * @param requestType
     * @return
     */
    public static REQUEST type(String requestType) {
        return REQUEST.valueOf(requestType);
    }
    
    /**
     * Get request type
     * @return
     */
    public String getType() {
        return this.requestType;
    }    
}
