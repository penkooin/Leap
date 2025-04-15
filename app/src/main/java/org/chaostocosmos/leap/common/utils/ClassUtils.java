package org.chaostocosmos.leap.common.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.LeapURLClassLoader;
import org.chaostocosmos.leap.common.data.Filtering;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.filter.IFilter;
import org.chaostocosmos.leap.service.model.ServiceModel;

/**
 * ClassUtils object
 * 
 * @author 9ins
 */
public class ClassUtils {    

    /**
     * Leap URL class loader
     */
    private static LeapURLClassLoader classLoader = null;

    /**
     * Get class loader for leap
     * @return
     */
    public static LeapURLClassLoader getClassLoader() {
        if(classLoader == null) {
            classLoader = new LeapURLClassLoader();
        }
        return classLoader;
    }

    /**
     * Get class loader for Leap
     * @param urls
     * @return
     */
    public static LeapURLClassLoader getClassLoader(URL[] urls) {
        classLoader = getClassLoader();
        for(URL url : urls) {            
            classLoader.addPath(url);
        }
        return classLoader;
    }

    /**
     * Find All Leap service instance
     * @param classLoader
     * @param reloadConfig
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws NotSupportedException
     */
    @SuppressWarnings("unchecked")
    public static <T, R> List<Class<? extends ServiceModel<T, R>>> findAllLeapServices(URLClassLoader classLoader, boolean reloadConfig, Filtering filters) throws IOException, URISyntaxException, NotSupportedException {
        if(reloadConfig) {
            Context.get().refresh();
        }
        List<Class<? extends ServiceModel<T, R>>> services = findClasses(classLoader, ServiceModel.class, null)
                                                       .stream()
                                                       .filter(f ->! Modifier.isAbstract(f.getModifiers()) && ! Modifier.isInterface(f.getModifiers()))
                                                       .map(c -> (Class<? extends ServiceModel<T, R>>) c)
                                                       .collect(Collectors.toList());
        return services;
    }

    /**
     * Get all Leap filters
     * @param classLoader
     * @param reloadConfig
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findAllLeapFilters(URLClassLoader classLoader, boolean reloadConfig, Filtering filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, IFilter.class, null)
                .stream()
                .filter(f -> //f.isAssignableFrom(IFilter.class)
                        !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers())
                        )
                .map(f -> (Class<? extends IFilter>) f)
                .collect(Collectors.toList());
    }

    /**
     * Find pre filters
     * @return
     * @param classLoader
     * @param filters
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findPreFilters(URLClassLoader classLoader, Filtering filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, IFilter.class, filters)
                .stream()
                .filter(f -> !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                .map(f -> (Class<? extends IFilter>)f)
                .collect(Collectors.toList());
    }

    /**
     * Find post filters
     * @param classLoader the class loader
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findPostFilters(URLClassLoader classLoader, Filtering filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, IFilter.class, filters)
                .stream()
                .filter(f -> !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                .map(f -> (Class<? extends IFilter>)f)
                .collect(Collectors.toList());
    }

    /**
     * Find all filters
     * @param classLoader
     * @param iFilter
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static List<Class<? extends IFilter>> findFilters(URLClassLoader classLoader, Class<? extends IFilter> iFilter, Filtering filters) throws IOException, URISyntaxException {
        return findClasses(classLoader, iFilter, filters)
               .stream()
               .map(c -> (Class<? extends IFilter>)c)
               .collect(Collectors.toList());
    }

    /**
     * Find all classes
     * @param classLoader
     * @param clazz
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends Object>> findClasses(URLClassLoader classLoader, Class<?> clazz, Filtering filters) throws IOException, URISyntaxException {        
        List<Class<? extends Object>> classes = new ArrayList<>();
        for(URL url : classLoader.getURLs()) {
            List<String> classNames = findClassNames(url, filters);
            for(String cName : classNames) {
                Class<?> cls = getClass(classLoader, cName);
                if(cls != null && clazz.isAssignableFrom(cls) && !classes.stream().anyMatch(c -> c.getName().equals(cName))) {
                    classes.add(cls);
                }
            }
        }
        return classes;
    }

    /**
     * Get class names from URL
     * @param url
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<String> findClassNames(URL url, Filtering filters) throws IOException, URISyntaxException {
        Stream<String> stream = null;
        String protocol = url.getProtocol();
        switch (protocol) {
            case "file" -> stream = Files.walk(Paths.get(url.toURI().getPath()))
                               .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                               .map(p -> p.toString().substring(new File(url.getFile()).getAbsolutePath().length() + 1).replace(File.separator, "."));
            case "jar" -> {
                try (FileSystem filesystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>())) {
                    stream = Files.walk(filesystem.getPath(""))
                            .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                            .map(p -> p.toString().substring(new File(url.getFile()).getAbsolutePath().length() + 1).replace(File.separator, "."));
                }
            }
            default -> throw new IllegalArgumentException("Protocol not collect!!!");
        }
        if (stream == null) {
            throw new IllegalStateException("Stream is null. Unable to process class names.");
        }
        return stream.filter(fqn -> filters == null || filters.include(fqn))
                     .map(c -> c.substring(0, c.lastIndexOf(".")))
                     .collect(Collectors.toList());
    }

    /**
     * Get Class object
     * @param classLoader
     * @param className
     * @return
     */
    public static Class<?> getClass(ClassLoader classLoader, String className) { 
        Class<?> clazz = null;
        try {
            clazz = classLoader.loadClass(className);
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return clazz;
    }

    /**
     * Invoke method
     * @param instance
     * @param methodName
     * @param params
     * @return
     */
    public static Object invokeMethod(Object instance, String methodName, Object... params) {
        Class<?> clazz = instance.getClass();
        Class<?>[] paramTypes = Arrays.stream(params).map(Object::getClass).toArray(Class<?>[]::new);
        try {
            return clazz.getMethod(methodName, paramTypes).invoke(instance, params);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Instantiate with specified name
     * @param classLoader
     * @param qualifiedClassName
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiate(ClassLoader classLoader, String qualifiedClassName, Object ... params) throws NoSuchMethodException, 
                                                                                                                  SecurityException, 
                                                                                                                  IllegalArgumentException, 
                                                                                                                  InvocationTargetException, 
                                                                                                                  ClassNotFoundException, 
                                                                                                                  InstantiationException, 
                                                                                                                  IllegalAccessException {
        Class<?> clazz = classLoader.loadClass(qualifiedClassName);
        Constructor<T> constructor = (Constructor<T>) clazz.getConstructor(Arrays.asList(params).stream().map(o -> o.getClass()).toArray(Class<?>[]::new));
        return constructor.newInstance(params);
    }

    /**
     * Instantiate with specified Class
     * @param classLoader
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static <T> T instantiate(URLClassLoader classLoader, Class<?> clazz, Object... params) throws NoSuchMethodException, 
                                                                                                         SecurityException, 
                                                                                                         IllegalArgumentException, 
                                                                                                         InvocationTargetException, 
                                                                                                         ClassNotFoundException, 
                                                                                                         InstantiationException, 
                                                                                                         IllegalAccessException {
        return ClassUtils.<T> instantiate(classLoader, clazz.getName(), params);
    }

    /**
     * @param <T>
     * @param classLoader
     * @param qualifiedClassName
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static <T> T instantiateDefaultConstructor(ClassLoader classLoader, String qualifiedClassName) throws NoSuchMethodException, 
                                                                                                                 SecurityException, 
                                                                                                                 IllegalArgumentException, 
                                                                                                                 InvocationTargetException, 
                                                                                                                 ClassNotFoundException, 
                                                                                                                 InstantiationException, 
                                                                                                                 IllegalAccessException {
        return ClassUtils.<T> instantiate(classLoader, qualifiedClassName, new Object[0]);
    }

    /**
     * Get Hosts object mapping with Map
     * @param map
     * @param isDefaultHost     * 
     * @return
     * @throws IOException
     * @throws ImageProcessingException
     */
    public static Host<?> mappingToHost(Map<String, Object> map) throws IOException {
        return new Host<>(map);
    }

    /**
     * Get Map object from Hosts object
     * @param host
     * @return
     */
    public static Map<String, Object> mappingToMap(Host<Map<String, Object>> host) {
        Map<String, Object> map = new HashMap<>();
        map.put("hostname", host.getHost());
        map.put("protocol", host.getProtocol());
        map.put("charset", host.charset());
        map.put("host", host.getHost());
        map.put("port", host.getPort());
        map.put("users", host.getValue("users"));
        map.put("dynamic-classpath", host.getDynamicClassPaths().toString());
            Map<Object, Object> filterMap = new HashMap<>();
            filterMap.put("access-filters", host.getAllowedPathFiltering());
        map.put("resource", filterMap);
            Map<Object, Object> ipFilterMap = new HashMap<>();
            ipFilterMap.put("forbidden", host.getIpForbiddenFiltering());
        map.put("ip-filter", ipFilterMap);
        map.put("home", host.getDocroot().toString());
        map.put("welcome", host.getIndexFile().toPath().toString());
        map.put("logs", host.getLogPath().toString());
        map.put("log-level", host.getLogLevel());
        map.put("details", host.<Boolean> getValue("logs.details"));
        return map;
    }
}
