package org.chaostocosmos.leap.client;

/**
 * Http request type
 * 
 * @author 9ins 
 */
public enum REQUEST_TYPE {    
    /**
     * Get type for IGetServlet
     */
    GET("GET"),

    /**
     * Post type for IPostServlet
     */
    POST("POST"),

    /**
     * Put type for IPutServelt
     */
    PUT("PUT"),

    /**
     * Delete type for IDeleteServlet
     */
    DELETE("DELETE");

    /**
     * Request type mananging class
     */
    String requestType;

    /**
     * Request type initializer
     * @param requestType
     * @param oprClass
     */
    REQUEST_TYPE(String requestType) {
        this.requestType = requestType;
    }

    /**
     * Get type
     * @param requestType
     * @return
     */
    public static REQUEST_TYPE type(String requestType) {
        return REQUEST_TYPE.valueOf(requestType);
    }

    /**
     * Get request type
     * @return
     */
    public String getType() {
        return this.requestType;
    }
}
