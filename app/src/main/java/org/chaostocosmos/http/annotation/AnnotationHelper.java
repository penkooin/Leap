package org.chaostocosmos.http.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.http.Context;
import org.chaostocosmos.http.REQUEST_TYPE;
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
}
