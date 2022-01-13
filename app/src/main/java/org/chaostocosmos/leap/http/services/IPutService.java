package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;

/**
 * Put Servlet interface
 */
public interface IPutService extends ILeapService {
    /**
     * Put process
     * @param request
     * @param response
     * @throws Exception
     */
    public void servePut(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException;    
}
