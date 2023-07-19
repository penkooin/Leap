package org.chaostocosmos.leap.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.ClassUtils;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.service.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;
import org.hibernate.MappingException;

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
    public static final Logger logger = LoggerFactory.getLogger(Context.get().hosts().getDefaultHost().getHostId()); 

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
    public static boolean vaildateRequestMethod(REQUEST type, String path, List<Object> servlets) {
        Method method = methodMatches(path, servlets);
        if(method != null) {
            MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
            if(type != methodDescriptor.method()) {
                return false;
            }
        }
        return true;        
    }

    /**
     * Get service method Map
     * @param service
     * @return
     * @throws LeapException
     */
    public static Map<String, Method> getServiceMethodMap(ServiceModel service) throws LeapException {
        Map<String, Method> methodMap = new HashMap<>();
        ServiceMapper serviceIndicator = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
        if(serviceIndicator != null) {
            String sPath = serviceIndicator.mappingPath();
            if(sPath == null) {
                throw new LeapException(HTTP.RES500, new MappingException("Service mapping path is not found: "+service.getClass().getName()));
            }
            Method[] methods = service.getClass().getDeclaredMethods();
            for(Method method : methods) {
                MethodMapper methodIndicator = method.getDeclaredAnnotation(MethodMapper.class);
                if(methodIndicator != null) {
                    String mPath = methodIndicator.mappingPath();
                    if(mPath == null) {
                        throw new LeapException(HTTP.RES500, new MappingException("Method mapping path is not found: "+method.getName()));
                    }        
                    String fullPath = sPath + mPath;
                    if(methodMap.containsKey(fullPath)) {
                        throw new LeapException(HTTP.RES500, new MappingException("Duplicate mapping path: "+methodMap.get(fullPath).getName()));
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
                String sPath = serviceDescriptor.mappingPath();
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.mappingPath();
                        String fullPath = sPath + mPath;
                        if(path.equals(fullPath)) {
                            return method;
                        }
                    }
                }
            } else {
                logger.info("Service mapper is not found: "+service.getClass().getName());
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
        for(Object service : services) {
            ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.mappingPath();
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.mappingPath();
                        String fullPath = sPath + mPath;
                        if(path.equals(fullPath)) {
                            return service;
                        }
                    }
                }
            } else {
                logger.info("Service mapper is not found: "+service.getClass().getName());
            }            
        }
        return null;
    }    

    /**
     * Get servlet conext mappings Map
     * @param classes
     * @return
     * @throws LeapException
     */
    public static Map<String, ServiceModel> getServiceContextMappings(List<String> classes) throws Exception {
        Map<String, ServiceModel> serviceContextMappings = new HashMap<>();
        for(String service : classes) {
            ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.mappingPath();
                if(!sPath.substring(0, 1).equals("/") || sPath.substring(sPath.length()-1).equals("/")) {
                    throw new MappingException("Service mapping path format is wrong: "+sPath);
                }
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodDescriptor = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.mappingPath();
                        if(!mPath.substring(0, 1).equals("/") || mPath.substring(mPath.length()-1).equals("/")) {
                            throw new MappingException("Method mapping path format is wrong: "+mPath);
                        }        
                        String fullPath = sPath + mPath;
                        if(serviceContextMappings.containsKey(fullPath)) {
                            throw new MappingException("Duplicate mapping path: "+fullPath);
                        }
                        serviceContextMappings.put(fullPath, (ServiceModel) ClassUtils.instantiate(ClassLoader.getSystemClassLoader(), service));
                    }
                }
            } else {
                logger.info("Service mapper is not found: "+service.getClass().getName());
            }                 
        }
        return serviceContextMappings;
    }

    /**
     * Get service method 
     * @param classes
     * @return
     * @throws LeapException
     */
    public static Map<String, Method> getServiceHolderMap(List<String> classes) throws Exception {
        Map<String, Method> serviceContextMappings = new HashMap<>();
        for(String service : classes) {
            ServiceMapper serviceMapper = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceMapper != null) {
                String sPath = serviceMapper.mappingPath();
                if(!sPath.substring(0, 1).equals("/") || sPath.substring(sPath.length()-1).equals("/")) {
                    throw new MappingException("Service mapping path format is wrong: "+sPath);
                }
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMapper methodMapper = method.getDeclaredAnnotation(MethodMapper.class);
                    if(methodMapper != null) {
                        String mPath = methodMapper.mappingPath();
                        if(!mPath.substring(0, 1).equals("/") || mPath.substring(mPath.length()-1).equals("/")) {
                            throw new MappingException("Method mapping path format is wrong: "+mPath);
                        }        
                        String fullPath = sPath + mPath;
                        if(serviceContextMappings.containsKey(fullPath)) {
                            throw new MappingException("Duplicate mapping path: "+fullPath);
                        }
                        serviceContextMappings.put(fullPath, method);
                    }
                }
            } else {
                logger.info("Service mapper is not found: "+service.getClass().getName());
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
    public static <T> List<Method> getFilterMethods(IFilter filter, Class<? extends Annotation> annotationClass) {
        return Arrays.asList(filter.getClass().getDeclaredMethods()).stream().filter(m -> m.getDeclaredAnnotation(annotationClass) != null).collect(Collectors.toList());
    }
}
