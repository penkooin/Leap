package org.chaostocosmos.leap.http.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.REQUEST_TYPE;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.AnnotationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet loader object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class ServletManager {
    /**
     * Logger
     */
    public static final Logger logger = LoggerFactory.getLogger(ServletManager.class);

    /**
     * Servlet class Map
     */
    private Map<String, ILeapServlet> servletContextMappings = new HashMap<>();

    /**
     * Class loader
     */
    private ClassLoader classLoader;

    /**
     * Constructor with 
     * @param servletBeans
     * @throws WASException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public ServletManager(List<ServletBean> servletBeans) throws InstantiationException, 
                                                                 IllegalAccessException, 
                                                                 IllegalArgumentException, 
                                                                 InvocationTargetException, 
                                                                 NoSuchMethodException, 
                                                                 SecurityException, 
                                                                 ClassNotFoundException, 
                                                                 WASException {
        this.classLoader = ClassLoader.getSystemClassLoader();
        this.servletContextMappings = AnnotationHelper.getServletContextMappings(servletBeans.stream().map(e -> e.getServletClass()).collect(Collectors.toList()));
    }

    /**
     * Validate request method
     * @param type
     * @param path
     * @return
     */
    public boolean vaildateRequestMethod(REQUEST_TYPE type, String path) {
        return AnnotationHelper.vaildateRequestMethod(type, path, this.servletContextMappings.values().stream().collect(Collectors.toList()));
    }

    /**
     * Get match Method class in servlet Map
     * @param path
     * @return
     */
    public Method getMatchMethod(String path) {
        return AnnotationHelper.methodMatches(path, this.servletContextMappings.values().stream().collect(Collectors.toList()));
    }

    /**
     * Get servlet object with parameted path
     * @param path
     * @return
     */
    public Object getMatchServlet(String path) {
        return AnnotationHelper.servletMatches(path, this.servletContextMappings.values().stream().collect(Collectors.toList()));
    }

    /**
     * Get servlet object matching with specfied context path
     * @param path
     * @return
     */
    public ILeapServlet getMappingServlet(String path) {
        return this.servletContextMappings.get(path);
    }

    /**
     * Instantiate SimpleServlet 
     * @param qualifiedClassName
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object instantiate(String qualifiedClassName) throws NoSuchMethodException, 
                                                                SecurityException, 
                                                                ClassNotFoundException, 
                                                                InstantiationException, 
                                                                IllegalAccessException, 
                                                                IllegalArgumentException, 
                                                                InvocationTargetException {
        Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(qualifiedClassName);
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * Get SimpleServlet instance
     * @param qualifiedClassName
     * @return
     * @throws Exception
     */
    public ILeapServlet getServletInstance(String qualifiedClassName) throws Exception {
        return this.servletContextMappings.get(qualifiedClassName).newInstance();
    }   
}
