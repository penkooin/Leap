package org.chaostocosmos.leap.http.filter;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;

/**
 * Http filter spec
 * 
 * @author 9ins
 * @since 2021.09.17
 */
public interface IHttpFilter extends IFilter {
    /**
     * Filter http request before servlet process
     * @param request
     * @return
     */
    public void filterRequest(HttpRequestDescriptor request) throws Exception;
    /**
     * Filter http respose after servlet process
     * @param response
     * @return
     */
    public void filterResponse(HttpResponseDescriptor response) throws Exception;
}
