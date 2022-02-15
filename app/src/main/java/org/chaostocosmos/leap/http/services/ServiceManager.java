package org.chaostocosmos.leap.http.services;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.ClassUtils;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.ILeapFilter;
import org.chaostocosmos.leap.http.security.UserManager;

import ch.qos.logback.classic.Logger;

/**
 * Service loader object
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class ServiceManager {
    /**
     * Logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Context.getDefaultHost());
    /**
     * ServiceHolder Map
     */
    private static Map<String, ServiceHolder> serviceHolderMap = new HashMap<>();
    /**
     * Leap security manager object
     */
    private static UserManager securityManager;
 
    /**
     * Constructor with 
     * @param serviceBeans
     * @throws WASException
     */
    public ServiceManager(UserManager securityManager_) throws WASException {
        try {
            List<Class<? extends ILeapService>> services = ClassUtils.findAllLeapServices(false);
            //List<Class<? extends IFilter>> filters = ClassUtils.findAllLeapFilters(false);    
            securityManager  = securityManager_;
            initialize(services);
        } catch(IOException | URISyntaxException e) {
            throw new WASException(e);
        }
    }

    /**
     * initialize
     */
    private void initialize(List<Class<? extends ILeapService>> services) {        
        for(Class<? extends ILeapService> serviceClass : services) {
            ILeapService service = (ILeapService) ClassUtils.instantiate(serviceClass);
            service.setSecurityManager(securityManager);
            setService(service);
        }
    }

    /**
     * Set service holder
     * @param service
     */
    public void setService(final ILeapService service) {
        ServiceMapper sm = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
        if(sm != null) {
            String sPath = sm.path();
            Method[] methods = service.getClass().getDeclaredMethods();
            for(Method method : methods) {
                MethodMappper mm = method.getDeclaredAnnotation(MethodMappper.class);
                if(mm != null) {
                    String mPath = mm.path();
                    REQUEST_TYPE rType = mm.mappingMethod();
                    logger.debug(service.toString()+" : "+rType.name());
                    FilterMapper fm = method.getDeclaredAnnotation(FilterMapper.class);
                    ServiceHolder serviceHolder;
                    if(fm != null) {
                        List<ILeapFilter> preFilters = new ArrayList<>();
                        Class<? extends ILeapFilter>[] preFilterClasses = fm.preFilters();
                        for(Class<? extends ILeapFilter> clazz : preFilterClasses) {
                            ILeapFilter f = (ILeapFilter)ClassUtils.instantiate(clazz);
                            f.setSecurityManager(securityManager);
                            preFilters.add(f);
                        }
                        List<ILeapFilter> postFilters = new ArrayList<>();
                        Class<? extends ILeapFilter>[] postFilterClasses = fm.postFilters();
                        for(Class<? extends ILeapFilter> clazz : postFilterClasses) {
                            ILeapFilter f = (ILeapFilter)ClassUtils.instantiate(clazz);
                            f.setSecurityManager(securityManager);
                            postFilters.add(f);
                        }
                        serviceHolder = new ServiceHolder(sPath+mPath, service, rType, method, preFilters, null);
                        serviceHolderMap.put(sPath+mPath, serviceHolder);
                    } else {
                        serviceHolder = new ServiceHolder(sPath+mPath, service, rType, method);
                        serviceHolderMap.put(sPath+mPath, serviceHolder);
                    }
                    logger.info("Add service: "+service.getClass().getName()+"  Mapping path: "+sPath+mPath);
                } else {
                    logger.debug("Method not mapped with MethodMapper: "+method.getName());
                }
            }
        } else {
            logger.debug("Service not mapped with ServiceMapper: "+service.getClass().getName());
        }
    }

    /**
     * Remove ServiceHolder for specified context path
     * @param contextPath
     * @return
     */
    public boolean removeService(String contextPath) {
        return serviceHolderMap.remove(contextPath) != null ? true : false;
    }

    /**
     * Validate request method
     * @param type
     * @param contextPath
     * @return
     * @throws WASException
     */
    public boolean vaildateRequestMethod(REQUEST_TYPE type, final String contextPath) throws WASException {
        ServiceHolder serviceHolder = serviceHolderMap.get(contextPath);
        if(serviceHolder.getRequestType() != type) {
            throw new WASException(MSG_TYPE.ERROR, 34, type.name());
        }
        return true;
    }

    /**
     * Get ServiceHolder object mapping with context path
     * @param contextPath
     * @return
     * @throws WASException
     */
    public ServiceHolder getMappingServiceHolder(String contextPath) throws WASException {
        ServiceHolder serviceHolder = serviceHolderMap.get(contextPath);
        return serviceHolder;
    }

    /**
     * Get ILeapService mapping with context path
     * @param contextPath
     * @return
     */
    public ILeapService getMappingService(String contextPath) {
        return serviceHolderMap.get(contextPath).getService();
    }

    /**
     * Get service object matching with specfied context path
     * @param contextPath
     * @return
     * @throws WASException
     */
    public Method getMappingServiceMethod(String contextPath) throws WASException {
        return serviceHolderMap.get(contextPath).getServiceMethod();
    }

    /**
     * Get service instance
     * @param qualifiedClassName
     * @return
     * @throws Exception
     */
    public static ILeapService newServiceInstance(String serviceClassName) throws WASException {
        return (ILeapService)ClassUtils.instantiate(serviceClassName);
    }   

    /**
     * Get filter instance
     * @param filterClassName
     * @return
     * @throws WASException
     */
    public static ILeapFilter newFilterInstance(String filterClassName) throws WASException {
        return (ILeapFilter)ClassUtils.instantiate(filterClassName);
    }
}

