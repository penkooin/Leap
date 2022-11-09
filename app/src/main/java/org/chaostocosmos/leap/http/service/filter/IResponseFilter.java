package org.chaostocosmos.leap.http.service.filter;

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
    public void filterResponse(R response) throws Exception;    
}
