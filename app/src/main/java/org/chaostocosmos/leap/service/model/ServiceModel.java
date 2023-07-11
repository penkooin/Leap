package org.chaostocosmos.leap.service.model;

import java.util.List;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.manager.ServiceManager;
import org.chaostocosmos.leap.manager.SessionManager;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.service.filter.IFilter;

/**
 * Interface for servlet
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public interface ServiceModel extends SpringJPAModel, Cloneable {

    /**
     * Get Host object using this service 
     * @return
     */
    public Host<?> getHost();

    /**
     * Get Host object to the service
     * @param host
     */
    public void setHost(Host<?> host);

    /**
     * First entry point of client requets
     * @param httpTransfer
     * @throws Exception
     */
    public HttpResponse handle(final HttpTransfer httpTransfer);

    /**
     * Set pre-filter list
     * @param preFilters
     */
    public void setPreFilters(final List<IFilter> preFilters);

    /**
     * Set post-filter list
     * @param postFilters
     */
    public void setPostFilters(final List<IFilter> postFilters);

    /**
     * Get ServiceManager
     * @return
     */
    public ServiceManager getServiceManager();

    /**
     * Set Leap security manager object
     * @param serviceManager
     */
    public void setServiceManager(final ServiceManager serviceManager);

    /**
     * Get Resource object
     * @return
     */
    public ResourcesModel getResourcesModel();

    /**
     * Set ResourcesModel
     * @param resourcesModel
     */
    public void setResourcesModel(ResourcesModel resourcesModel);

    /**
     * Get SessionManager
     * @return
     */
    public SessionManager getSessionManager();

    /**
     * Set SessionManager
     */
    public void setSessionManager(SessionManager sessionManager);

    /**
     * Send response to client
     * @param response
     * @throws Throwable
     */
    public void sendResponse(final HttpResponse response) throws Throwable;

    /**
     * Service error handle method
     * @param response
     * @param t
     */
    public Exception errorHandling(final HttpResponse response, Exception e) throws Exception;
}