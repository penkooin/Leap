package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.filters.ILeapFilter;
import org.chaostocosmos.leap.http.resources.Resources;

/**
 * Interface for servlet
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public interface ILeapService extends IJPAModel, Cloneable {
    /**
     * First entry point of client requets
     * @param httpTransfer
     * @param serviceMethod
     * @throws Exception
     */
    public Response serve(final HttpTransfer httpTransfer, final Method serviceMethod) throws Throwable;

    /**
     * Set filters
     * @param preFilters
     * @param postFilters
     * @throws WASException
     */
    public void setFilters(final List<ILeapFilter> preFilters, final List<ILeapFilter> postFilters);

    /**
     * Set Leap security manager object
     * @param serviceManager
     */
    public void setServiceManager(final ServiceManager serviceManager);

    /**
     * Get Resource object
     */
    public Resources getResource();

    /**
     * Get ServiceManager
     * @return
     */
    public ServiceManager getServiceManager();

    /**
     * Send response to client
     * @param response
     * @throws Throwable
     */
    public void sendResponse(final Response response) throws Throwable;

    /**
     * Service error handle method
     * @param response
     * @param t
     */
    public Throwable errorHandling(final Response response, Throwable t) throws Throwable;
}