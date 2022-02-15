package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.filters.ILeapFilter;
import org.chaostocosmos.leap.http.security.UserManager;

/**
 * Interface for servlet
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public interface ILeapService {
    /**
     * First entry point of client requets
     * @param request
     * @param response
     * @throws Exception
     */
    public void serve(HttpRequestDescriptor request, HttpResponseDescriptor response, Method serviceMethod) throws WASException;

    /**
     * Set filters
     * @param preFilters
     * @param postFilters
     * @throws WASException
     */
    public void setFilters(List<ILeapFilter> preFilters, List<ILeapFilter> postFilters) throws WASException;

    /**
     * Set Leap security manager object
     * @param securityManager
     */
    public void setSecurityManager(UserManager securityManager);
}