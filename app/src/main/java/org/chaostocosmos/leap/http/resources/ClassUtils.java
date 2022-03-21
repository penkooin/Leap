package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.filters.ILeapFilter;
import org.chaostocosmos.leap.http.services.ILeapService;
import org.chaostocosmos.leap.http.user.GRANT;
import org.chaostocosmos.leap.http.user.User;

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
     * Get class loader for Leap
     * @return
     * @throws MalformedURLException
     */
    public static LeapURLClassLoader getClassLoader() throws MalformedURLException {
        if(classLoader == null) {
            classLoader = new LeapURLClassLoader(HostsManager.get().getAllDynamicClasspathURLs());
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
     */
    public static List<Class<? extends ILeapService>> findAllLeapServices(URLClassLoader classLoader, boolean reloadConfig, List<String> filters) throws IOException, URISyntaxException {
        if(reloadConfig) {
            Context.get().loadConfig();
        }
        List<Class<? extends ILeapService>> services = findClasses(classLoader, ILeapService.class, classLoader.getResource(""), null)
                                                       .stream()
                                                       .filter(f ->!Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                                                       .map(c -> (Class<? extends ILeapService>)c)
                                                       .collect(Collectors.toList());
        for(URL url : classLoader.getURLs()) {
            services.addAll(findClasses(classLoader, ILeapService.class, url, filters)
                            .stream()
                            .filter(f ->!Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                            .map(c -> (Class<? extends ILeapService>)c)
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
    public static List<Class<? extends ILeapFilter>> findAllLeapFilters(URLClassLoader classLoader, boolean reloadConfig, List<String> filters) throws IOException, URISyntaxException {
        List<Class<? extends ILeapFilter>> filterClasses = findFilters(classLoader, ILeapFilter.class, classLoader.getResource(""), null)
                                                          .stream()
                                                          .filter(f -> //f.isAssignableFrom(IFilter.class)
                                                                      !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers())
                                                                 )
                                                          .map(f -> (Class<? extends ILeapFilter>)f)
                                                          .collect(Collectors.toList());
        for(URL url : classLoader.getURLs()) {
            filterClasses.addAll(findFilters(classLoader, ILeapFilter.class, url, filters)
                                 .stream()
                                 .filter(f -> //f.isAssignableFrom(IFilter.class)
                                            !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                                 .map(f -> (Class<? extends ILeapFilter>)f)
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
    public static List<Class<? extends ILeapFilter>> findPreFilters(URLClassLoader classLoader, URL url, List<String> filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, ILeapFilter.class, url, filters)
                    .stream()
                    .filter(f -> !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends ILeapFilter>)f)
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
    public static List<Class<? extends ILeapFilter>> findPostFilters(URLClassLoader classLoader, URL url, List<String> filters) throws IOException, URISyntaxException {
        return findFilters(classLoader, ILeapFilter.class, url, filters)
                    .stream()
                    .filter(f -> !Modifier.isAbstract(f.getModifiers()) && !Modifier.isInterface(f.getModifiers()))
                    .map(f -> (Class<? extends ILeapFilter>)f)
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
    public static List<Class<? extends ILeapFilter>> findFilters(URLClassLoader classLoader, Class<? extends ILeapFilter> iFilter, URL url, List<String> filters) throws IOException, URISyntaxException {
        return findClasses(classLoader, iFilter, url, filters)
               .stream()
               .map(c -> (Class<? extends ILeapFilter>)c)
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
    public static List<Class<? extends Object>> findDynamicClasses(URLClassLoader classLoader, Class<?> clazz, List<String> filters) throws IOException, URISyntaxException {
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
    public static List<Class<? extends Object>> findClasses(URLClassLoader classLoader, Class<?> clazz, URL url, List<String> filters) throws IOException, URISyntaxException {
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
    public static List<String> findClassNames(URL url, List<String> filters) throws IOException, URISyntaxException {
        List<String> classes = null;
        String protocol = url.getProtocol();
        if(protocol.equals("file")) {
            classes = Files.walk(Paths.get(url.toURI()))
                           .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                           .map(p -> p.toString().substring(new File(url.getFile()).getAbsolutePath().length()+1).replace(File.separator, "."))
                           .filter(pkg -> filters == null || filters.stream().anyMatch(f -> !f.trim().equals("") && pkg.matches(Arrays.asList(f.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*")))
                           .map(c -> c.substring(0, c.lastIndexOf(".")))
                           .collect(Collectors.toList());

        } else if(protocol.equals("jar")) {
            try(FileSystem filesystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>())) {
                classes = Files.walk(filesystem.getPath(""))
                            .filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".class"))
                            .map(p -> p.toString().replace(File.separator, "."))
                            .filter(pkg -> filters == null || filters.stream().anyMatch(f -> !f.trim().equals("") && pkg.matches(Arrays.asList(f.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*")))
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
     * @return
     * @throws IOException
     */
    public static Hosts mappingToHosts(Map<Object, Object> map) throws IOException {
        return new Hosts(
            map.get("default") != null ? (boolean)map.get("default") : false,
            (String)map.get("server-name"),
            PROTOCOL.getProtocol((String)map.get("protocol")),
            Charset.forName((String)map.get("charset")),
            (String)map.get("host"),
            (int)map.get("port"),
            (List<User>)((List<Map<?, ?>>)map.get("users")).stream().map(m -> new User(m.get("username").toString(), m.get("password").toString(), GRANT.valueOf(m.get("grant").toString()))).collect(Collectors.toList()),
            !map.get("dynamic-classpath").equals("") ? Paths.get((String)map.get("dynamic-classpath")) : null,
            (List<String>)map.get("dynamic-packages"),
            (List<String>)map.get("spring-jpa-packages"),
            ((List<?>)((Map<?, ?>)map.get("resource")).get("in-memory-filter")).stream().map(p -> p.toString()).collect(Collectors.toList()),
            (List<String>)((Map<?, ?>)map.get("resource.access-filters")),
            ((List<?>)map.get("error-filters")).stream().map(f -> ClassUtils.getClass(ClassLoader.getSystemClassLoader(), f.toString().trim())).collect(Collectors.toList()),
            Paths.get((String)map.get("doc-root")),
            Paths.get((String)map.get("doc-root")).resolve("webapp"),
            Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF"),
            Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF").resolve("static"),
            Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("services"),
            Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF").resolve("static").resolve("index.html").toFile(),
            Paths.get((String)map.get("logs")),
            UtilBox.getLogLevels((String)map.get("log-level"), ","),
            map,
            StaticResourceManager.get((String)map.get("host"))
            );
    }

    /**
     * Get Map object from Hosts object
     * @param host
     * @return
     */
    public static Map<Object, Object> mappingToMap(Hosts host) {
        Map<Object, Object> map = new HashMap<>();
        map.put("default", host.isDefaultHost());
        map.put("server-name", host.getServerName());
        map.put("protocol", host.getProtocol().name());
        map.put("charset", host.charset().name());
        map.put("host", host.getHost());
        map.put("port", host.getPort());
        map.put("users", host.getUsers().stream().map(user -> user.getUserMap()).collect(Collectors.toList()));
        map.put("dynamic-classpath", host.getDynamicClasspaths().toString());
        map.put("dynamic-packages", host.getDynamicPackages());
        map.put("spring-jpa-packages", host.getSpringJPAPackages());
        Map<Object, Object> filterMap = new HashMap<>();
        filterMap.put("in-memory-filters", host.getInMemoryFilters()); 
        filterMap.put("access-filters", host.getAccessFilters());
        map.put("resource", filterMap);
        map.put("error-filters", host.getErrorFilters());
        map.put("doc-root", host.getDocroot().toString());
        map.put("welcome", host.getWelcomeFile().toPath().toString());
        map.put("logs", host.getLogPath().toString());
        map.put("log-level", host.getLogLevel().stream().map(l -> l.toString()).collect(Collectors.joining(",")));
        return map;
    }
}
