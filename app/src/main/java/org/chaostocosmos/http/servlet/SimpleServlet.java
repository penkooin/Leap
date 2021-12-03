package org.chaostocosmos.http.servlet;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.http.HttpRequestDescriptor;
import org.chaostocosmos.http.HttpResponseDescriptor;
import org.chaostocosmos.http.filter.IHttpFilter;

/**
 * Interface for servlet
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public interface SimpleServlet {
    /**
     * First entry point of client requets
     * @param request
     * @param response
     * @param invokingMethod
     * @throws Exception
     */
    public void service(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws Exception;
    /**
     * Get process
     * @param request
     * @param response
     * @param invokingMethod
     * @throws Exception
     */
    public void doGet(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws Exception;
    /**
     * Post process
     * @param request
     * @param response
     * @param invokingMethod
     * @throws Exception
     */
    public void doPost(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws Exception;
    /**
     * Apply filters
     * @param httpFilter
     * @throws Exception
     */
    public void applyFilters(List<IHttpFilter> filters) throws Exception;
    /**
     * Instantiate SimpleServlet
     * @return
     * @throws Exception
     */
    public SimpleServlet newInstance() throws Exception;
}