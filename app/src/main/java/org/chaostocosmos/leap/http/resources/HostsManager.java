package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.user.User;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Virtual host manager object
 * @author 9ins
 * @since 2021.09.18
 */
public class HostsManager {
    /**
     * Hosts Map
     */
    private Map<String, Hosts> hostsMap = null;

    /**
     * HostsManager
     */
    private static HostsManager hostsManager = null;

    /**
     * Constructor
     */
    private HostsManager() {        
        this.hostsMap = Context.getHostsMap();
        System.out.println("HostsManager Initialized... "+new Date().toString());
    }

    /**
     * Get VirtualHostManager instance
     * @return
     */
    public static HostsManager get() {
        if(hostsManager == null) {
            hostsManager = new HostsManager();
        }
        return hostsManager;
    }

    /**
     * Get host object by name
     * @param host
     * @return
     */
    public Hosts getHosts(String host) {
        return this.hostsMap.get(host);
    }

    /**
     * Get all host list
     * @return
     */
    public List<Hosts> getAllHosts() {
        return this.hostsMap.values().stream().collect(Collectors.toList());
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
            urls.add(path.toFile().toURI().toURL());
        }
        return urls.stream().toArray(URL[]::new);
    }

    /**
     * Get dynamic classpath list
     * @param host
     * @return
     */
    public Path getDynamicClaspaths(String host) {        
        return this.hostsMap.get(host).getDynamicClasspaths();
    }

    /**
     * Get all of dynamic classpath list
     * @return
     */
    public List<Path> getAllDynamicClasspaths() {
        return this.hostsMap.values().stream().map(h -> h.getDynamicClasspaths()).filter(cp -> cp != null).collect(Collectors.toList());
    }

    /**
     * Get all of spring JPA packages
     * @return
     */
    public List<String> getAllSpringPackages() {
        return this.hostsMap.values().stream().filter(h -> h != null).flatMap(h -> h.getSpringJPAPackages().getFilters().stream()).collect(Collectors.toList());
    }

    /**
     * Get all configured host names. It could be having same value.
     * @return
     */
    public List<String> getAllHostNames() {
        return this.hostsMap.keySet().stream().collect(Collectors.toList()); 
    }    

    /**
     * Whether the host is existing in this server
     * @param host
     * @return
     */
    public boolean isExistHost(final String host) {
        return getAllHostNames().stream().anyMatch(h -> h.equals(host));
    }

    /**
     * Get host user object
     * @param host
     * @return
     */
    public List<User> getUsers(String host) {
        return this.hostsMap.get(host).getUsers();
    }

    /**
     * Get default Hosts
     * @return
     */
    public Hosts getDefaultHosts() {
        return this.hostsMap.values().stream().filter(h -> h.isDefaultHost()).findAny().orElseThrow();
    }

    /**
     * Load error filters from config
     * @param host
     * @return
     */
    public List<Class<?>> loadErrorFilters(String host) {
        return this.hostsMap.get(host).getErrorFilters();
    }

    /**
     * Get host list matching with a port 
     * @param port
     * @return
     */
    public List<String> getHostsByPort(int port) {
        return this.hostsMap.values().stream().filter(h -> h.getPort() == port).map(h -> h.getHost()).collect(Collectors.toList());
    }

    /**
     * Get using ports
     * @return
     */ 
    public int[] getUsingPorts() {
        return this.hostsMap.values().stream().mapToInt(h -> h.getPort()).toArray();
    }

    /**
     * Get docroot path by host name
     * @param host
     * @return
     */
    public Path getDocroot(String host) {
        return this.hostsMap.get(host).getDocroot();
    }

    /**
     * Whether virtual host
     * @param host
     * @return
     */
    public boolean isVirtualHost(String host) {
        return !this.hostsMap.get(host).isDefaultHost();
    }

    /**
     * Get virtual host list
     * @return
     */
    public List<Hosts> getVirtualHosts() {
        return this.hostsMap.values().stream().filter(h -> !h.isDefaultHost()).collect(Collectors.toList());
    }

    /**
     * Get web protocol of Host or vHost
     * @param host
     * @return
     */
    public PROTOCOL getProtocol(String host) {
        return this.hostsMap.get(host).getProtocol();
    }

    /**
     * Get welcome file
     * @param host
     * @return
     */
    public File getWelcomeFile(String host) {
        return this.hostsMap.get(host).getWelcomeFile();
    }

    /**
     * Get server name by host
     * @param host
     * @return
     */
    public String getServerName(String host) {
        return this.hostsMap.get(host).getServerName();
    }

    /**
     * Get host's port
     * @param host
     * @return
     */
    public int getPort(String host) {
        return this.hostsMap.get(host).getPort();
    }

    /**
     * Get logger by host
     * @param host
     * @return
     */
    public Logger getLogger(String host) {
        return LoggerFactory.getLogger(host);
    }

    /**
     * Get log path
     * @param host
     * @return
     */
    public Path getLogPath(String host) {
        return this.hostsMap.get(host).getLogPath();
    }

    /**
     * Get log level
     * @param host
     * @return
     */
    public List<Level> getLogLevel(String host) {
        return this.hostsMap.get(host).getLogLevel();
    }

    /**
     * Get charset by the host
     * @param host
     * @return
     */
    public Charset charset(String host) {
        return this.hostsMap.get(host).charset();
    }

    /**
     * Get in-memory filters
     * @param host
     * @return
     */
    public List<String> getInMemoryFilters(String host) {
        return this.hostsMap.get(host).getInMemoryFilters();
    }

    /**
     * Get allowed resource pattern
     * @param host
     * @return
     */
    public List<String> getAccessFilters(String host) {
        return this.hostsMap.get(host).getAccessFilters();
    }

    /**
     * Filtering dynamic packages
     * @param host
     * @param resourceName
     * @return
     */
    public boolean filteringDynamicPackages(String host, String resourceName) {
        return this.hostsMap.get(host).filteringDynamicPackages(resourceName);
    }

    /**
     * Filtering Spring JPA packages
     * @param host
     * @param resourceName
     * @return
     */
    public boolean filteringSpringJPAPackages(String host, String resourceName) {
        return this.hostsMap.get(host).filteringSpringJPAPackages(resourceName);
    }

    /**
     * Filtering in-memory resources with specified resourceName
     * @param host
     * @param resourceName
     * @return
     */
    public boolean filteringInMemory(String host, String resourceName) {
        return getHosts(host).filteringInMemory(resourceName);
    }

    /**
     * Filtering in-disk resources with specified resourceName
     * @param resourceName
     * @return
     */
    public boolean filteringInAccess(String host, String resourceName) {
        return getHosts(host).filteringInAccess(resourceName);
    }
}
