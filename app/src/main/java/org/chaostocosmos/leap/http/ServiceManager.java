package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.REQUEST;
import org.chaostocosmos.leap.http.inject.FilterIndicates;
import org.chaostocosmos.leap.http.inject.MethodIndicates;
import org.chaostocosmos.leap.http.inject.ServiceIndicates;
import org.chaostocosmos.leap.http.enums.HTTP;
import org.chaostocosmos.leap.http.resource.ClassUtils;
import org.chaostocosmos.leap.http.resource.LeapURLClassLoader;
import org.chaostocosmos.leap.http.security.SecurityManager;
import org.chaostocosmos.leap.http.service.filter.IFilter;
import org.chaostocosmos.leap.http.service.filter.ISecurityFilter;
import org.chaostocosmos.leap.http.service.filter.ISessionFilter;
import org.chaostocosmos.leap.http.service.model.ServiceModel;
import org.chaostocosmos.leap.http.session.SessionManager;

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
    public static final Logger logger = LoggerFactory.getLogger(Context.hosts().getDefaultHost().getHostId());

    /**
     * Host object 
     */
    Host<?> host;

    /**
     * Leap security manager object
     */
    private SecurityManager userManager;

    /**
     * Session manager
     */
    private SessionManager sessionManager;

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
     * @param host
     * @param userManager 
     * @param sessionManager
     * @param classLoader
     * @throws URISyntaxException
     * @throws IOException
     * @throws NotSupportedException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public ServiceManager(Host<?> host, 
                          SecurityManager userManager, 
                          SessionManager sessionManager, 
                          LeapURLClassLoader classLoader) throws NoSuchMethodException, 
                                                                 SecurityException, 
                                                                 IllegalArgumentException, 
                                                                 InvocationTargetException, 
                                                                 ClassNotFoundException, 
                                                                 InstantiationException, 
                                                                 IllegalAccessException, 
                                                                 IOException, 
                                                                 URISyntaxException, 
                                                                 NotSupportedException {
        this.host = host;
        this.userManager  = userManager;
        this.sessionManager = sessionManager;
        this.classLoader = classLoader;
        initialize();
    }

    /**
     * Initialize ServiceManager
     * @throws IOException
     * @throws URISyntaxException
     * @throws NotSupportedException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public void initialize() throws NoSuchMethodException, 
                                    SecurityException, 
                                    IllegalArgumentException, 
                                    InvocationTargetException, 
                                    ClassNotFoundException, 
                                    InstantiationException, 
                                    IllegalAccessException, 
                                    IOException, 
                                    URISyntaxException, 
                                    NotSupportedException {
        List<Class<? extends ServiceModel>> services = ClassUtils.findAllLeapServices(classLoader, false, host.getDynamicPackageFiltering());
        for(Class<? extends ServiceModel> service : services) {
            ServiceIndicates sm = service.getDeclaredAnnotation(ServiceIndicates.class);
            if(sm != null) {
                String servicePath = sm.path();
                Method[] methods = service.getDeclaredMethods();
                for(Method method : methods) {
                    MethodIndicates mm = method.getDeclaredAnnotation(MethodIndicates.class);
                    if(mm != null) {
                        String contextPath = servicePath + mm.path();
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
        ServiceIndicates sm = serviceModel.getClass().getDeclaredAnnotation(ServiceIndicates.class);
        if(sm == null) {
            return null;
        }
        Method[] methods = serviceModel.getClass().getDeclaredMethods();
        for(Method method : methods) {
            MethodIndicates mm = method.getDeclaredAnnotation(MethodIndicates.class);
            if(mm == null) {
                continue;
            }
            REQUEST rType = mm.method();
            String path = sm.path() + mm.path();
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
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public ServiceHolder createServiceHolder(String contextPath) throws NoSuchMethodException, 
                                                                        SecurityException, 
                                                                        IllegalArgumentException, 
                                                                        InvocationTargetException, 
                                                                        ClassNotFoundException, 
                                                                        InstantiationException, 
                                                                        IllegalAccessException {
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
    public ServiceModel createServiceModel(final String serviceClassName)throws NoSuchMethodException, 
                                                                                SecurityException, 
                                                                                IllegalArgumentException, 
                                                                                InvocationTargetException, 
                                                                                ClassNotFoundException, 
                                                                                InstantiationException, 
                                                                                IllegalAccessException {
        ServiceModel service = newServiceInstance(serviceClassName);
        Method[] methods = service.getClass().getDeclaredMethods();
        for(Method method : methods) {
            MethodIndicates mm = method.getDeclaredAnnotation(MethodIndicates.class);
            if(mm != null) {
                FilterIndicates filterMapper = method.getDeclaredAnnotation(FilterIndicates.class);                
                if(filterMapper != null) {
                    List<IFilter> preFilters = new ArrayList<>();
                    Class<? extends IFilter>[] preFilterClasses = filterMapper.preFilters();
                    for(Class<? extends IFilter> clazz : preFilterClasses) {
                        IFilter f = (IFilter)newFilterInstance(clazz.getName());
                        if(f instanceof ISecurityFilter) {
                            ((ISecurityFilter)f).setSecurityManager(this.userManager);
                        }
                        if(f instanceof ISessionFilter) {
                            ((ISessionFilter)f).setSessionManager(this.sessionManager);
                        }
                        preFilters.add(f);
                    }
                    List<IFilter> postFilters = new ArrayList<>();
                    Class<? extends IFilter>[] postFilterClasses = filterMapper.postFilters();
                    for(Class<? extends IFilter> clazz : postFilterClasses) {
                        IFilter f = (IFilter)newFilterInstance(clazz.getName());
                        if(f instanceof ISecurityFilter) {
                            ((ISecurityFilter)f).setSecurityManager(this.userManager);
                        }
                        if(f instanceof ISessionFilter) {
                            ((ISessionFilter)f).setSessionManager(this.sessionManager);
                        }
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
    public SecurityManager getUserManager() {
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
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws Exception
     */
    public ServiceModel newServiceInstance(String serviceClassName) throws NoSuchMethodException, 
                                                                           SecurityException, 
                                                                           IllegalArgumentException, 
                                                                           InvocationTargetException, 
                                                                           ClassNotFoundException, 
                                                                           InstantiationException, 
                                                                           IllegalAccessException {
        return ClassUtils.<ServiceModel> instantiate(this.classLoader, serviceClassName);
    }

    /**
     * Get filter instance
     * @param filterClassName
     * @return
     * @throws HTTPException
     */
    public IFilter newFilterInstance(String filterClassName) throws NoSuchMethodException, 
                                                                    SecurityException, 
                                                                    IllegalArgumentException, 
                                                                    InvocationTargetException, 
                                                                    ClassNotFoundException, 
                                                                    InstantiationException, 
                                                                    IllegalAccessException {
        return ClassUtils.<IFilter> instantiate(this.classLoader, filterClassName);
    }
}

