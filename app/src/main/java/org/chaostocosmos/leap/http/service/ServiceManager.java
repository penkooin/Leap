package org.chaostocosmos.leap.http.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.MSG_TYPE;
import org.chaostocosmos.leap.http.REQUEST_TYPE;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.ClassUtils;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.filter.IFilter;

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
     * Class loader
     */
    private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    /**
     * Leap service Class list
     */
    private static List<Class<? extends ILeapService>> services; 

    /**
     * Filter Class list
     */
    private static List<Class<? extends IFilter>> filters;

    /**
     * ServiceHolder Map
     */
    private static Map<String, ServiceHolder> serviceHolderMap = new HashMap<>();
 
    /**
     * Constructor with 
     * @param serviceBeans
     * @throws WASException
     * @throws URISyntaxException
     * @throws IOException
     */
    public ServiceManager() throws WASException {
        try {
            services = ClassUtils.findAllLeapServices();
            filters = ClassUtils.findAllFilters(IFilter.class);    
        } catch(IOException | URISyntaxException e) {
            throw new WASException(e);
        }
        initialize();
    }

    /**
     * initialize
     */
    private void initialize() {
        for(Class<? extends ILeapService> serviceClass : services) {
            ILeapService service = (ILeapService) ClassUtils.instantiate(serviceClass);
            ServiceMapper sm = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(sm != null) {
                String sPath = sm.path();
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMappper mm = method.getDeclaredAnnotation(MethodMappper.class);
                    if(mm != null) {
                        String mPath = mm.path();
                        REQUEST_TYPE rType = mm.requestMethod();
                        FilterMapper fm = method.getDeclaredAnnotation(FilterMapper.class);                        
                        ServiceHolder serviceHolder;
                        if(fm != null) {           
                            List<IFilter> preFilters = Arrays.asList(fm.preFilters())
                                                                .stream()
                                                                .map(fc -> (IFilter)ClassUtils.instantiate(fc))
                                                                .collect(Collectors.toList());
                            List<IFilter> postFilters = Arrays.asList(fm.postFilters())
                                                                .stream()
                                                                .map(fc -> (IFilter)ClassUtils.instantiate(fc))
                                                                .collect(Collectors.toList());
                            serviceHolder = new ServiceHolder(sPath+mPath, service, rType, method, preFilters, postFilters);
                            serviceHolderMap.put(sPath+mPath, serviceHolder);
                        } else {
                            serviceHolder = new ServiceHolder(sPath+mPath, service, rType, method);
                            serviceHolderMap.put(sPath+mPath, serviceHolder);
                        }
                        logger.info("Add service: "+service.getClass().getName()+"  Mapping path: "+sPath+mPath);
                    } else {
                        logger.debug("Method not mapping with MethodMapper annotation: "+method.getName());
                    }
                }
            } else {
                logger.debug("Service not mapping with ServiceMapper annotation: "+service.getClass().getName());
            }
        }
    }

    /**
     * Validate request method
     * @param type
     * @param contextPath
     * @return
     * @throws WASException
     */
    public boolean vaildateRequestMethod(REQUEST_TYPE type, final String contextPath) throws WASException {
        Method sMethod = getMappingServiceMethod(contextPath);
        if(sMethod.getDeclaredAnnotation(MethodMappper.class).requestMethod() != type) {
            throw new WASException(MSG_TYPE.ERROR, 34, type.name());
        }
        return true;
    }

    /**
     * Get ServiceHolder object mapping with context path
     * @param contextPath
     * @return
     */
    public ServiceHolder getMappingServiceHolder(String contextPath) {
        return serviceHolderMap.get(contextPath);
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
    public static IFilter newFilterInstance(String filterClassName) throws WASException {
        return (IFilter)ClassUtils.instantiate(filterClassName);
    }
}
