package org.chaostocosmos.leap.http;

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
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.resource.ClassUtils;
import org.chaostocosmos.leap.resource.LeapURLClassLoader;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.service.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;
import org.chaostocosmos.leap.session.SessionManager;

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
    private static final Logger logger = LoggerFactory.getLogger(Context.hosts().getDefaultHost().getHostId());

    /**
     * Host object 
     */
    Host<?> host;

    /**
     * Leap security manager object
     */
    private org.chaostocosmos.leap.security.SecurityManager securityManager;

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
     * Service Holder Map
     */
    Map<String, ServiceHolder> serviceHolderMap = new HashMap<>();

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
    public ServiceManager(Host<?> host, org.chaostocosmos.leap.security.SecurityManager securityManager, SessionManager sessionManager, ResourcesModel resourcesModel, LeapURLClassLoader classLoader) throws IOException, URISyntaxException, NotSupportedException {
        this.host = host;
        this.securityManager  = securityManager;
        this.sessionManager = sessionManager;
        this.resourcesModel = resourcesModel;
        this.classLoader = classLoader;
        //initialize();
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
                        ServiceModel serviceModel = createServiceModel(service.getCanonicalName());
                        ServiceHolder serviceHolder = new ServiceHolder(contextPath, serviceModel, mm.method());
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
        throw new HTTPException(HTTP.RES404, "Requested context path is not found in Server.  Request METHOD: "+requestType.name()+"  Rquest context path: "+contextPath);
    }

    /**
     * Create ServiceHolder
     * @param contextPath
     * @return
     */
    public ServiceHolder createServiceHolder(String contextPath) {
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
                service.setServiceManager(this);
                service.setSessionManager(this.sessionManager);
                service.setResourcesModel(this.resourcesModel);
                PreFilters preFilters = method.getDeclaredAnnotation(PreFilters.class);
                List<IFilter> preFilterList = Arrays.asList(preFilters.filterClasses()).stream().map(c -> newFilterInstance(c.getName())).collect(Collectors.toList());
                service.setPreFilters(preFilterList);
                PostFilters postFilters = method.getDeclaredAnnotation(PostFilters.class);
                List<IFilter> postFilterList = Arrays.asList(postFilters.filterClasses()).stream().map(c -> newFilterInstance(c.getName())).collect(Collectors.toList());
                service.setPostFilters(postFilterList);                
                return service;
            }
        }
        return null;
    }

    /**
     * Get user manager object
     * @return
     */
    public org.chaostocosmos.leap.security.SecurityManager getSecurityManager() {
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
     * Get service instance
     * @param serviceClassName
     * @return
     */
    public ServiceModel newServiceInstance(String serviceClassName) {
        try {
            return ClassUtils.<ServiceModel> instantiate(this.classLoader, serviceClassName);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException
                | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get filter instance
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

