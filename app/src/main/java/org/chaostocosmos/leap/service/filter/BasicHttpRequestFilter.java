package org.chaostocosmos.leap.service.filter;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.http.HttpRequest;

/**
 * BasicHttpFilter
 * 
 * @author 9ins
 */
public class BasicHttpRequestFilter extends AbstractRequestFilter {

    @Override
    public void filterRequest(HttpRequest request) throws Exception { 
        super.filterRequest(request);
        if(request.getClass().isAssignableFrom(HttpRequest.class)) {
            LoggerFactory.getLogger(request.getRequestedHost()).debug("Basic Http request filter processing......");
        }
    }
}
