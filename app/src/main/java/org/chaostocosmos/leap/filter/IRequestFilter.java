package org.chaostocosmos.leap.filter;

import org.chaostocosmos.leap.exception.LeapException;

/**
 * IHttpRequestFilter
 * 
 * @author 9ins
 */
public interface IRequestFilter<F> extends IPreFilter<F>, ISessionFilter {

    /**
     * Filter http request before servlet process
     * @param request
     * @return
     */
    public void filterRequest(F request) throws Exception;    

    /**
     * Check URL is valied
     * @param url
     * @return
     * @throws LeapException 
     */
    public boolean isValidURL(String url); 
}
