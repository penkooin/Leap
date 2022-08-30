package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.HTTPException;

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
     * @throws HTTPException 
     */
    public boolean isValidURL(String url); 

}
