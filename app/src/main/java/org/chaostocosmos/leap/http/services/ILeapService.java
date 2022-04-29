package org.chaostocosmos.leap.http.services;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.HttpTransferBuilder.HttpTransfer;
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
    public HttpResponseDescriptor serve(HttpTransfer httpTransfer, Method serviceMethod) throws Throwable;

    /**
     * Set filters
     * @param preFilters
     * @param postFilters
     * @throws WASException
     */
    public void setFilters(List<ILeapFilter> preFilters, List<ILeapFilter> postFilters);

    /**
     * Set Leap security manager object
     * @param serviceManager
     */
    public void setServiceManager(ServiceManager serviceManager);

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
     * Service error handle method
     * @param response
     * @param throwable
     */
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable throwable) throws Throwable;
}