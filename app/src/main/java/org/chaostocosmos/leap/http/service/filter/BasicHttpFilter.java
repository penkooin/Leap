package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.common.LoggerFactory;

/**
 * BasicHttpFilter
 * 
 * @author 9ins
 */
public class BasicHttpFilter extends AbstractFilter {

    @Override
    @PreFilter
    public void filterRequest(Request request) throws Exception { 
        super.filterRequest(request);
        if(request.getClass().isAssignableFrom(Request.class)) {
            LoggerFactory.getLogger(request.getRequestedHost()).debug("Basic Http request filter processing......");
        }
    }

    @Override
    @PostFilter
    public void filterResponse(Response response) throws Exception {
        super.filterResponse(response);
        if(response.getClass().isAssignableFrom(Response.class)) {
            LoggerFactory.getLogger(response.getRequestedHost()).debug("Basic Http response filter processing......");
        }
    }
}
