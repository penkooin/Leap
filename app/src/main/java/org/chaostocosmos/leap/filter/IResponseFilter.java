package org.chaostocosmos.leap.filter;

import org.chaostocosmos.leap.http.Http;

/**
 * IHttpResponseFilter
 * @author 9ins
 */
public interface IResponseFilter<HttpResponse> extends IPostFilter<Http> {

    /**
     * Filter http respose after servlet process
     * @param response
     * @return
     */
    public void filterResponse(HttpResponse response) throws Exception;    
}
