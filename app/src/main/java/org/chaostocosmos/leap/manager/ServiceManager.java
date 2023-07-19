package org.chaostocosmos.leap.manager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.PostFilters;
import org.chaostocosmos.leap.annotation.PreFilters;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.common.ClassUtils;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.resource.LeapURLClassLoader;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.service.ServiceHolder;
import org.chaostocosmos.leap.service.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;

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
    private static final Logger logger = LoggerFactory.getLogger(Context.get().hosts().getDefaultHost().getHostId());

    /**
     * Host object 
     */
    Host<?> host;

    /**
     * Leap security manager object
     */
    private org.chaostocosmos.leap.manager.SecurityManager securityManager;

    /**
     * Session manager
     */
    private SessionManager sessionManager;

    /**
     * ResourcesModel object
     */
    private ResourcesModel resourcesModel;

    /**
     * ClassLoder for host
     */
    private LeapURLClassLoader classLoader;

    /**
     * Context path with Service model mapping Map
     */
    Map<String, Class<? extends ServiceModel>> serviceHolderMap = new HashMap<>();

    /**
     * Constructor
     * 
     * @param host
     * @param securityManager 
     * @param sessionManager 
     * @param resourcesModel
     * @param classLoader
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws IOException
     */
    public ServiceManager(Host<?> host, org.chaostocosmos.leap.manager.SecurityManager securityManager, SessionManager sessionManager, ResourcesModel resourcesModel, LeapURLClassLoader classLoader) throws IOException, URISyntaxException, NotSupportedException {
        this.host = host;
        this.securityManager  = securityManager;
        this.sessionManager = sessionManager;
        this.resourcesModel = resourcesModel;
        this.classLoader = classLoader;
        initialize();
    }

    /**
     * Initialize ServiceManager
     * @throws NotSupportedException
     * @throws URISyntaxException
     * @throws IOException
     */
    public void initialize() throws IOException, URISyntaxException, NotSupportedException {
        List<Class<? extends ServiceModel>> services = ClassUtils.findAllLeapServices(classLoader, false, host.getDynamicPackageFiltering());
        for(Class<? extends ServiceModel> service : services) {
            ServiceMapper sm = service.getDeclaredAnnotation(ServiceMapper.class);
            if(sm != null) {
                String servicePath = sm.mappingPath();
                Method[] methods = service.getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper mm = method.getDeclaredAnnotation(MethodMapper.class);
                    if(mm != null) {
                        String contextPath = servicePath + mm.mappingPath();
                        this.serviceHolderMap.put(contextPath, service);
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
    public Method getServiceMethod(REQUEST requestType, String contextPath, ServiceModel serviceModel) {        
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
            REQUEST rType = mm.method();
            String path = sm.mappingPath() + mm.mappingPath();
            if(requestType == rType && path.equals(contextPath)) {
                return method;
            }
        }
        throw new LeapException(HTTP.RES404, "Requested context path is not found in Server.  Request METHOD: "+requestType.name()+"  Rquest context path: "+contextPath);
    }

    /**
     * Create ServiceHolder
     * @param contextPath
     * @return
     */
    public ServiceHolder createServiceHolder(String contextPath) {        
        if(!this.serviceHolderMap.containsKey(contextPath)) {
            this.host.getLogger().debug("Service Holder not found. request path: "+contextPath);
            return null;
        }
        Class<? extends ServiceModel> serviceModel = this.serviceHolderMap.get(contextPath); 
        return new ServiceHolder(contextPath, createServiceModel(serviceModel));
    }
    
    /**
     * Create new instance with service Class object
     * @param serviceClassName
     * @return
     */
    public ServiceModel createServiceModel(final Class<? extends ServiceModel> serviceModelClass) {
        ServiceModel serviceModel = newServiceInstance(serviceModelClass);
        serviceModel.setHost(this.host);        
        Method[] methods = serviceModel.getClass().getDeclaredMethods();
        for(Method method : methods) {
            MethodMapper mm = method.getDeclaredAnnotation(MethodMapper.class);
            if(mm != null) {                
                serviceModel.setServiceManager(this);
                serviceModel.setSessionManager(this.sessionManager);
                serviceModel.setResourcesModel(this.resourcesModel);
                PreFilters preFilters = method.getDeclaredAnnotation(PreFilters.class);
                if(preFilters != null) {
                    List<IFilter> preFilterList = Arrays.asList(preFilters.filterClasses()).stream().map(c -> newFilterInstance(c.getName())).collect(Collectors.toList());
                    serviceModel.setPreFilters(preFilterList);
                }
                PostFilters postFilters = method.getDeclaredAnnotation(PostFilters.class);
                if(postFilters != null) {
                    List<IFilter> postFilterList = Arrays.asList(postFilters.filterClasses()).stream().map(c -> newFilterInstance(c.getName())).collect(Collectors.toList());
                    serviceModel.setPostFilters(postFilterList);                
                }
                return serviceModel;
            }
        }
        return null;
    }

    /**
     * Get user manager object
     * @return
     */
    public org.chaostocosmos.leap.manager.SecurityManager getSecurityManager() {
        return this.securityManager;
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
     * Create service instance
     * @param serviceModelClass
     * @return
     */
    public ServiceModel newServiceInstance(Class<? extends ServiceModel> serviceModelClass) {
        try {
            ServiceModel serviceModel = ClassUtils.<ServiceModel> instantiate(this.classLoader, serviceModelClass);
            return serviceModel;
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException
                | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create service instance
     * @param serviceClassName
     * @return
     */
    public ServiceModel newServiceInstance(String serviceClassName) {
        try {
            ServiceModel serviceModel = ClassUtils.<ServiceModel> instantiate(this.classLoader, serviceClassName);
            return serviceModel;
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException
                | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create filter instance
     * @param filterClassName
     * @return
     */
    public IFilter newFilterInstance(String filterClassName) {
        try {
            return ClassUtils.<IFilter> instantiate(this.classLoader, filterClassName);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException
                | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

