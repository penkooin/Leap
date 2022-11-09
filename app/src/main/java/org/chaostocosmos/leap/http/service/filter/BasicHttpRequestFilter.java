package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.PostFilters;
import org.chaostocosmos.leap.http.annotation.PreFilters;
import org.chaostocosmos.leap.http.common.LoggerFactory;

/**
 * BasicHttpFilter
 * 
 * @author 9ins
 */
public class BasicHttpRequestFilter extends AbstractRequestFilter {

    @Override
    public void filterRequest(Request request) throws Exception { 
        super.filterRequest(request);
        if(request.getClass().isAssignableFrom(Request.class)) {
            LoggerFactory.getLogger(request.getRequestedHost()).debug("Basic Http request filter processing......");
        }
    }
}
