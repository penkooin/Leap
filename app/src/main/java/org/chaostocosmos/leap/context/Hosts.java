package org.chaostocosmos.leap.context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.data.Filtering;
import org.chaostocosmos.leap.common.log.LEVEL;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.security.UserCredentials;

/**
 * Virtual host manager object
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class Hosts <T> extends Metadata <T> {

    /**
     * Constructor
     * @param hostsMap
     */
    public Hosts(T hostsMap) {
        super(hostsMap);
    }    

    /**
     * Get host object by name
     * @param hostId
     * @return
     */
    public Host<?> getHost(String hostId) {
        return new Host<T>(super.<List<T>>getValue("hosts")
                                .stream()
                                .filter(m -> ((Map<?, ?>)m).get("id").equals(hostId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Host not found!!!")));
    }

    /**
     * Get all host list
     * @return
     */
    public List<Host<?>> getHosts() {
        //List<String> hostIds = super.<List<Map<String, Object>>> getValue("hosts").stream().map(m -> m.get("id").toString()).collect(Collectors.toList());        
        List<String> hostIds = Context.get().server().getHosts().keySet().stream().collect(Collectors.toList());
        return hostIds.stream().map(h -> getHost(h)).collect(Collectors.toList());
    }

    /**
     * Get all of host id
     * @return
     */
    public List<String> getHostIds() {
        return getHosts().stream().map(h -> h.getId()).collect(Collectors.toList());
    }

    /**
     * Get server logs level
     * @return
     */
    public Path getServerLogsPath() {
        return Paths.get(super.<String> getValue("logs.path"));
    }

    /**
     * Get server logs level
     * @return
     */
    public LEVEL getServerLogsLevel() {
        return LEVEL.valueOf(super.<String> getValue("logs.level"));
    }

    /**
     * Get monitor image path
     * @return
     */
    public Path getMonitorImagePath() {
        return Paths.get(super.<String> getValue("monitor.image-path"));
    }

    /**
     * Get monitor probing interval
     * @return
     */
    public int getMonitorProbingInterval() {
        return super.<Integer> getValue("monitor.probing-interval");        
    }

    /**
     * Get flag whether monitor
     * @return
     */
    public boolean getSupportMonitoring() {
        return Boolean.valueOf(super.getValue("monitor.support-monitoring"));
    }

    /**
     * Get dynamic classpath list
     * @param hostId
     * @return
     */
    public List<Path> getDynamicClaspaths(String hostId) {        
        return getHost(hostId).getDynamicClassPaths();
    }

    /**
     * Get all of dynamic classpath list
     * @return
     */
    public List<Path> getAllDynamicClasspaths() {
        return getHosts().stream().flatMap(h -> h.getDynamicClassPaths().stream()).collect(Collectors.toList());
    }

    /**
     * Get all configured host names. 
     * It could be having same value.
     * @return
     */
    public List<String> getAllHostname() {
        return getHosts().stream().map(v -> v.getHost()).collect(Collectors.toList()); 
    }

    /**
     * Get host name matching with host 
     * @param host
     * @return
     */
    public String getId(String hostname) {
        Host<?> host = getHosts().stream().filter(h -> h.getHost().equals(hostname)).findAny().orElse(null);        
        if(host == null) {
            throw new LeapException(HTTP.RES500);
        }
        return host.getId();
    }

    /**
     * Get host's port
     * @param hostId
     * @return
     */
    public int getPort(String hostId) {
        return getHost(hostId).getPort();
    }

////////////////////////////////////////////////////////////////////////////////////////////    

    /**
     * Get using ports
     * @return
     */     
    public int[] getUsingPorts() {
        return getHosts().stream().mapToInt(h -> h.getPort()).toArray();
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
        return getHost(hostId).getIndexFile();
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
    public LEVEL getLogLevel(String hostId) {
        return getHost(hostId).getLogLevel();
    }

    /**
     * Get charset by the host
     * @param hostId
     * @return
     */
    public Charset charset(String hostId) {
        return getHost(hostId).charset();
    }

    /**
     * Get allowed resource pattern
     * @param hostId
     * @return
     */
    public Filtering getAccessFiltering(String hostId) {
        return getHost(hostId).getAllowedPathFiltering();
    }

    /**
     * Filtering in-disk resources with specified resourceName
     * @param hostId
     * @param resourceName
     * @return
     */
    public boolean filteringInAccess(String hostId, String resourceName) {
        return getHost(hostId).getAllowedPathFiltering().include(resourceName);
    }

    /**
     * Get host list matching with a port 
     * @param port
     * @return
     */
    public List<String> getHostsByPort(int port) {
        return getHosts().stream().filter(h -> h.getPort() == port).map(h -> h.getHost()).collect(Collectors.toList());
    }

    /**
     * Load error filters from config
     * @param hostId
     * @return
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     */
    // public List<Class<?>> loadErrorFilters(String hostId) throws ClassNotFoundException, MalformedURLException {
    //     List<?> filters = getHost(hostId).<List<?>>getErrorFilters();
    //     List<Class<?>> errorFilters = new ArrayList<>();
    //     for(Object obj : filters) {
    //         errorFilters.add(ClassUtils.getClassLoader().loadClass((String)obj));
    //     }
    //     return errorFilters;
    // }

    /**
     * Get host user object
     * @param hostId
     * @return
     */
    public List<UserCredentials> getUsers(String hostId) {
        return getHost(hostId).<List<Map<String, Object>>> getValue("global.users").stream().map(u -> new UserCredentials(u)).collect(Collectors.toList());
    }

    /**
     * Whether the host is existing in this server
     * @param watcherId
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
    public URL[] getAllDynamicClasspathURLs() {
        List<Path> paths = getAllDynamicClasspaths();
        return getPathsToURLs(paths);
    }

    /**
     * Get URL list from Path list
     * @param paths
     * @return
     */
    public static URL[] getPathsToURLs(List<Path> paths) {
        return paths.stream().map(p -> {
            try {
                return p.toFile().toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);
    }
}
