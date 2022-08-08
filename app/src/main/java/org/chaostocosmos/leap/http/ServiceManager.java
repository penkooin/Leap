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
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.resources.ClassUtils;
import org.chaostocosmos.leap.http.resources.LeapURLClassLoader;
import org.chaostocosmos.leap.http.services.filters.IFilter;
import org.chaostocosmos.leap.http.services.model.ServiceModel;

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
     * Services Method Map
     */
    Map<String, Method> serviceMethodMap = new HashMap<>();    

    /**
     * Service Holder Map
     */
    Map<String, ServiceHolder> serviceHolderMap = new HashMap<>();

    /**
     * Constructor with 
     * 
     * @param host
     * @param userManager 
     * @param classLoader
     * @throws URISyntaxException
     * @throws IOException
     * @throws NotSupportedException
     */
    public ServiceManager(Host<?> host, UserManager userManager, LeapURLClassLoader classLoader) throws IOException, URISyntaxException, NotSupportedException {
        this.host = host;
        this.userManager  = userManager;
        this.classLoader = classLoader;        
        initialize();        
    }
    /**
     * Initialize ServiceManager
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws NotSupportedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public void initialize() throws IOException, URISyntaxException, NotSupportedException, NoSuchMethodException, SecurityException {
        List<Class<? extends ServiceModel>> services = ClassUtils.findAllLeapServices(classLoader, false, host.getDynamicPackageFiltering());
        for(Class<? extends ServiceModel> service : services) {            
            ServiceMapper sm = service.getDeclaredAnnotation(ServiceMapper.class);
            if(sm != null) {
                String servicePath = sm.path();
                Method[] methods = service.getDeclaredMethods();
                for(Method method : methods) {
                    MethodMappper mm = method.getDeclaredAnnotation(MethodMappper.class);
                    if(mm != null) {
                        String contextPath = servicePath + mm.path();
                        this.serviceMethodMap.put(contextPath, method);
                        this.serviceHolderMap.put(contextPath, createServiceHolder(contextPath));
                    }
                }
            }
        }
    }

    /**
     * Create ServiceHolder
     * 
     * @param contextPath
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public ServiceHolder createServiceHolder(String contextPath) throws NoSuchMethodException, SecurityException {
        if(!this.serviceHolderMap.containsKey(contextPath)) {
            return null;
        }
        Method serviceMethod = this.serviceMethodMap.get(contextPath);
        ServiceModel service = createService(serviceMethod);
        serviceMethod = service.getClass().getDeclaredMethod(serviceMethod.getName(), serviceMethod.getParameterTypes());
        REQUEST_TYPE requestType = serviceMethod.getDeclaredAnnotation(MethodMappper.class).mappingMethod();
        return new ServiceHolder(contextPath, service, requestType, serviceMethod);
    }
    /**
     * Create new instance of service mapping with context path
     * @param contextPath
     */
    public ServiceModel createService(String contextPath) {
        Method serviceMethod = this.serviceMethodMap.get(contextPath);
        if(serviceMethod == null) {
            return null;
        }
        return createService(this.serviceMethodMap.get(contextPath));
    }
    /**
     * Create new instance with service Class object
     * @param contextPath
     * @return
     */
    public ServiceModel createService(final Method serviceMethod) {
        ServiceModel service = newServiceInstance(serviceMethod.getDeclaringClass().getCanonicalName());
        Method[] methods = service.getClass().getDeclaredMethods();
        for(Method method : methods) {
            MethodMappper mm = method.getDeclaredAnnotation(MethodMappper.class);
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

