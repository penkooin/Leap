package org.chaostocosmos.leap.http.services.filters;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.commons.LoggerFactory;

/**
 * BasicHttpFilter
 * @author 9ins
 */
public class BasicHttpFilter<R, S> extends AbstractFilter<R, S> {

    @Override
    @PreFilter
    public void filterRequest(R r) throws Exception { 
        super.filterRequest(r);
        if(r.getClass().isAssignableFrom(Request.class)) {
            Request request = (Request)r;
            LoggerFactory.getLogger(request.getRequestedHost()).debug("Basic Http request filter processing......");
        }
    }

    @Override
    @PostFilter
    public void filterResponse(S s) throws Exception {
        super.filterResponse(s);
        if(s.getClass().isAssignableFrom(Response.class)) {
            Response response = (Response)s;
            LoggerFactory.getLogger(response.getRequestedHost()).debug("Basic Http response filter processing......");
        }
    }
}
