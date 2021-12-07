package org.chaostocosmos.leap.http.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.MSG_TYPE;
import org.chaostocosmos.leap.http.REQUEST_TYPE;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.servlet.ILeapServlet;
import org.chaostocosmos.leap.http.servlet.ServletManager;
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
    public static final Logger logger = LoggerFactory.getLogger(AnnotationHelper.class);

    /**
     * Servlet context mapping Map
     */
    //public static Map<String, ILeapServlet> servletContextMappings = null;

    /**
     * context
     */
    private static Context context = Context.getInstance();

    /**
     * Whether matching with path in servlets
     * @param path
     * @param servlets
     * @return
     */
    public static boolean pathMatches(String path, List<Object> servlets) {
        if(methodMatches(path, servlets) == null) {
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
        System.out.println(method.getName());
        if(method != null) {
            MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
            if(type != methodDescriptor.requestMethod()) {
                return false;
            }
        }
        return true;        
    }

    /**
     * Match Method class matches with path in servlet
     * @param path
     * @param servlets
     * @return
     */
    public static Method methodMatches(String path, List<Object> servlets) {
        for(Object servlet : servlets) {
            ServletMapper servletDescriptor = servlet.getClass().getDeclaredAnnotation(ServletMapper.class);
            if(servletDescriptor != null) {
                String sPath = servletDescriptor.path();
                Method[] methods = servlet.getClass().getDeclaredMethods();
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
                logger.info(context.getInfoMsg("info001", servlet.getClass().getName()));
            }            
        }
        return null;
    }

    /**
     * Matches servlet object 
     * @param path
     * @param servlets
     * @return
     */
    public static Object servletMatches(String path, List<Object> servlets) {
        for(Object servlet : servlets) {
            ServletMapper servletDescriptor = servlet.getClass().getDeclaredAnnotation(ServletMapper.class);
            if(servletDescriptor != null) {
                String sPath = servletDescriptor.path();
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
                logger.info(context.getInfoMsg("info001", servlet.getClass().getName()));
            }            
        }
        return null;
    }
    /**
     * Get servlet conext mappings Map
     * @param classes
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws WASException
     * @throws ClassNotFoundException
     */
    public static Map<String, ILeapServlet> getServletContextMappings(List<String> classes) 
                                                                                    throws InstantiationException, 
                                                                                           IllegalAccessException, 
                                                                                           IllegalArgumentException, 
                                                                                           InvocationTargetException, 
                                                                                           NoSuchMethodException, 
                                                                                           SecurityException, 
                                                                                           WASException, 
                                                                                           ClassNotFoundException {
        Map<String, ILeapServlet> servletContextMappings = new HashMap<>();
        for(String servlet : classes) {
            ServletMapper servletDescriptor = servlet.getClass().getDeclaredAnnotation(ServletMapper.class);
            if(servletDescriptor != null) {
                String sPath = servletDescriptor.path();
                Method[] methods = servlet.getClass().getDeclaredMethods();
                for(Method method : methods) {
                    MethodMappper methodDescriptor = method.getDeclaredAnnotation(MethodMappper.class);
                    if(methodDescriptor != null) {
                        String mPath = methodDescriptor.path();
                        String fullPath = sPath + mPath;
                        if(servletContextMappings.containsKey(fullPath)) {
                            throw new WASException(MSG_TYPE.ERROR, "error021", new Object[]{servlet, fullPath});
                        }
                        servletContextMappings.put(fullPath, (ILeapServlet)ServletManager.instantiate(servlet));
                    }
                }
            } else {
                logger.info(context.getInfoMsg("info001", servlet.getClass().getName()));
            }                 
        }
        return servletContextMappings;
    }
}
