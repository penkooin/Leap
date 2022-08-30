package org.chaostocosmos.leap.http.service.model;

import java.util.List;

import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
import org.chaostocosmos.leap.http.resource.ResourcesModel;
import org.chaostocosmos.leap.http.service.filter.IFilter;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.ServiceManager;
import org.chaostocosmos.leap.http.HTTPException;

/**
 * Interface for servlet
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public interface ServiceModel extends SpringJPAModel, Cloneable {
    /**
     * First entry point of client requets
     * @param httpTransfer
     * @param serviceMethod
     * @throws Exception
     */
    public Response serve(final HttpTransfer httpTransfer) throws Exception;

    /**
     * Set filters
     * @param preFilters
     * @param postFilters
     * @throws HTTPException
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
    public Exception errorHandling(final Response response, Exception e) throws Exception;
}