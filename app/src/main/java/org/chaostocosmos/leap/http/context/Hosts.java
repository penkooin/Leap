package org.chaostocosmos.leap.http.context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.resources.ClassUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Virtual host manager object
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class Hosts <T> extends Metadata <T> {
    /**
     * Host Map
     */
    private Map<String, Host<T>> hostMap = new HashMap<>();

    /**
     * Constructor
     * @param hostsMap
     */
    public Hosts(T hostsMap) {
        super(hostsMap);
        getAllHost();
    }  
    
    /**
     * Get host object by name
     * @param hostId
     * @return
     */
    public Host<T> getHost(String hostId) {
        if(!hostMap.containsKey(hostId)) {
            hostMap.put(hostId, new Host<T>(super.<List<T>>getValue("hosts").stream().filter(m -> ((Map<?, ?>)m).get("id").equals(hostId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Host not found!!!"))));
        }
        return hostMap.get(hostId);
    }

    /**
     * Get default Hosts
     * @return
     */
    public Host<T> getDefaultHost() {
        return new Host<T>(super.<List<T>>getValue("hosts").stream().filter(m -> ((Map<?, ?>)m).get("default").equals(true)).findFirst().orElseThrow(() -> new IllegalArgumentException("Default host not found!!!")));
    }

    /**
     * Get all host list
     * @return
     */
    public List<Host<T>> getAllHost() {
        List<String> hostIds = super.<List<Map<String, Object>>> getValue("hosts").stream().map(m -> (String) m.get("id")).collect(Collectors.toList());
        return hostIds.stream().map(s -> getHost(s)).collect(Collectors.toList());
    }

    /**
     * Get default host
     * @return
     */
    public <V> V getDefaultHostName() {
        return getDefaultHost().getHost();
    }

    /**
     * Get default port
     * @return
     */
    public <V> V getDefaultPort() {
        return getDefaultHost().getPort();
    }

    /**
     * Get all of host id
     * @return
     */
    public <V> List<V> getHostIds() {
        return getAllHost().stream().map(h -> h.<V>getHostId()).collect(Collectors.toList());
    }

    /**
     * Get dynamic classpath list
     * @param hostId
     * @return
     */
    public <V> V getDynamicClaspaths(String hostId) {        
        return getHost(hostId).getDynamicClasspaths();
    }

    /**
     * Get all of dynamic classpath list
     * @return
     */
    public <V> List<V> getAllDynamicClasspaths() {
        return getAllHost().stream().map(h -> h.<V>getDynamicClasspaths()).collect(Collectors.toList());
    }

    /**
     * Get all configured host names. It could be having same value.
     * @return
     */
    public <V> List<V> getAllHostname() {
        return getAllHost().stream().map(v -> v.<V>getHost()).collect(Collectors.toList()); 
    }    

    /**
     * Get host name matching with host 
     * @param hostId
     * @return
     */
    public <V> V getHostId(V host) {
        return getAllHost().stream().filter(h -> h.<V> getHost().equals(host)).findFirst().orElse(null).getHostId();
    }

    /**
     * Get host's port
     * @param hostId
     * @return
     */
    public <V> V getPort(String hostId) {
        return getHost(hostId).getPort();
    }

////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Get using ports
     * @return
     */ 
    public int[] getUsingPorts() {
        return getAllHost().stream().mapToInt(h -> h.<Integer> getPort()).toArray();
    }

    /**
     * Get docroot path by host name
     * @param hostId
     * @return
     */
    public Path getDocroot(String hostId) {
        return getHost(hostId).getDocroot();
    }

    /**
     * Get template path by host getNameCount
     * @param hostId
     * @return
     */
    public Path getTemplates(String hostId) {
        return getHost(hostId).getTemplates();
    }

    /**
     * Whether virtual host
     * @param hostId
     * @return
     */
    public boolean isVirtualHost(String hostId) {
        return !getHost(hostId).<Boolean> isDefaultHost();
    }

    /**
     * Get virtual host list
     * @return
     */
    public List<Host<T>> getVirtualHosts() {
        return getAllHost().stream().filter(h -> !h.<Boolean> isDefaultHost()).collect(Collectors.toList());
    }

    /**
     * Get web protocol of Host or vHost
     * @param hostId
     * @return
     */
    public PROTOCOL getProtocol(String hostId) {
        return getHost(hostId).getProtocol();
    }

    /**
     * Get welcome file
     * @param hostId
     * @return
     */
    public File getWelcomeFile(String hostId) {
        return getHost(hostId).getWelcomeFile();
    }

    /**
     * Get logger by host
     * @param hostId
     * @return
     */
    public Logger getLogger(String hostId) {
        return LoggerFactory.getLogger(hostId);
    }

    /**
     * Get log path
     * @param hostId
     * @return
     */
    public Path getLogPath(String hostId) {
        return getHost(hostId).getLogPath();
    }

    /**
     * Get log level
     * @param hostId
     * @return
     */
    public List<Level> getLogLevel(String hostId) {
        return getHost(hostId).getLogLevel();
    }

    /**
     * Get charset by the host
     * @param hostId
     * @return
     */
    public Charset charset(String hostId) {
        return Charset.forName(getHost(hostId).charset());
    }

    /**
     * Get in-memory filters
     * @param hostId
     * @return
     */
    public Filtering getInMemoryFiltering(String hostId) {
        return getHost(hostId).getInMemoryFiltering();
    }

    /**
     * Get allowed resource pattern
     * @param hostId
     * @return
     */
    public Filtering getAccessFiltering(String hostId) {
        return getHost(hostId).getAccessFiltering();
    }

    /**
     * Filtering dynamic packages
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringDynamicPackages(String hostId, String resourceName) {
        return getHost(hostId).getDynamicPackageFiltering().include(resourceName);
    }

    /**
     * Filtering in-memory resources with specified resourceName
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringInMemory(String hostId, String resourceName) {
        return getHost(hostId).getInMemoryFiltering().include(resourceName);
    }

    /**
     * Filtering in-disk resources with specified resourceName
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringInAccess(String hostId, String resourceName) {
        return getHost(hostId).getAccessFiltering().include(resourceName);
    }

    /**
     * Get host list matching with a port 
     * @param port
     * @return
     */
    public List<String> getHostsByPort(int port) {
        return getAllHost().stream().filter(h -> h.<Integer> getPort() == port).map(h -> h.<String> getHost()).collect(Collectors.toList());
    }

    /**
     * Load error filters from config
     * @param hostId
     * @return
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     */
    public List<Class<?>> loadErrorFilters(String hostId) throws ClassNotFoundException, MalformedURLException {
        List<?> filters = getHost(hostId).<List<?>>getErrorFilters();
        List<Class<?>> errorFilters = new ArrayList<>();
        for(Object obj : filters) {
            errorFilters.add(ClassUtils.getClassLoader().loadClass((String)obj));
        }
        return errorFilters;
    }

    /**
     * Get host user object
     * @param hostId
     * @return
     */
    public List<User> getUsers(String hostId) {
        return getHost(hostId).getUsers();
    }

    /**
     * Whether the host is existing in this server
     * @param host
     * @return
     */
    public boolean isExistHostname(String hostname) {
        return getAllHostname().stream().anyMatch(h -> h.equals(hostname));
    }

    /**
     * Whether host ID exists in this server
     */
    public boolean isExistHostId(String hostId) {
        return getHostIds().stream().anyMatch(h -> h.equals(hostId));
    }

    /**
     * Get all dynamic classpath URL array
     * @return
     * @throws MalformedURLException
     */
    public URL[] getAllDynamicClasspathURLs() throws MalformedURLException {
        List<Path> paths = getAllDynamicClasspaths();
        List<URL> urls = new ArrayList<>();
        for(Path path : paths) {
            if(path != null) {
                urls.add(path.toFile().toURI().toURL());
            }            
        }
        return urls.stream().toArray(URL[]::new);
    }
}
