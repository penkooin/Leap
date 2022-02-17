package org.chaostocosmos.leap.http.filters;

/**
 * IHttpRequestFilter
 * @author 9ins
 */
public interface IHttpRequestFilter<R> extends ILeapFilter{
    /**
     * Filter http request before servlet process
     * @param request
     * @return
     */
    public void filterRequest(R request) throws Exception;    
}
