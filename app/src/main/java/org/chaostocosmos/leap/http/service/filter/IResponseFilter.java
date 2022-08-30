package org.chaostocosmos.leap.http.service.filter;

import org.chaostocosmos.leap.http.annotation.PostFilterIndicates;

/**
 * IHttpResponseFilter
 * @author 9ins
 */
public interface IResponseFilter<R> extends IPostFilter {
    /**
     * Filter http respose after servlet process
     * @param response
     * @return
     */
    @PostFilterIndicates
    public void filterResponse(R response) throws Exception;    
}
