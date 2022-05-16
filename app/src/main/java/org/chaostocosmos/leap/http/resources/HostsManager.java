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

import org.chaostocosmos.leap.http.commons.Filtering;
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
     * @param hostId
     * @return
     */
    public Hosts getHosts(String hostId) {
        return this.hostsMap.get(hostId);
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
     * @param hostId
     * @return
     */
    public Path getDynamicClaspaths(String hostId) {        
        return this.hostsMap.get(hostId).getDynamicClasspaths();
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
    public List<String> getAllHost() {
        return this.hostsMap.values().stream().map(v -> v.getHost()).collect(Collectors.toList()); 
    }    

    /**
     * Get host name matching with host 
     * @param hostId
     * @return
     */
    public String getHostId(String host) {
        return this.hostsMap.values().stream().filter(h -> h.getHost().equals(host)).findFirst().orElse(null).getHostId();
    }

    /**
     * Whether the host is existing in this server
     * @param host
     * @return
     */
    public boolean isExistHost(final String host) {
        return getAllHost().stream().anyMatch(h -> h.equals(host));
    }

    /**
     * Get host user object
     * @param hostId
     * @return
     */
    public List<User> getUsers(String hostId) {
        return this.hostsMap.get(hostId).getUsers();
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
     * @param hostId
     * @return
     */
    public List<Class<?>> loadErrorFilters(String hostId) {
        return this.hostsMap.get(hostId).getErrorFilters();
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
     * @param hostId
     * @return
     */
    public Path getDocroot(String hostId) {
        return this.hostsMap.get(hostId).getDocroot();
    }

    /**
     * Whether virtual host
     * @param hostId
     * @return
     */
    public boolean isVirtualHost(String hostId) {
        return !this.hostsMap.get(hostId).isDefaultHost();
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
     * @param hostId
     * @return
     */
    public PROTOCOL getProtocol(String hostId) {
        return this.hostsMap.get(hostId).getProtocol();
    }

    /**
     * Get welcome file
     * @param hostId
     * @return
     */
    public File getWelcomeFile(String hostId) {
        return this.hostsMap.get(hostId).getWelcomeFile();
    }

    /**
     * Get server name by host
     * @param hostId
     * @return
     */
    public String getServerName(String hostId) {
        return this.hostsMap.get(hostId).getHostId();
    }

    /**
     * Get host's port
     * @param hostId
     * @return
     */
    public int getPort(String hostId) {
        return this.hostsMap.get(hostId).getPort();
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
        return this.hostsMap.get(hostId).getLogPath();
    }

    /**
     * Get log level
     * @param hostId
     * @return
     */
    public List<Level> getLogLevel(String hostId) {
        return this.hostsMap.get(hostId).getLogLevel();
    }

    /**
     * Get charset by the host
     * @param hostId
     * @return
     */
    public Charset charset(String hostId) {
        return this.hostsMap.get(hostId).charset();
    }

    /**
     * Get in-memory filters
     * @param hostId
     * @return
     */
    public Filtering getInMemoryFilters(String hostId) {
        return this.hostsMap.get(hostId).getInMemoryFiltering();
    }

    /**
     * Get allowed resource pattern
     * @param host
     * @return
     */
    public Filtering getAccessFilters(String host) {
        return this.hostsMap.get(host).getAccessFiltering();
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
