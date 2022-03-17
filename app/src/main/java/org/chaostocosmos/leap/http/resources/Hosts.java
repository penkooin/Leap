package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.ClassUtils;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.user.GRANT;
import org.chaostocosmos.leap.http.user.User;

import ch.qos.logback.classic.Level;

/**
 * Host object
 */
public class Hosts {
    /**
     * Whether main host
     */
    private boolean isDefaultHost;

    /**
     * Server name
     */
    private String serverName;

    /**
     * Web protocol type
     */
    private PROTOCOL protocol;

    /**
     * Host charset
     */
    private Charset charset;

    /**
     * Host name
     */
    private String host;
    
    /**
     * Host port
     */
    private int port;

    /**
     * User Map list
     */
    private List<User> users;

    /**
     * Dynamic class path object
     */
    private Path dynamicClasspaths;

    /**
     * Resources filter to be in memory area
     */
    private List<String> inMemoryFilter;

    /**
     * Allowed resource filter string
     */
    private List<String> accessFilters;

    /**
     * Error filter classes
     */
    private List<Class<?>> errorFilters;

    /**
     * Document root path object
     */
    private Path docroot;

    /**
     * webapp path
     */
    private Path webapp;

    /**
     * WEB-INF path
     */
    private Path webinf;

    /**
     * Static contents path
     */
    private Path statics;

    /**
     * Dynamic services path
     */
    private Path services;

    /**
     * Welcome file object
     */
    private File welcomeFile;

    /**
     * Log path object
     */
    private Path logPath;

    /**
     * Log levels liste
     */
    private List<Level> logLevel;

    /**
     * Host map object
     */
    private Map<Object, Object> hostsMap;

    /**
     * Resource object for host
     */
    private Resource resource;

    /**
     * Default constructor
     * @param map
     * @throws IOException
     */
    public Hosts(Map<Object, Object> map) throws IOException {
        this(
        map.get("default") != null ? (boolean)map.get("default") : false,
        (String)map.get("server-name"),
        PROTOCOL.getProtocol((String)map.get("protocol")),
        Charset.forName((String)map.get("charset")),
        (String)map.get("host"),
        (int)map.get("port"),
        (List<User>)((List<Map<?, ?>>)map.get("users")).stream().map(m -> new User(m.get("username").toString(), m.get("password").toString(), GRANT.valueOf(m.get("grant").toString()))).collect(Collectors.toList()),
        !map.get("dynamic-classpaths").equals("") ? Paths.get((String)map.get("dynamic-classpaths")) : null,
        (List<String>)((Map<?, ?>)map.get("resource")).get("in-memory-filters"),
        (List<String>)((Map<?, ?>)map.get("resource")).get("access-filters"),
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
        null);
    }

    /**
     * Constructor
     * @param isDefaultHost
     * @param serverName
     * @param protocol
     * @param charset
     * @param host
     * @param port
     * @param users
     * @param dynamicClasspaths
     * @param inMemoryFilter
     * @param accessFilters
     * @param errorFilters
     * @param docroot
     * @param webinf
     * @param statics
     * @param services
     * @param welcomeFile
     * @param logPath
     * @param logLevel
     * @param hostsMap
     * @param resource
     */
    public Hosts(
                 boolean isDefaultHost, 
                 String serverName, 
                 PROTOCOL protocol,
                 Charset charset,
                 String host, 
                 int port, 
                 List<User> users, 
                 Path dynamicClaspaths, 
                 List<String> inMemoryFilter, 
                 List<String> accessFilters, 
                 List<Class<?>> errorFilters, 
                 Path docroot, 
                 Path webapp,
                 Path webinf,
                 Path statics,
                 Path services,
                 File welcomeFile,
                 Path logPath,  
                 List<Level> logLevel,
                 Map<Object, Object> hostsMap,
                 Resource resource
                 )  {
        this.isDefaultHost = isDefaultHost;
        this.serverName = serverName;
        this.protocol = protocol;
        this.charset = charset;
        this.host = host;
        this.port = port;
        this.users = users;
        this.dynamicClasspaths = dynamicClaspaths;
        this.inMemoryFilter = inMemoryFilter;
        this.accessFilters = accessFilters;
        this.errorFilters = errorFilters;
        this.docroot = docroot.toAbsolutePath().normalize();
        this.webapp = webapp.toAbsolutePath().normalize();
        this.webinf = webinf.toAbsolutePath().normalize();
        this.statics = statics.toAbsolutePath().normalize();
        this.services = services.toAbsolutePath().normalize();
        this.welcomeFile = welcomeFile;
        this.logPath = logPath;
        this.logLevel = logLevel;
        this.hostsMap = hostsMap;
        this.resource = resource;
    }

    /**
     * Whether main host
     * @return
     */
    public boolean isDefaultHost() {
        return this.isDefaultHost;
    }

    /**
     * Get server name
     * @return
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Get server name
     * @param serverName
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Get specified web protocol
     * @return
     */
    public PROTOCOL getProtocol() {
        return this.protocol;
    }

    /**
     * Set protocol
     * @param protocol
     */
    public void setProtocol(PROTOCOL protocol) {
        this.protocol = protocol;
    }

    /**
     * Get charset of the host
     * @return
     */
    public Charset charset() {
        return this.charset;
    }

    /**
     * Set charset 
     * @param charset
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Get host name
     * @return
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Set host name
     * @param hostName
     */
    public void setHost(String hostName) {
        this.host = hostName;
    }

    /**
     * Get port;
     * @return
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Set port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get users
     * @return
     */
    public List<User> getUsers() {
        return this.users;
    }

    /**
     * Set users
     * @param users
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Get dynamic class path
     * @return
     */
    public Path getDynamicClasspaths() {
        return this.dynamicClasspaths;
    }

    /**
     * Set synamic class path 
     * @param dynamicClasspaths
     */
    public void setDynamicClasspaths(Path dynamicClaspaths) {
        this.dynamicClasspaths = dynamicClaspaths;
    }

    /**
     * Get filters for being loaded resources to memory
     * @return
     */
    public List<String> getInMemoryFilters() {
        return this.inMemoryFilter;
    }

    /**
     * Set filters for being loaded resources to memory
     * @param resourcesInMemory
     */
    public void setInMemoryFilter(List<String> inMemoryFilter) {
        this.inMemoryFilter = inMemoryFilter;
    }

    /**
     * Get allowed resource filters
     * @return
     */
    public List<String> getAccessFilters() {
        return this.accessFilters;
    }

    /**
     * Set allowed resource filters
     * @param allowedResourceFilters
     */
    public void setAccessFilters(List<String> accessFilters) {
        this.accessFilters = accessFilters;
    }

    /**
     * Get error filters
     * @return
     */
    public List<Class<?>> getErrorFilters() {
        return this.errorFilters;
    }

    /**
     * Set error filters
     * @param errorFilters
     */
    public void setErrorFilters(List<Class<?>> errorFilters) {
        this.errorFilters = errorFilters;
    }  

    /**
     * Filtering in-memory resources with specified resourceName
     * @param resourceName
     * @return
     */
    public boolean filteringInMemory(String resourceName) {
        return filtering(resourceName, this.inMemoryFilter);
    }

    /**
     * Filtering in-disk resources with specified resourceName
     * @param resourceName
     * @return
     */
    public boolean filteringInAccess(String resourceName) {
        return filtering(resourceName, this.accessFilters);
    }

    /**
     * Whether resource name is corresponded with in-memory filters
     * @param resourceName
     * @param filters
     * @return
     */
    public boolean filtering(String resourceName, List<String> filters) {
        return filters.stream().anyMatch(f -> !f.trim().equals("") && resourceName.matches(Arrays.asList(f.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*"));
    }

    /**
     * Get docroot
     * @return
     */    
    public Path getDocroot() {
        return this.docroot;
    }

    /**
     * Set docroot
     * @param docroot
     */
    public void setDocroot(Path docroot) {
        this.docroot = docroot;
    }

    /**
     * Get web app path
     * @return
     */
    public Path getWebApp() {
        return this.webapp;
    }

    /**
     * Set web app path
     * @param webapp
     */
    public void setWebapp(Path webapp) {
        this.webapp = webapp;
    }

    /**
     * Get web inf path
     * @return
     */
    public Path getWebInf() {
        return this.webinf;
    }

    /**
     * Set web inf path
     * @param webinf
     */
    public void setWebInf(Path webinf) {
        this.webinf = webinf;
    }

    /**
     * Get static content path
     * @return
     */
    public Path getStatic() {
        return this.statics;
    }

    /**
     * Set static content path
     * @param statics
     */
    public void setStatic(Path statics) {
        this.statics = statics;
    }

    /**
     * Get services path
     */
    public Path getServices() {
        return this.services;
    }

    /**
     * Set services path
     */
    public void setServices(Path services) {
        this.services = services;
    }

    /**
     * Get welcome file
     * @return
     */
    public File getWelcomeFile() {
        return this.welcomeFile;
    }

    /**
     * Set welcome file
     * @param welcomeFile
     */
    public void setWelcomeFile(File welcomeFile) {
        this.welcomeFile = welcomeFile;
    }

    /**
     * Get logPath
     * @return
     */
    public Path getLogPath() {
        return this.logPath;
    }

    /**
     * Set logPath
     * @param logPath
     */
    public void setLogger(Path logPath) {
        this.logPath = logPath;
    }

    /**
     * Get log level
     * @return
     */
    public List<Level> getLogLevel() {
        return this.logLevel;
    }

    /**
     * Set log level
     * @param logLevel
     */
    public void setLogLevel(List<Level> logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Get hosts Map
     * @return
     */
    public Map<Object, Object> getHostsMap() {
        return this.hostsMap;
    }

    /**
     * Get resource for host object
     * @return
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * Set resource for host object
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Get InetSocketAddress
     */
    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    /**
     * Assing to Map
     */
    public Map<Object, Object> assignToMap() {
        return ClassUtils.mappingToMap(this);
    }


    @Override
    public String toString() {
        return "{" +
            " isDefaultHost='" + isDefaultHost + "'" +
            ", serverName='" + serverName + "'" +
            ", protocol='" + protocol + "'" +
            ", charset='" + charset + "'" +
            ", host='" + host + "'" +
            ", port='" + port + "'" +
            ", users='" + users + "'" +
            ", dynamicClasspaths='" + dynamicClasspaths + "'" +
            ", inMemoryFilter='" + inMemoryFilter + "'" +
            ", accessFilters='" + accessFilters + "'" +
            ", errorFilters='" + errorFilters + "'" +
            ", docroot='" + docroot + "'" +
            ", webapp='" + webapp + "'" +
            ", webinf='" + webinf + "'" +
            ", statics='" + statics + "'" +
            ", services='" + services + "'" +
            ", welcomeFile='" + welcomeFile + "'" +
            ", logPath='" + logPath + "'" +
            ", logLevel='" + logLevel + "'" +
            ", hostsMap='" + hostsMap + "'" +
            ", resource='" + resource + "'" +
            "}";
    }
}
