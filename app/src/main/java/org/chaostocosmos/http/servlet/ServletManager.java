package org.chaostocosmos.http.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.http.Context;
import org.chaostocosmos.http.REQUEST_TYPE;
import org.chaostocosmos.http.annotation.AnnotationHelper;
import org.chaostocosmos.http.filter.IHttpFilter;
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
    private Map<String, SimpleServlet> servletClasMap = new HashMap<>();
    /**
     * Class loader
     */
    private ClassLoader classLoader;
    /**
     * Constructor with 
     * @param servletBeans
     */
    public ServletManager(List<ServletBean> servletBeans) {
        this.classLoader = ClassLoader.getSystemClassLoader();
        for(ServletBean sBean : servletBeans) {
            try {
                String servletClass = sBean.getServletClass();
                SimpleServlet servlet = (SimpleServlet)instantiate(sBean.getServletClass());
                List<String> filters = sBean.getServletFilterClassNames();
                List<IHttpFilter> filterList = new ArrayList<>();
                for(String f : filters) {
                    IHttpFilter filter = (IHttpFilter)instantiate(f);
                    filterList.add(filter);
                }
                servlet.applyFilters(filterList);
                this.servletClasMap.put(servletClass, servlet);    
            } catch (ClassNotFoundException e) {
                logger.error(Context.getInstance().getErrorMsg("error011", sBean.getServletClass()), e);
            } catch (NoSuchMethodException e) {
                logger.error(Context.getInstance().getErrorMsg("error012", sBean.getServletClass()), e);
            } catch (SecurityException e) {
                logger.error(Context.getInstance().getErrorMsg("error013"), e);
            } catch (Exception e) {
                logger.error(Context.getInstance().getErrorMsg("error015"), e);
            }
        }
    }
    /**
     * Validate request method
     * @param type
     * @param path
     * @return
     */
    public boolean vaildateRequestMethod(REQUEST_TYPE type, String path) {
        return AnnotationHelper.vaildateRequestMethod(type, path, this.servletClasMap.values().stream().collect(Collectors.toList()));
    }
    /**
     * Get match Method class in servlet Map
     * @param path
     * @return
     */
    public Method getMatchMethod(String path) {
        return AnnotationHelper.methodMatches(path, this.servletClasMap.values().stream().collect(Collectors.toList()));
    }
    /**
     * Get servlet object with parameted path
     * @param path
     * @return
     */
    public Object getMatchServlet(String path) {
        return AnnotationHelper.servletMatches(path, this.servletClasMap.values().stream().collect(Collectors.toList()));
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
    public Object instantiate(String qualifiedClassName) throws NoSuchMethodException, 
                                                                SecurityException, 
                                                                ClassNotFoundException, 
                                                                InstantiationException, 
                                                                IllegalAccessException, 
                                                                IllegalArgumentException, 
                                                                InvocationTargetException {
        Class<?> clazz = this.classLoader.loadClass(qualifiedClassName);
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
    public SimpleServlet getServletInstance(String qualifiedClassName) throws Exception {
        return this.servletClasMap.get(qualifiedClassName).newInstance();
    }
   
}
