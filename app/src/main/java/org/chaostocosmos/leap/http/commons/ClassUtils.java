package org.chaostocosmos.leap.http.commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.filters.IFilter;
import org.chaostocosmos.leap.http.services.ILeapService;

import ch.qos.logback.classic.Logger;

/**
 * ClassUtils object
 * 
 * @author 9ins
 */
public class ClassUtils {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(Context.getDefaultHost());

    /**
     * Dynamic URL class loader
     */
    private static DynamicURLClassLoader dynamicURLClassLoader = new DynamicURLClassLoader(Context.getAllDynamicClasspaths());

    /**
     * Get DynamicURLClassLoader
     * @return
     */
    public static DynamicURLClassLoader getClassLoader() {
        return dynamicURLClassLoader;
    }

    /**
     * Find All Leap service instance
     * @param reloadConfig
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends ILeapService>> findAllLeapServices(boolean reloadConfig) throws WASException, IOException, URISyntaxException {
        if(reloadConfig) {
            Context.getInstance().loadConfig();
        }
        List<Class<? extends ILeapService>> services = findClasses(ILeapService.class, dynamicURLClassLoader.getResource(""))
                                                            .stream()
                                                            .filter(f ->!Modifier.isAbstract(f.getModifiers())
                                                                    && !Modifier.isInterface(f.getModifiers()))
                                                            .map(c -> (Class<? extends ILeapService>)c)
                                                            .collect(Collectors.toList());
        for(URL url : dynamicURLClassLoader.getURLs()) {
            System.out.println(url);
            services.addAll(findClasses(ILeapService.class, url)
                                .stream()
                                .filter(f ->!Modifier.isAbstract(f.getModifiers())
                                        && !Modifier.isInterface(f.getModifiers()))
                                .map(c -> (Class<? extends ILeapService>)c)
                                .collect(Collectors.toList()));
        }
        return services;
    }

    /**
     * Get all Leap filters
     * @param reloadConfig
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends IFilter>> findAllLeapFilters(boolean reloadConfig) throws IOException, URISyntaxException {
        List<Class<? extends IFilter>> filters = findFilters(IFilter.class, dynamicURLClassLoader.getResource(""))
                                                    .stream()
                                                    .filter(f -> //f.isAssignableFrom(IFilter.class)
                                                                !Modifier.isAbstract(f.getModifiers())
                                                                && !Modifier.isInterface(f.getModifiers())
                                                                )
                                                    .map(f -> (Class<? extends IFilter>)f)
                                                    .collect(Collectors.toList());
        for(URL url : dynamicURLClassLoader.getURLs()) {
            filters.addAll(findFilters(IFilter.class, url)
                            .stream()
                            .filter(f -> //f.isAssignableFrom(IFilter.class)
                            !Modifier.isAbstract(f.getModifiers())
                            && !Modifier.isInterface(f.getModifiers()))
                            .map(f -> (Class<? extends IFilter>)f)
                            .collect(Collectors.toList()));

        }
        System.out.println(filters.toString());
        return filters;
    }

    /**
     * Find pre filters
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends IFilter>> findPreFilters(URL url) throws IOException, URISyntaxException {
        return findFilters(IFilter.class, url)
                    .stream()
                    .filter(f -> 
                            !Modifier.isAbstract(f.getModifiers())
                            && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends IFilter>)f)
                    .collect(Collectors.toList());
    }

    /**
     * Find post filters
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends IFilter>> findPostFilters(URL url) throws IOException, URISyntaxException {
        return findFilters(IFilter.class, url)
                    .stream()
                    .filter(f -> 
                                !Modifier.isAbstract(f.getModifiers())
                                && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends IFilter>)f)
                    .collect(Collectors.toList());
    }

    /**
     * Find all filters
     * @param iFilter
     * @param url
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findFilters(Class<? extends IFilter> iFilter, URL url) throws IOException, URISyntaxException {
        return findClasses(iFilter, url)
                .stream()
                .map(c -> (Class<? extends IFilter>)(c))
                .collect(Collectors.toList());
    }

    /**
     * Find dynamic classes
     * @param clazz
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends Object>> findDynamicClasses(Class<?> clazz) throws IOException, URISyntaxException {
        List<Class<? extends Object>> classes = new ArrayList<>();
        for(URL url : dynamicURLClassLoader.getURLs()) {
            classes.addAll(findClasses(clazz, url));
        }
        return classes;
    }

    /**
     * Find all classes
     * @param url
     * @param resourcePath
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     */
    public static List<Class<? extends Object>> findClasses(Class<?> clazz, URL url) throws IOException, URISyntaxException {        
        List<String> classes = findClassNames(url);
        return classes.stream().map(c -> getClass(c)).filter(c -> c != null && clazz.isAssignableFrom(c)).collect(Collectors.toList());
    }

    /**
     * Get class names from URL
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<String> findClassNames(URL url) throws IOException, URISyntaxException {
        List<String> classes = null;
        String protocol = url.getProtocol();
        if(protocol.equals("file")) {
            classes = Files.walk(Paths.get(url.toURI()))
                           .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                           .map(p -> p.toString().substring(new File(url.getFile()).getAbsolutePath().length()+1).replace(File.separator, "."))
                           .map(c -> c.substring(0, c.lastIndexOf(".")))
                           .collect(Collectors.toList());

        } else if(protocol.equals("jar")) {
            try(FileSystem filesystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>())) {
                classes = Files.walk(filesystem.getPath(""))
                            .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                            .map(p -> p.toString().replace(File.separator, "."))
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
     * @param className
     * @param packageName
     * @return
     */
    private static Class<? extends Object> getClass(String className) { 
        try {            
            return dynamicURLClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Instantiate with specified name
     * @param qualifiedClassName
     * @return
     */
    public static Object instantiate(String qualifiedClassName) {
        try {
            Class<?> clazz =  dynamicURLClassLoader.loadClass(qualifiedClassName);
            return clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Instantiate with specified Class
     * @param clazz
     * @return
     */
    public static Object instantiate(Class<?> clazz) {
        return instantiate(clazz.getName());
    }
}
