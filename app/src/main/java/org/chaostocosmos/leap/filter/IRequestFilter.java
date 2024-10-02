package org.chaostocosmos.leap.filter;

/**
 * IHttpRequestFilter
 * @author 9ins
 */
public interface IRequestFilter<R> extends IPreFilter, ISessionFilter {
    /**
     * Filter http request before servlet process
     * @param request
     * @return
     */
    public void filterRequest(R request) throws Exception;    

        /**
     * Check URL is valied
     * @param url
     * @return
     * @throws LeapException 
     */
    public boolean isValidURL(String url); 
}
