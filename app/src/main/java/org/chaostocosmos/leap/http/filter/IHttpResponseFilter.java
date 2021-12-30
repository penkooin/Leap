package org.chaostocosmos.leap.http.filter;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.PostFilter;

/**
 * IHttpResponseFilter
 * @author 9ins
 */
public interface IHttpResponseFilter<S> extends IFilter {
    /**
     * Filter http respose after servlet process
     * @param response
     * @return
     */
    @PostFilter
    public void filterResponse(S response) throws WASException;    
}
