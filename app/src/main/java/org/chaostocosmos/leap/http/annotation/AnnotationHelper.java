package org.chaostocosmos.leap.http.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.resources.ClassUtils;
import org.chaostocosmos.leap.http.services.filters.IFilter;
import org.chaostocosmos.leap.http.services.servicemodel.ServiceModel;

import ch.qos.logback.classic.Logger;  

/**
 * Annotation helper object
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class AnnotationHelper {
    /**
     * logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Context.getHosts().getDefaultHost().getHostId()); 

    /**
     * Servlet context mapping Map
     */
    //public static Map<String, ILeapServlet> servletContextMappings = null;

    /**
     * Whether matching with path in services
     * @param path
     * @param services
     * @return
     */
    public static boolean pathMatches(String path, List<Object> services) {
        if(methodMatches(path, services) == null) {
            return false;
        }
        return true;
    }

    /**
     * Validate HTTP Method with servlet method annotaion specfied
     * @param type
     * @param path
     * @param servlets
     * @return
     */
    public static boolean vaildateRequestMethod(REQUEST_TYPE type, String path, List<Object> servlets) {
        Method method = methodMatches(path, servlets);
        if(method != null) {
            MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
            if(type != methodDescriptor.mappingMethod()) {
                return false;
            }
        }
        return true;        
    }

    /**
     * Get service method Map
     * @param service
     * @return
     * @throws WASException
     */
    public static Map<String, Method> getServiceMethodMap(ServiceModel service) throws WASException {
        Map<String, Method> methodMap = new HashMap<>();
        ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
        if(serviceDescriptor != null) {
            String sPath = serviceDescriptor.path();
            if(sPath == null) {
                throw new WASException(MSG_TYPE.ERROR, 6, sPath, service.getClass().getName());
            }
            Method[] methods = service.getClass().getDeclaredMethods();
            for(Method method : methods) {
                MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                if(methodDescriptor != null) {
                    String mPath = methodDescriptor.path();
                    if(mPath == null) {
                        throw new WASException(MSG_TYPE.ERROR, 7, mPath, method.getName());
                    }        
                    String fullPath = sPath + mPath;
                    if(methodMap.containsKey(fullPath)) {
                        throw new WASException(MSG_TYPE.ERROR, 8, fullPath, methodMap.get(fullPath).getName());
                    }
                    methodMap.put(fullPath, method);
                }
            }
        }
        return methodMap;
    }

    /**
     * Match Method class matches with path in servlet
     * @param path
     * @param services
     * @return
     */
    public static Method methodMatches(String path, List<Object> services) {
        for(Object service : services) {
            ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.path();
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        String fullPath = sPath + mPath;
                        if(path.equals(fullPath)) {
                            return method;
                        }
                    }
                }
            } else {
                logger.info(Context.getMessages().getInfoMsg(1, service.getClass().getName()));
            }            
        }
        return null;
    }

    /**
     * Matches service object 
     * @param path
     * @param services
     * @return
     */
    public static Object serviceMatches(String path, List<Object> services) {
        for(Object servlet : services) {
            ServiceMapper serviceDescriptor = servlet.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.path();
                Method[] methods = servlet.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        String fullPath = sPath + mPath;
                        if(path.equals(fullPath)) {
                            return servlet;
                        }
                    }
                }
            } else {
                logger.info(Context.getMessages().getInfoMsg(1, servlet.getClass().getName()));
            }            
        }
        return null;
    }    

    /**
     * Get servlet conext mappings Map
     * @param classes
     * @return
     * @throws WASException
     */
    public static Map<String, ServiceModel> getServiceContextMappings(List<String> classes) throws WASException {
        Map<String, ServiceModel> serviceContextMappings = new HashMap<>();
        for(String service : classes) {
            ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.path();
                if(!sPath.substring(0, 1).equals("/") || sPath.substring(sPath.length()-1).equals("/")) {
                    throw new WASException(MSG_TYPE.ERROR, 9);
                }
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        if(!mPath.substring(0, 1).equals("/") || mPath.substring(mPath.length()-1).equals("/")) {
                            throw new WASException(MSG_TYPE.ERROR, 10);
                        }        
                        String fullPath = sPath + mPath;
                        if(serviceContextMappings.containsKey(fullPath)) {
                            throw new WASException(MSG_TYPE.ERROR, 11, new Object[]{service, fullPath});
                        }
                        serviceContextMappings.put(fullPath, (ServiceModel)ClassUtils.instantiate(ClassLoader.getSystemClassLoader(), service));
                    }
                }
            } else {
                logger.info(Context.getMessages().getInfoMsg(1, service.getClass().getName()));
            }                 
        }
        return serviceContextMappings;
    }

    /**
     * Get service method 
     * @param classes
     * @return
     * @throws WASException
     */
    public static Map<String, Method> getServiceHolderMap(List<String> classes) throws WASException {
        Map<String, Method> serviceContextMappings = new HashMap<>();
        for(String service : classes) {
            ServiceMapper serviceMapper = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceMapper != null) {
                String sPath = serviceMapper.path();
                if(!sPath.substring(0, 1).equals("/") || sPath.substring(sPath.length()-1).equals("/")) {
                    throw new WASException(MSG_TYPE.ERROR, 9);
                }
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodMapper = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodMapper != null) {
                        String mPath = methodMapper.path();
                        if(!mPath.substring(0, 1).equals("/") || mPath.substring(mPath.length()-1).equals("/")) {
                            throw new WASException(MSG_TYPE.ERROR, 10);
                        }        
                        String fullPath = sPath + mPath;
                        if(serviceContextMappings.containsKey(fullPath)) {
                            throw new WASException(MSG_TYPE.ERROR, 8, new Object[]{service, fullPath});
                        }
                        serviceContextMappings.put(fullPath, method);
                    }
                }
            } else {
                logger.info(Context.getMessages().getInfoMsg(1, service.getClass().getName()));
            }                 
        }
        return serviceContextMappings;
    }    

    /**
     * Get filter method by annotation
     * @param filter
     * @param annotationClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Method> getFilterMethods(IFilter filter, Class annotationClass) {
        return Arrays.asList(filter.getClass().getDeclaredMethods()).stream().filter(m -> m.getDeclaredAnnotation(annotationClass) != null).collect(Collectors.toList());
    }
}
