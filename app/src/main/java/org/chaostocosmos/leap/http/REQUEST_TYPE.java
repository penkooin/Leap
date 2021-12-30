package org.chaostocosmos.leap.http;

import org.chaostocosmos.leap.http.service.IDeleteService;
import org.chaostocosmos.leap.http.service.IGetService;
import org.chaostocosmos.leap.http.service.ILeapService;
import org.chaostocosmos.leap.http.service.IPostService;
import org.chaostocosmos.leap.http.service.IPutService;

/**
 * Http request type
 * @author 9ins 
 */
public enum REQUEST_TYPE {    
    /**
     * Get type for IGetServlet
     */
    GET(IGetService.class),

    /**
     * Post type for IPostServlet
     */
    POST(IPostService.class),

    /**
     * Put type for IPutServelt
     */
    PUT(IPutService.class),

    /**
     * Delete type for IDeleteServlet
     */
    DELETE(IDeleteService.class);

    /**
     * Request type mananging class
     */
    Class<? extends ILeapService> oprClass;

    /**
     * Request type initializer
     * @param requestType
     * @param oprClass
     */
    REQUEST_TYPE(Class<? extends ILeapService> oprClass) {
        this.oprClass = oprClass;
    }

    /**
     * Get type
     * @param requestType
     * @return
     */
    public REQUEST_TYPE getType(String requestType) {
        return REQUEST_TYPE.valueOf(requestType);
    }
}
