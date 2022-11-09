package org.chaostocosmos.leap.http.enums;

import org.chaostocosmos.leap.http.service.model.DeleteServiceModel;
import org.chaostocosmos.leap.http.service.model.GetServiceModel;
import org.chaostocosmos.leap.http.service.model.PostServiceModel;
import org.chaostocosmos.leap.http.service.model.PutServiceModel;
import org.chaostocosmos.leap.http.service.model.ServiceModel;

/**
 * Http request type
 * @author 9ins 
 */
public enum REQUEST {    
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
    REQUEST(Class<? extends ServiceModel> oprClass) {
        this.oprClass = oprClass;
    }
    /**
     * Get type
     * @param requestType
     * @return
     */
    public REQUEST type(String requestType) {
        return REQUEST.valueOf(requestType);
    }
}
