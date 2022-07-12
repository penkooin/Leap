package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.services.filters.IFilter;
import org.chaostocosmos.leap.http.services.model.ServiceModel;

/**
 * ClassUtils object
 * 
 * @author 9ins
 */
public class ClassUtils {

    /**
     * Leap class loader
     */
    private static LeapURLClassLoader classLoader = null;

    /**
     * Dynamic 
     */
    private static Filtering filters;

    /**
     * Get class loader for Leap
     * @return
     * @throws MalformedURLException
     */
    public static LeapURLClassLoader getClassLoader() throws MalformedURLException {
        if(classLoader == null) {
            classLoader = new LeapURLClassLoader(Context.getHosts().getAllDynamicClasspathURLs());
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
     * @throws Exception
     */
    public static List<Class<? extends ServiceModel>> findAllLeapServices(URLClassLoader classLoader, boolean reloadConfig, Filtering filters) throws IOException, URISyntaxException, NotSupportedException {
        if(reloadConfig) {
            Context.refresh();
        }
        List<Class<? extends ServiceModel>> services = findClasses(classLoader, ServiceModel.class, classLoader.getResource(""), null)
                                                       .stream()
                                                       .filter(f ->!Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                                                       .map(c -> (Class<? extends ServiceModel>)c)
                                                       .collect(Collectors.toList());
        for(URL url : classLoader.getURLs()) {
            services.addAll(findClasses(classLoader, ServiceModel.class, url, filters)
                            .stream()
                            .filter(f ->!Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                            .map(c -> (Class<? extends ServiceModel>)c)
                            .collect(Collectors.toList()));
        }
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
        List<Class<? extends IFilter>> filterClasses = findFilters(classLoader, IFilter.class, classLoader.getResource(""), null)
                                                          .stream()
                                                          .filter(f -> //f.isAssignableFrom(IFilter.class)
                                                                      !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers())
                                                                 )
                                                          .map(f -> (Class<? extends IFilter>)f)
                                                          .collect(Collectors.toList());
        for(URL url : classLoader.getURLs()) {
            filterClasses.addAll(findFilters(classLoader, IFilter.class, url, filters)
                                 .stream()
                                 .filter(f -> //f.isAssignableFrom(IFilter.class)
                                            !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                                 .map(f -> (Class<? extends IFilter>)f)
                                 .collect(Collectors.toList()));

        }
        return filterClasses;
    }

    /**
     * Find pre filters
     * @return
     * @param classLoader
     * @param url
     * @param filters
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findPreFilters(URLClassLoader classLoader, URL url, Filtering filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, IFilter.class, url, filters)
                    .stream()
                    .filter(f -> !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends IFilter>)f)
                    .collect(Collectors.toList());
    }

    /**
     * Find post filters
     * @param classLoader the class loader
     * @param url
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findPostFilters(URLClassLoader classLoader, URL url, Filtering filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, IFilter.class, url, filters)
                    .stream()
                    .filter(f -> !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends IFilter>)f)
                    .collect(Collectors.toList());
    }

    /**
     * Find all filters
     * @param classLoader
     * @param iFilter
     * @param url
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findFilters(URLClassLoader classLoader, Class<? extends IFilter> iFilter, URL url, Filtering filters) throws IOException, URISyntaxException {
        return findClasses(classLoader, iFilter, url, filters)
               .stream()
               .map(c -> (Class<? extends IFilter>)c)
               .collect(Collectors.toList());
    }

    /**
     * Find dynamic classes
     * @param classLoader
     * @param clazz
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends Object>> findDynamicClasses(URLClassLoader classLoader, Class<?> clazz, Filtering filters) throws IOException, URISyntaxException {
        List<Class<? extends Object>> classes = new ArrayList<>();
        for(URL url : classLoader.getURLs()) {
            classes.addAll(findClasses(classLoader, clazz, url, filters));
        }
        return classes;
    }

    /**
     * Find all classes
     * @param classLoader
     * @param clazz
     * @param url
     * @param filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends Object>> findClasses(URLClassLoader classLoader, Class<?> clazz, URL url, Filtering filters) throws IOException, URISyntaxException {
        List<String> classes = findClassNames(url, filters);
        return classes.stream().map(c -> getClass(classLoader, c)).filter(c -> c != null && clazz.isAssignableFrom(c)).collect(Collectors.toList());
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
        List<String> classes = null;
        String protocol = url.getProtocol();
        if(protocol.equals("file")) {
            classes = Files.walk(Paths.get(url.toURI()))
                           .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                           .map(p -> p.toString().substring(new File(url.getFile()).getAbsolutePath().length()+1).replace(File.separator, "."))
                           .filter(pkg -> filters == null || filters.include(pkg))
                           .map(c -> c.substring(0, c.lastIndexOf(".")))
                           .collect(Collectors.toList());

        } else if(protocol.equals("jar")) {
            try(FileSystem filesystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>())) {
                classes = Files.walk(filesystem.getPath(""))
                            .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                            .map(p -> p.toString().replace(File.separator, "."))
                            .filter(pkg -> filters == null || filters.include(pkg))
                            .map(c -> c.substring(0, c.lastIndexOf(".")))
                            .collect(Collectors.toList());
            }
        } else {
            throw new IllegalArgumentException("Protocol not collect!!!");
        }
        return classes;
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
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * Instantiate with specified name
     * @param classLoader
     * @param qualifiedClassName
     * @return
     */
    public static Object instantiate(ClassLoader classLoader, String qualifiedClassName) {
        try {
            Class<?> clazz = classLoader.loadClass(qualifiedClassName);
            return clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Instantiate with specified Class
     * @param classLoader
     * @param clazz
     * @return
     */
    public static Object instantiate(URLClassLoader classLoader, Class<?> clazz) {
        return instantiate(classLoader, clazz.getName());
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
        return new Host<Map<String, Object>>(map);
    }

    /**
     * Get Map object from Hosts object
     * @param host
     * @return
     */
    public static Map<String, Object> mappingToMap(Host<Map<String, Object>> host) {
        Map<String, Object> map = new HashMap<>();
        map.put("default", host.isDefaultHost());
        map.put("hostname", host.getHostId());
        map.put("protocol", host.<String> getProtocol());
        map.put("charset", host.<String> charset());
        map.put("host", host.getHost());
        map.put("port", host.getPort());
        map.put("users", host.getValue("users"));
        map.put("dynamic-classpath", host.getDynamicClasspaths().toString());
        map.put("dynamic-packages", host.getDynamicPackages());
            Map<Object, Object> filterMap = new HashMap<>();
            filterMap.put("in-memory-filters", host.getInMemoryFiltering()); 
            filterMap.put("access-filters", host.getAccessFiltering());
            filterMap.put("forbidden-filters", host.getForbiddenFiltering());
        map.put("resource", filterMap);
            Map<Object, Object> ipFilterMap = new HashMap<>();
            ipFilterMap.put("allowed", host.getIpAllowedFiltering());
            ipFilterMap.put("forbidden", host.getIpForbiddenFiltering());
        map.put("ip-filter", ipFilterMap);
        map.put("error-filters", host.getErrorFilters());
        map.put("doc-root", host.getDocroot().toString());
        map.put("welcome", host.getWelcomeFile().toPath().toString());
        map.put("logs", host.getLogPath().toString());
        map.put("log-level", host.getLogLevel().stream().map(l -> l.toString()).collect(Collectors.joining(",")));
        return map;
    }
}
