package org.chaostocosmos.leap.enums;

/**
 * Http request type
 * 
 * @author 9ins 
 */
public enum REQUEST {    
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
