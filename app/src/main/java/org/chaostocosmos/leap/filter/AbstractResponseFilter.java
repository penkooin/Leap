package org.chaostocosmos.leap.filter;

import org.chaostocosmos.leap.http.HttpResponse;

public abstract class AbstractResponseFilter implements IResponseFilter<HttpResponse> {

    @Override
    public void filterResponse(HttpResponse response) throws Exception {
        if(response.getClass().isAssignableFrom(HttpResponse.class)) {
            response.getHost().getLogger().debug("Basic Http response filter processing......");
        }
    }
}
