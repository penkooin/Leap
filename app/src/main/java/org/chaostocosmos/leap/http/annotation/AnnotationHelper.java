package org.chaostocosmos.leap.http.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.ClassUtils;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.IFilter;
import org.chaostocosmos.leap.http.services.ILeapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

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
    public static final Logger logger = LoggerFactory.getLogger(Context.getDefaultHost());

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
            MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
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
    public static Map<String, Method> getServiceMethodMap(ILeapService service) throws WASException {
        Map<String, Method> methodMap = new HashMap<>();
        ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
        if(serviceDescriptor != null) {
            String sPath = serviceDescriptor.path();
            if(sPath == null) {
                throw new WASException(MSG_TYPE.ERROR, 35, sPath, service.getClass().getName());
            }
            Method[] methods = service.getClass().getDeclaredMethods();
            for(Method method : methods) {
                MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
                if(methodDescriptor != null) {
                    String mPath = methodDescriptor.path();
                    if(mPath == null) {
                        throw new WASException(MSG_TYPE.ERROR, 36, mPath, method.getName());
                    }        
                    String fullPath = sPath + mPath;
                    if(methodMap.containsKey(fullPath)) {
                        throw new WASException(MSG_TYPE.ERROR, 37, fullPath, methodMap.get(fullPath).getName());
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
                    MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        String fullPath = sPath + mPath;
                        if(path.equals(fullPath)) {
                            return method;
                        }
                    }
                }
            } else {
                logger.info(Context.getInfoMsg(1, service.getClass().getName()));
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
                    MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        String fullPath = sPath + mPath;
                        if(path.equals(fullPath)) {
                            return servlet;
                        }
                    }
                }
            } else {
                logger.info(Context.getInfoMsg(1, servlet.getClass().getName()));
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
    public static Map<String, ILeapService> getServiceContextMappings(List<String> classes) throws WASException {
        Map<String, ILeapService> serviceContextMappings = new HashMap<>();
        for(String service : classes) {
            ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.path();
                if(!sPath.substring(0, 1).equals("/") || sPath.substring(sPath.length()-1).equals("/")) {
                    throw new WASException(MSG_TYPE.ERROR, 28);
                }
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        if(!mPath.substring(0, 1).equals("/") || mPath.substring(mPath.length()-1).equals("/")) {
                            throw new WASException(MSG_TYPE.ERROR, 29);
                        }        
                        String fullPath = sPath + mPath;
                        if(serviceContextMappings.containsKey(fullPath)) {
                            throw new WASException(MSG_TYPE.ERROR, 21, new Object[]{service, fullPath});
                        }
                        serviceContextMappings.put(fullPath, (ILeapService)ClassUtils.instantiate(service));
                    }
                }
            } else {
                logger.info(Context.getInfoMsg(1, service.getClass().getName()));
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
            ServiceMapper serviceDescriptor = service.getClass().getDeclaredAnnotation(ServiceMapper.class);
            if(serviceDescriptor != null) {
                String sPath = serviceDescriptor.path();
                if(!sPath.substring(0, 1).equals("/") || sPath.substring(sPath.length()-1).equals("/")) {
                    throw new WASException(MSG_TYPE.ERROR, 28);
                }
                Method[] methods = service.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        if(!mPath.substring(0, 1).equals("/") || mPath.substring(mPath.length()-1).equals("/")) {
                            throw new WASException(MSG_TYPE.ERROR, 29);
                        }        
                        String fullPath = sPath + mPath;
                        if(serviceContextMappings.containsKey(fullPath)) {
                            throw new WASException(MSG_TYPE.ERROR, 21, new Object[]{service, fullPath});
                        }
                        serviceContextMappings.put(fullPath, method);
                    }
                }
            } else {
                logger.info(Context.getInfoMsg(1, service.getClass().getName()));
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
    public static List<Method> getFilterMethods(IFilter filter, Class annotationClass) {
        return Arrays.asList(filter.getClass().getDeclaredMethods()).stream().filter(m -> m.getDeclaredAnnotation(annotationClass) != null).collect(Collectors.toList());
    }
}
