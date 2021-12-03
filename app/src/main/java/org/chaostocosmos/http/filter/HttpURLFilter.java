package org.chaostocosmos.http.filter;

import org.chaostocosmos.http.Context;
import org.chaostocosmos.http.HttpRequestDescriptor;
import org.chaostocosmos.http.HttpResponseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filtering request URL
 */
public class HttpURLFilter implements IHttpFilter {
    /**
     * logger
     */
    Logger logger = LoggerFactory.getLogger(HttpURLFilter.class);
    /**
     * Context
     */
    Context context = Context.getInstance();

    @Override
    public void filterRequest(HttpRequestDescriptor request) throws Exception {
        logger.info("URL filter processing...");
    }

    @Override
    public void filterResponse(HttpResponseDescriptor response) throws Exception {
    }    

    public boolean isVaildURL(HttpRequestDescriptor request) {
        return false;
    }
}
