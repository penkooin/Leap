package org.chaostocosmos.leap.filter;

import org.chaostocosmos.leap.http.HttpResponse;

/**
 * AbstractResponseFilter
 * 
 * @author 9ins
 */
public abstract class AbstractResponseFilter<R> implements IResponseFilter<HttpResponse<R>> {

    @Override
    public void filterResponse(HttpResponse<R> response) throws Exception {
        if(response.getClass().isAssignableFrom(HttpResponse.class)) {
            response.getHost().getLogger().debug("Basic Http response filter processing......");
        }
    }
}
