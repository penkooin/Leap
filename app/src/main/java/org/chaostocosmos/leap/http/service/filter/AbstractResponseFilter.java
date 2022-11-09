package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.inject.PostFilterIndicates;

public abstract class AbstractResponseFilter implements IResponseFilter<Response> {

    @Override
    @PostFilterIndicates
    public void filterResponse(Response response) throws Exception {
        if(response.getClass().isAssignableFrom(Response.class)) {
            LoggerFactory.getLogger(response.getRequestedHost()).debug("Basic Http response filter processing......");
        }
    }    
}
