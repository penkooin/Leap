package org.chaostocosmos.leap.http.service;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.filter.IFilter;

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
    public void setFilters(List<IFilter> preFilters, List<IFilter> postFilters) throws WASException;
}