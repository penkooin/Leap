package org.chaostocosmos.leap.service.model;

import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.filter.IFilter;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;
import org.chaostocosmos.leap.service.mgmt.ServiceManager;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.session.SessionManager;

/**
 * Interface for servlet
 * 
 * @author 9ins
 * @since 2021.09.15
 */
public interface ServiceModel extends SpringJPAModel, Cloneable {

    /**
     * Get Host object using this service 
     * @return
     */
    public Host<?> getHost();

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
    public ResourcesWatcherModel getResourcesModel();

    /**
     * Set ResourcesModel
     * @param resourcesModel
     */
    public void setResourcesModel(ResourcesWatcherModel resourcesModel);

    /**
     * Resolving placeholder with parameter Map
     * @param htmlPage
     * @param placeHolderValueMap
     * @return
     */
    public String resolvePlaceHolder(String htmlPage, Map<String, ?> placeHolderValueMap);

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