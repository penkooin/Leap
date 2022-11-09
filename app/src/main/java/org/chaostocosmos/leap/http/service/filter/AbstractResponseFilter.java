package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.common.LoggerFactory;

public abstract class AbstractResponseFilter implements IResponseFilter<Response> {

    @Override
    public void filterResponse(Response response) throws Exception {
        if(response.getClass().isAssignableFrom(Response.class)) {
            LoggerFactory.getLogger(response.getRequestedHost()).debug("Basic Http response filter processing......");
        }
    }
}
