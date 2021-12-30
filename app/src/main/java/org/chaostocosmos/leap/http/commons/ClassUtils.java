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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.filter.IFilter;
import org.chaostocosmos.leap.http.service.ILeapService;

import ch.qos.logback.classic.Logger;

/**
 * ClassUtils object
 * @author 9ins
 */
public class ClassUtils {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(Context.getDefaultHost());

    /**
     * Class loader
     */
    private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    /**
     * Find All Leap service instance
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends ILeapService>> findAllLeapServices() throws IOException, URISyntaxException {
        return findAllClasses(ILeapService.class)
                .stream()
                .filter(f ->!Modifier.isAbstract(f.getModifiers())
                            && !Modifier.isInterface(f.getModifiers()))
                .map(c -> (Class<? extends ILeapService>)c)
                .collect(Collectors.toList());
    }

    /**
     * Find pre filters
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Class<? extends IFilter>> findPreFilters() throws IOException, URISyntaxException {
        return findAllFilters(IFilter.class)
                    .stream()
                    .filter(f -> f.isAssignableFrom(IFilter.class)
                                && !Modifier.isAbstract(f.getModifiers())
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
    public static List<Class<? extends IFilter>> findPostFilters() throws IOException, URISyntaxException {
        return findAllFilters(IFilter.class)
                    .stream()
                    .filter(f -> f.isAssignableFrom(IFilter.class)
                                && !Modifier.isAbstract(f.getModifiers())
                                && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends IFilter>)f)
                    .collect(Collectors.toList());
    }

    /**
     * Find all filters
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<Class<? extends IFilter>> findAllFilters(Class<? extends IFilter> iFilter) throws IOException, URISyntaxException {
        return findAllClasses(iFilter)
                .stream()
                .map(c -> (Class<? extends IFilter>)(c))
                .collect(Collectors.toList());
    }

    /**
     * Find all classes
     * @param clazz
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Set<Class<? extends Object>> findAllClasses(Class<?> clazz) throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("");
        String protocol = url.getProtocol();
        List<String> classes;
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
        return classes.stream()
                    .map(c -> getClass(c))
                    .filter(c -> 
                            clazz.isAssignableFrom(c))
                    .collect(Collectors.toSet());
    }
 
    /**
     * Get Class object
     * @param className
     * @param packageName
     * @return
     */
    private static Class<? extends Object> getClass(String className) { 
        try {            
            return Class.forName(className);
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
            return instantiate(classLoader.loadClass(qualifiedClassName));
        } catch (ClassNotFoundException e) {
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
        try {            
            return clazz.newInstance();
        } catch (
                 InstantiationException | 
                 IllegalAccessException | 
                 IllegalArgumentException
                  e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        findAllClasses(null).stream().forEach(System.out::println);
    }
}
