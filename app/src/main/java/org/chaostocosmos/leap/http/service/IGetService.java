package org.chaostocosmos.leap.http.service;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;

/**
 * Get Servlet interface
 */
public interface IGetService extends ILeapService { 
    /**
     * Get process
     * @param request
     * @param response
     * @throws Exception
     */
    public void serveGet(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException;    
}
