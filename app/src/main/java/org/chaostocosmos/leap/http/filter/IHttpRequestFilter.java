package org.chaostocosmos.leap.http.filter;

import org.chaostocosmos.leap.http.WASException;

/**
 * IHttpRequestFilter
 * @author 9ins
 */
public interface IHttpRequestFilter<R> extends IFilter {
    /**
     * Filter http request before servlet process
     * @param request
     * @return
     */
    public void filterRequest(R request) throws WASException;    
}
