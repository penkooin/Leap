package org.chaostocosmos.leap.http.services.servicemodel;

import java.util.List;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.ServiceManager;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.resources.ResourcesModel;
import org.chaostocosmos.leap.http.services.filters.IFilter;

/**
 * Interface for servlet
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public interface ServiceModel extends SpringJPAModel, Cloneable {
    /**
     * First entry point of client requets
     * @param httpTransfer
     * @throws Exception
     */
    public Response serve(final HttpTransfer httpTransfer) throws Throwable;

    /**
     * Set filters
     * @param preFilters
     * @param postFilters
     * @throws WASException
     */
    public void setFilters(final List<IFilter> preFilters, final List<IFilter> postFilters);

    /**
     * Set Leap security manager object
     * @param serviceManager
     */
    public void setServiceManager(final ServiceManager serviceManager);

    /**
     * Get Resource object
     */
    public ResourcesModel getResource();

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