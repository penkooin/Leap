package org.chaostocosmos.leap.http.enums;

import org.chaostocosmos.leap.http.services.servicemodel.DeleteServiceModel;
import org.chaostocosmos.leap.http.services.servicemodel.GetServiceModel;
import org.chaostocosmos.leap.http.services.servicemodel.PostServiceModel;
import org.chaostocosmos.leap.http.services.servicemodel.PutServiceModel;
import org.chaostocosmos.leap.http.services.servicemodel.ServiceModel;

/**
 * Http request type
 * @author 9ins 
 */
public enum REQUEST_TYPE {    
    /**
     * Get type for IGetServlet
     */
    GET(GetServiceModel.class),
    /**
     * Post type for IPostServlet
     */
    POST(PostServiceModel.class),
    /**
     * Put type for IPutServelt
     */
    PUT(PutServiceModel.class),
    /**
     * Delete type for IDeleteServlet
     */
    DELETE(DeleteServiceModel.class);
    /**
     * Request type mananging class
     */
    Class<? extends ServiceModel> oprClass;
    /**
     * Request type initializer
     * @param requestType
     * @param oprClass
     */
    REQUEST_TYPE(Class<? extends ServiceModel> oprClass) {
        this.oprClass = oprClass;
    }
    /**
     * Get type
     * @param requestType
     * @return
     */
    public REQUEST_TYPE type(String requestType) {
        return REQUEST_TYPE.valueOf(requestType);
    }
}
