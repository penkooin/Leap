package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMapper;
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
import org.chaostocosmos.leap.http.services.servicemodel.ServiceModel;

import ch.qos.logback.classic.Logger;

/**
 * Service loader object
 * 
 * @author 9ins
 * @since 2021.09.15
 */
public class ServiceManager {
    /**
     * Logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId());

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
     * Service Holder Map
     */
    Map<String, ServiceHolder> serviceHolderMap = new HashMap<>();

    /**
     * Constructor with 
     * @param host
     * @param userManager 
     * @param classLoader
     * @throws URISyntaxException
     * @throws IOException
     * @throws NotSupportedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public ServiceManager(Host<?> host, 
                          UserManager userManager, 
                          LeapURLClassLoader classLoader) throws 
                                                          IOException, 
                                                          URISyntaxException, 
                                                          NotSupportedException, 
                                                          NoSuchMethodException, 
                                                          SecurityException {
        this.host = host;
        this.userManager  = userManager;
        this.classLoader = classLoader;        
        initialize();
    }

    /**
     * Initialize ServiceManager
     * @throws IOException
     * @throws URISyntaxException
     * @throws NotSupportedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public void initialize() throws 
                             IOException, 
                             URISyntaxException, 
                             NotSupportedException, 
                             NoSuchMethodException, 
                             SecurityException {
        List<Class<? extends ServiceModel>> services = ClassUtils.findAllLeapServices(classLoader, false, host.getDynamicPackageFiltering());
        for(Class<? extends ServiceModel> service : services) {
            ServiceMapper sm = service.getDeclaredAnnotation(ServiceMapper.class);
            if(sm != null) {
                String servicePath = sm.path();
                Method[] methods = service.getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper mm = method.getDeclaredAnnotation(MethodMapper.class);
                    if(mm != null) {
                        String contextPath = servicePath + mm.path();
                        ServiceModel serviceModel = createServiceModel(service.getCanonicalName());
                        ServiceHolder serviceHolder = new ServiceHolder(contextPath, serviceModel, mm.mappingMethod());
                        this.serviceHolderMap.put(contextPath, serviceHolder);
                    }
                }
            }
        }
    }
    
    /**
     * Get service method with request method and context path from service object
     * @param requestType
     * @param contextPath
     * @param serviceModel
     * @return
     */
    public Method getServiceMethod(REQUEST_TYPE requestType, String contextPath, ServiceModel serviceModel) {
        ServiceMapper sm = serviceModel.getClass().getDeclaredAnnotation(ServiceMapper.class);
        if(sm == null) {
            return null;
        }
        Method[] methods = serviceModel.getClass().getDeclaredMethods();
        for(Method method : methods) {
            MethodMapper mm = method.getDeclaredAnnotation(MethodMapper.class);
            if(mm == null) {                
                continue;
            }
            REQUEST_TYPE rType = mm.mappingMethod();
            String path = sm.path() + mm.path();
            if(requestType == rType && path.equals(contextPath)) {
                return method;
            }
        }
        throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Requested context path is not found in Server.  Request METHOD: "+requestType.name()+"  Rquest context path: "+contextPath);
    }

    /**
     * Create ServiceHolder
     * @param contextPath
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public ServiceHolder createServiceHolder(String contextPath) throws NoSuchMethodException, SecurityException {
        if(!this.serviceHolderMap.containsKey(contextPath)) {
            //throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Not exist service mapped with specfied context path: "+contextPath);
            return null;
        }
        ServiceHolder serviceHolder = this.serviceHolderMap.get(contextPath);
        return new ServiceHolder(contextPath, createServiceModel(serviceHolder.getServiceModel().getClass().getCanonicalName()), serviceHolder.getRequestType());
    }
    
    /**
     * Create new instance with service Class object
     * @param serviceClassName
     * @return
     */
    public ServiceModel createServiceModel(final String serviceClassName) {
        ServiceModel service = newServiceInstance(serviceClassName);
        Method[] methods = service.getClass().getDeclaredMethods();
        for(Method method : methods) {
            MethodMapper mm = method.getDeclaredAnnotation(MethodMapper.class);
            if(mm != null) {
                FilterMapper filterMapper = method.getDeclaredAnnotation(FilterMapper.class);                
                if(filterMapper != null) {
                    List<IFilter> preFilters = new ArrayList<>();
                    Class<? extends IFilter>[] preFilterClasses = filterMapper.preFilters();
                    for(Class<? extends IFilter> clazz : preFilterClasses) {
                        IFilter f = (IFilter)newFilterInstance(clazz.getName());
                        f.setUserManager(userManager);
                        preFilters.add(f);
                    }
                    List<IFilter> postFilters = new ArrayList<>();
                    Class<? extends IFilter>[] postFilterClasses = filterMapper.postFilters();
                    for(Class<? extends IFilter> clazz : postFilterClasses) {
                        IFilter f = (IFilter)newFilterInstance(clazz.getName());
                        f.setUserManager(userManager);
                        postFilters.add(f);
                    }    
                    service.setFilters(preFilters, postFilters);
                }
                service.setServiceManager(this);    
                return service;
            }
        }
        return null;
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
     * Get service instance
     * @param serviceClassName
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

