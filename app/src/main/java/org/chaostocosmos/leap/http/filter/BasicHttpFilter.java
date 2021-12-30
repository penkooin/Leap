package org.chaostocosmos.leap.http.filter;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.commons.LoggerFactory;

/**
 * BasicHttpFilter
 * @author 9ins
 */
public class BasicHttpFilter<R, S> extends AbstractHttpFilter<R, S> {

    @Override
    @PreFilter
    public void filterRequest(R r) throws WASException {
        super.filterRequest(r);
        if(r.getClass().isAssignableFrom(HttpRequestDescriptor.class)) {
            HttpRequestDescriptor request = (HttpRequestDescriptor)r;
            LoggerFactory.getLogger(request.getRequestedHost()).debug("Basic Http request filter processing.................");
        }
    }

    @Override
    @PostFilter
    public void filterResponse(S s) throws WASException {
        super.filterResponse(s);
        if(s.getClass().isAssignableFrom(HttpResponseDescriptor.class)) {
            HttpResponseDescriptor response = (HttpResponseDescriptor)s;
            LoggerFactory.getLogger(response.getRequestedHost()).debug("Basic Http response filter processing...");
        }
    }
}
