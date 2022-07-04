package org.chaostocosmos.leap.http.context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
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
public class Hosts <M> extends Metadata <M> {

    Host<?> defaultHost;
    List<Host<?>> virtualHost;
    List<Host<?>> allHost;
    Map<String, Host<?>> hostMap;    

    /**
     * Constructor
     * 
     * @param hostsMap
     */
    public Hosts(M hostsMap) {        
        super(hostsMap);
        this.defaultHost = new Host<>(super.getValue("hosts.default"), true);
        this.virtualHost = super.<List<Map<String, Object>>>getValue("hosts.virtual").stream().map(m -> new Host<>(m, false)).collect(Collectors.toList());
        this.allHost = new ArrayList<>();
        this.allHost.add(this.defaultHost);
        this.allHost.addAll(this.virtualHost);
        this.hostMap = this.allHost.stream().map(h -> new Object[]{ h.getHostId(), h}).collect(Collectors.toMap(k -> (String)k[0], v -> (Host<?>)v[1]));
    }    

    /**
     * Get host object by name
     * @param hostId
     * @return
     */
    public Host<?> getHost(String hostId) {
        return hostId.equals(defaultHost.getHostId()) ? this.defaultHost : this.virtualHost.stream().filter(h -> h.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new IllegalArgumentException("There isn't exist host ID: "+hostId));
    }

    /**
     * Get default Hosts
     * @return
     */
    public Host<?> getDefaultHost() {
        return this.defaultHost;
    }

    /**
     * Get default host
     * @return
     */
    public String getDefaultHostName() {
        return this.defaultHost.getHost();
    }

    /**
     * Get default port
     * @return
     */
    public int getDefaultPort() {
        return this.defaultHost.getPort();
    }

    /**
     * Get all host list
     * @return
     */
    public List<Host<?>> getAllHosts() {
        this.virtualHost.add(0, this.defaultHost);
        return this.virtualHost;
    }

    /**
     * Get host Map
     * @return
     */
    public Map<String, Host<?>> getHostMap() {
        return this.hostMap;
    }

    /**
     * Get all of host id
     * @return
     */
    public List<String> getHostIds() {
        return getAllHosts().stream().map(h -> h.getHostId()).collect(Collectors.toList());
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

    /**
     * Get dynamic classpath list
     * @param hostId
     * @return
     */
    public Path getDynamicClaspaths(String hostId) {        
        return this.hostMap.get(hostId).getDynamicClasspaths();
    }

    /**
     * Get all of dynamic classpath list
     * @return
     */
    public List<Path> getAllDynamicClasspaths() {
        return this.hostMap.values().stream().map(h -> h.getDynamicClasspaths()).collect(Collectors.toList());
    }

    /**
     * Get all configured host names. It could be having same value.
     * @return
     */
    public List<String> getAllHost() {
        return this.hostMap.values().stream().map(v -> v.getHost()).collect(Collectors.toList()); 
    }    

    /**
     * Get host name matching with host 
     * @param hostId
     * @return
     */
    public String getHostId(String host) {
        return this.hostMap.values().stream().filter(h -> h.getHost().equals(host)).findFirst().orElse(null).getHostId();
    }

    /**
     * Whether the host is existing in this server
     * @param host
     * @return
     */
    public boolean isExistHost(String host) {
        return this.hostMap.keySet().stream().anyMatch(h -> h.equals(host));
    }

    /**
     * Get host user object
     * @param hostId
     * @return
     */
    public List<User<?>> getUsers(String hostId) {
        return this.hostMap.get(hostId).getUsers();
    }

    /**
     * Load error filters from config
     * @param hostId
     * @return
     */
    public List<Class<?>> loadErrorFilters(String hostId) {
        return this.hostMap.get(hostId).getErrorFilters().stream().map(e -> {
            try {
                return ClassUtils.getClassLoader().loadClass(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return null;
        }).filter(e -> e != null).collect(Collectors.toList());
    }

    /**
     * Get host list matching with a port 
     * @param port
     * @return
     */
    public List<String> getHostsByPort(int port) {
        return this.hostMap.values().stream().filter(h -> h.getPort() == port).map(h -> h.getHost()).collect(Collectors.toList());
    }

    /**
     * Get using ports
     * @return
     */ 
    public int[] getUsingPorts() {
        return this.hostMap.values().stream().mapToInt(h -> h.getPort()).toArray();
    }

    /**
     * Get docroot path by host name
     * @param hostId
     * @return
     */
    public Path getDocroot(String hostId) {
        return this.hostMap.get(hostId).getDocroot();
    }

    /**
     * Get template path by host getNameCount
     * @param hostId
     * @return
     */
    public Path getTemplates(String hostId) {
        return this.hostMap.get(hostId).getTemplates();
    }

    /**
     * Whether virtual host
     * @param hostId
     * @return
     */
    public boolean isVirtualHost(String hostId) {
        return !this.hostMap.get(hostId).isDefaultHost();
    }

    /**
     * Get virtual host list
     * @return
     */
    public List<Host<?>> getVirtualHosts() {
        return this.hostMap.values().stream().filter(h -> !h.isDefaultHost()).collect(Collectors.toList());
    }

    /**
     * Get web protocol of Host or vHost
     * @param hostId
     * @return
     */
    public PROTOCOL getProtocol(String hostId) {
        return this.hostMap.get(hostId).getProtocol();
    }

    /**
     * Get welcome file
     * @param hostId
     * @return
     */
    public File getWelcomeFile(String hostId) {
        return this.hostMap.get(hostId).getWelcomeFile();
    }

    /**
     * Get host's port
     * @param hostId
     * @return
     */
    public int getPort(String hostId) {
        return this.hostMap.get(hostId).getPort();
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
        return this.hostMap.get(hostId).getLogPath();
    }

    /**
     * Get log level
     * @param hostId
     * @return
     */
    public List<Level> getLogLevel(String hostId) {
        return this.hostMap.get(hostId).getLogLevel();
    }

    /**
     * Get charset by the host
     * @param hostId
     * @return
     */
    public Charset charset(String hostId) {
        return this.hostMap.get(hostId).charset();
    }

    /**
     * Get in-memory filters
     * @param hostId
     * @return
     */
    public Filtering getInMemoryFiltering(String hostId) {
        return this.hostMap.get(hostId).getInMemoryFiltering();
    }

    /**
     * Get allowed resource pattern
     * @param hostId
     * @return
     */
    public Filtering getAccessFiltering(String hostId) {
        return this.hostMap.get(hostId).getAccessFiltering();
    }

    /**
     * Filtering dynamic packages
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringDynamicPackages(String hostId, String resourceName) {
        return this.hostMap.get(hostId).getDynamicPackageFiltering().include(resourceName);
    }

    /**
     * Filtering in-memory resources with specified resourceName
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringInMemory(String hostId, String resourceName) {
        return this.hostMap.get(hostId).getInMemoryFiltering().include(resourceName);
    }

    /**
     * Filtering in-disk resources with specified resourceName
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringInAccess(String hostId, String resourceName) {
        return this.hostMap.get(hostId).getAccessFiltering().include(resourceName);
    }
}
