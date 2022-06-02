package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.ClassUtils;
import org.chaostocosmos.leap.http.resources.LeapURLClassLoader;
import org.chaostocosmos.leap.http.services.filters.IFilter;
import org.chaostocosmos.leap.http.services.model.ServiceModel;

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
    public static final Logger logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId());

    /**
     * ServiceHolder Map
     */
    private Map<String, ServiceHolder> serviceHolderMap = new HashMap<>();

    /**
     * Host object 
     */
    Host<?> host;

    /**
     * Leap security manager object
     */
    private UserManager userManager;

    /**
     * ClassLoder for host
     */
    private LeapURLClassLoader classLoader;

    /**
     * Constructor with 
     * @param host
     * @param userManager 
     * @param classLoader
     */
    public ServiceManager(Host<?> host, UserManager userManager, LeapURLClassLoader classLoader) {
        this.host = host;
        this.userManager  = userManager;
        this.classLoader = (LeapURLClassLoader) classLoader;
        try {
            List<Class<? extends ServiceModel>> services = ClassUtils.findAllLeapServices(classLoader, false, host.getDynamicPackageFiltering());
            //List<Class<? extends IFilter>> filters = ClassUtils.findAllLeapFilters(false); 
            initialize(services);
        } catch(IOException | URISyntaxException e) {
            throw new WASException(e);
        }
    }

    /**
     * initialize
     */
    private void initialize(List<Class<? extends ServiceModel>> services) {        
        for(Class<? extends ServiceModel> serviceClass : services) {
            ServiceModel service = (ServiceModel) ClassUtils.instantiate(classLoader, serviceClass);
            addService(service);
        }
    }

    /**
     * Set service holder
     * @param service
     */
    public void addService(final ServiceModel service) {
        service.setServiceManager(this);
        ServiceMapper sm = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
        if(sm != null) {
            String sPath = sm.path();
            Method[] methods = service.getClass().getDeclaredMethods();
            for(Method method : methods) {
                MethodMappper mm = method.getDeclaredAnnotation(MethodMappper.class);
                if(mm != null) {
                    String sKey = sPath + mm.path();
                    if(!this.serviceHolderMap.containsKey(sKey)) {
                        logger.info("Add service: "+service.getClass().getName()+"  Mapping path: "+ sKey + "   METHOD: "+mm.mappingMethod());
                    }
                    REQUEST_TYPE rType = mm.mappingMethod();
                    FilterMapper fm = method.getDeclaredAnnotation(FilterMapper.class);
                    ServiceHolder serviceHolder;
                    if(fm != null) {
                        List<IFilter> preFilters = new ArrayList<>();
                        Class<? extends IFilter>[] preFilterClasses = fm.preFilters();
                        for(Class<? extends IFilter> clazz : preFilterClasses) {
                            IFilter f = (IFilter)newFilterInstance(clazz.getName());
                            f.setUserManager(userManager);
                            preFilters.add(f);
                        }
                        List<IFilter> postFilters = new ArrayList<>();
                        Class<? extends IFilter>[] postFilterClasses = fm.postFilters();
                        for(Class<? extends IFilter> clazz : postFilterClasses) {
                            IFilter f = (IFilter)newFilterInstance(clazz.getName());
                            f.setUserManager(userManager);
                            postFilters.add(f);
                        }
                        service.setFilters(preFilters, postFilters);
                        service.setServiceManager(this);
                        serviceHolder = new ServiceHolder(sKey, service, rType, method);
                        serviceHolderMap.put(sKey, serviceHolder);
                    } else {
                        serviceHolder = new ServiceHolder(sKey, service, rType, method);
                        serviceHolderMap.put(sKey, serviceHolder);
                    }
                } else {
                    //logger.debug("Method not mapped with MethodMapper: "+method.getName());
                }
            }
        } else {
            logger.debug("Service not mapped with ServiceMapper: "+service.getClass().getName());
        }
    }

    /**
     * Get user manager object
     * @return
     */
    public UserManager getUserManager() {
        return this.userManager;
    }

    /**
     * Get Host
     * @return
     */
    public Host<?> getHost() {
        return this.host;
    }

    /**
     * Get class loader object
     * @return
     */
    public LeapURLClassLoader getClassLoader() {
        return this.classLoader;
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
    public boolean vaildateRequestMethod(REQUEST_TYPE type, final String contextPath) throws Exception {
        ServiceHolder serviceHolder = serviceHolderMap.get(contextPath);
        if(serviceHolder.getRequestType() != type) {
            throw new WASException(MSG_TYPE.ERROR, 15, type.name());
        }
        return true;
    }

    /**
     * Get ServiceHolder object mapping with context path
     * @param contextPath
     * @return
     * @throws WASException
     */
    public ServiceHolder getMappingServiceHolder(String contextPath) throws Exception {
        ServiceHolder serviceHolder = serviceHolderMap.get(contextPath);
        return serviceHolder;
    }

    /**
     * Get ILeapService mapping with context path
     * @param contextPath
     * @return
     */
    public ServiceModel getMappingService(String contextPath) {
        return serviceHolderMap.get(contextPath).getService();
    }

    /**
     * Get service object matching with specfied context path
     * @param contextPath
     * @return
     * @throws WASException
     */
    public Method getMappingServiceMethod(String contextPath) throws Exception {
        return serviceHolderMap.get(contextPath).getServiceMethod();
    }

    /**
     * Remove service instance by service qualifiedClassName
     * @param qualifiedClassName
     */
    public void removeServiceInstance(final String qualifiedClassName) {
        serviceHolderMap.entrySet().stream().filter(e -> e.getValue().getService().getClass().getName().equals(qualifiedClassName)).findFirst().orElseThrow(() -> new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), "There isn't exist service: " + qualifiedClassName));
    }    

    /**
     * Get service instance
     * @param qualifiedClassName
     * @return
     * @throws Exception
     */
    public ServiceModel newServiceInstance(String serviceClassName) {
        return (ServiceModel)ClassUtils.instantiate(this.classLoader, serviceClassName);
    }   

    /**
     * Get filter instance
     * @param filterClassName
     * @return
     * @throws WASException
     */
    public IFilter newFilterInstance(String filterClassName) {
        return (IFilter)ClassUtils.instantiate(this.classLoader, filterClassName);
    }
}

