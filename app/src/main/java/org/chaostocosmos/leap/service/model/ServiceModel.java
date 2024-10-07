package org.chaostocosmos.leap.service.model;

import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.filter.IPostFilter;
import org.chaostocosmos.leap.filter.IPreFilter;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;
import org.chaostocosmos.leap.service.mgmt.ServiceManager;
import org.chaostocosmos.leap.session.SessionManager;

/**
 * Interface for servlet
 * 
 * @author 9ins
 * @since 2021.09.15
 */
public interface ServiceModel<T, R> extends SpringJPAModel, Cloneable {

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
    public HttpResponse<R> handle(final HttpTransfer<T, R> httpTransfer) throws Exception;

    /**
     * Set pre-filter list
     * @param preFilters
     */
    public void setPreFilters(final List<IPreFilter<HttpRequest<T>>> preFilters);

    /**
     * Set post-filter list
     * @param postFilters
     */
    public void setPostFilters(final List<IPostFilter<HttpResponse<R>>> postFilters);

    /**
     * Get ServiceManager
     * @return
     */
    public ServiceManager<T, R> getServiceManager();

    /**
     * Set Leap security manager object
     * @param serviceManager
     */
    public void setServiceManager(final ServiceManager<T, R> serviceManager);

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
    public String resolvePlaceHolder(String htmlPage, Map<String, Object> placeHolderValueMap);

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
    public void sendResponse(final HttpResponse<R> response) throws Throwable;

    /**
     * Service error handle method
     * @param response
     * @param t
     */
    public Exception errorHandling(final HttpResponse<R> response, Exception e);
}