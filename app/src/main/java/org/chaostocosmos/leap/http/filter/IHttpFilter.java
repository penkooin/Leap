package org.chaostocosmos.leap.http.filter;

import org.chaostocosmos.leap.http.WASException;

/**
 * Http filter spec
 * 
 * @author 9ins
 * @since 2021.09.17
 */
public interface IHttpFilter<R, S> extends IHttpRequestFilter<R>, IHttpResponseFilter<S> {    
    /**
     * Check URL is valied
     * @param url
     * @return
     * @throws WASException
     */
    public boolean isValidURL(String url); 
}
