package org.chaostocosmos.leap.service.filter;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.http.Request;

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
