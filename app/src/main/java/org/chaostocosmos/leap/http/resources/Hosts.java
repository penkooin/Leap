package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
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
     * Host id
     */
    private String hostId;

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
     * Dynamic class Path
     */
    private Path dynamicClasspaths;

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
     * Template path
     */
    private Path template;

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
     * Each Filtering
     */
    private Filtering inMemoryFiltering, 
                      accessFiltering, 
                      forbiddenFiltering, 
                      ipAllowedFiltering, 
                      ipForbiddenFiltering, 
                      dynamicPackagesFiltering, 
                      springJpaPackagesFiltering
                      ;
    
    /**
     * Error Filtering
     */
    private List<Class<?>> errorFilters;

    /**
     * Resource object for host
     */
    private Resources resource;

    /**
     * Default constructor
     * @param map
     * @throws IOException
     */
    // public Hosts(Map<Object, Object> map) throws IOException {
    //     this(            
    //         map.get("default") != null ? (boolean)map.get("default") : false,
    //         (String)map.get("server-name"),
    //         PROTOCOL.getProtocol((String)map.get("protocol")),
    //         Charset.forName((String)map.get("charset")),
    //         (String)map.get("host"),
    //         (int)map.get("port"),
    //         (List<User>)((List<Map<?, ?>>)map.get("users")).stream().map(m -> new User(m.get("username").toString(), m.get("password").toString(), GRANT.valueOf(m.get("grant").toString()))).collect(Collectors.toList()),
    //         !map.get("dynamic-classpath").equals("") ? Paths.get((String)map.get("dynamic-classpath")) : null,
    //         new Filtering(map.get("dynamic-packages") == null ? new ArrayList<>() : (List<String>)map.get("dynamic-packages")),
    //         new Filtering(map.get("spring-jpa-packages") == null ? new ArrayList<>() : (List<String>)map.get("spring-jpa-packages")),
    //         new Filtering((List<String>)((Map<?, ?>)map.get("resource")).get("in-memory-filters")),
    //         new Filtering((List<String>)((Map<?, ?>)map.get("resource")).get("access-filters")),
    //         new Filtering((List<String>)((Map<?, ?>)map.get("resource")).get("forbidden-filters")),
    //         new Filtering((List<String>)((Map<?, ?>)map.get("ip-filter")).get("allowed")),
    //         new Filtering((List<String>)((Map<?, ?>)map.get("ip-filter")).get("forbidden")),
    //         ((List<?>)map.get("error-filters")).stream().map(f -> ClassUtils.getClass(ClassLoader.getSystemClassLoader(), f.toString().trim())).collect(Collectors.toList()),
    //         Paths.get((String)map.get("doc-root")),
    //         Paths.get((String)map.get("doc-root")).resolve("webapp"),
    //         Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF"),
    //         Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF").resolve("static"),
    //         Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("services"),
    //         Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF").resolve("template"),
    //         Paths.get((String)map.get("doc-root")).resolve("webapp").resolve("WEB-INF").resolve("template").resolve(map.get("welcome")+"").toFile(),
    //         Paths.get((String)map.get("logs")),
    //         UtilBox.getLogLevels((String)map.get("log-level"), ","),
    //         map,
    //         null
    //     );
    // }

    /**
     * Constructor
     * @param isDefaultHost
     * @param hostId
     * @param protocol
     * @param charset
     * @param host
     * @param port
     * @param users
     * @param dynamicClasspaths
     * @param dynamicPackagesFiltering
     * @param springJpaPackagesFiltering
     * @param inMemoryFiltering
     * @param accessFiltering
     * @param forbiddenFiltering
     * @param errorFiltering
     * @param ipAllowedFiltering
     * @param ipForbiddenFiltering
     * @param docroot
     * @param webinf
     * @param statics
     * @param services
     * @param template
     * @param welcomeFile
     * @param logPath
     * @param logLevel
     * @param resource
     */
    public Hosts(
                 boolean isDefaultHost, 
                 String hostId, 
                 PROTOCOL protocol,
                 Charset charset,
                 String host, 
                 int port, 
                 List<User> users, 
                 Path dynamicClaspaths, 
                 Filtering dynamicPackagesFiltering,
                 Filtering springJpaPackagesFiltering,
                 Filtering inMemoryFiltering, 
                 Filtering accessFiltering, 
                 Filtering forbiddenFiltering,
                 Filtering ipAllowedFiltering,
                 Filtering ipForbiddenFiltering,
                 List<Class<?>> errorFiltering, 
                 Path docroot, 
                 Path webapp,
                 Path webinf,
                 Path statics,
                 Path services,
                 Path template,
                 File welcomeFile,
                 Path logPath,  
                 List<Level> logLevel,
                 Resources resource
                 )  {
        this.isDefaultHost = isDefaultHost;
        this.hostId = hostId;
        this.protocol = protocol;
        this.charset = charset;
        this.host = host;
        this.port = port;
        this.users = users;
        this.dynamicClasspaths = dynamicClaspaths;
        this.dynamicPackagesFiltering = dynamicPackagesFiltering;
        this.springJpaPackagesFiltering = springJpaPackagesFiltering;
        this.inMemoryFiltering = inMemoryFiltering;
        this.accessFiltering = accessFiltering;
        this.forbiddenFiltering = forbiddenFiltering;
        this.ipAllowedFiltering = ipAllowedFiltering;
        this.ipForbiddenFiltering = ipForbiddenFiltering;
        this.errorFilters = errorFiltering;
        this.docroot = docroot.toAbsolutePath().normalize();
        this.webapp = webapp.toAbsolutePath().normalize();
        this.webinf = webinf.toAbsolutePath().normalize();
        this.statics = statics.toAbsolutePath().normalize();
        this.services = services.toAbsolutePath().normalize();
        this.template = template.toAbsolutePath().normalize();
        this.welcomeFile = welcomeFile;
        this.logPath = logPath;
        this.logLevel = logLevel;
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
    public String getHostId() {
        return this.hostId;
    }

    /**
     * Get server name
     * @param hostId
     */
    public void setHostId(String hostId) {
        this.hostId = hostId;
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
     * Get dynamic package Filtering
     * @return
     */
    public Filtering getDynamicPackages() {
        return this.dynamicPackagesFiltering;
    }

    /**
     * Get spring JPA scan packages
     * @return
     */
    public Filtering getSpringJPAPackages() {
        return this.springJpaPackagesFiltering;
    }

    /**
     * Get Filtering for being loaded resources to memory
     * @return
     */
    public Filtering getInMemoryFiltering() {
        return this.inMemoryFiltering;
    }

    /**
     * Get allowed resource filters
     * @return
     */
    public Filtering getAccessFiltering() {
        return this.accessFiltering;
    }

    /**
     * Get forbidden Filtering
     * @return
     */
    public Filtering getForbiddenFiltering() {
        return this.forbiddenFiltering;
    }

    /**
     * Get IP allowed Filtering
     * @return
     */
    public Filtering getIpAllowedFiltering() {
        return this.ipAllowedFiltering;
    }

    /**
     * Get IP forbidden Filtering
     * @return
     */
    public Filtering getIpForbiddenFiltering() {
        return this.ipForbiddenFiltering;
    }

    /**
     * Get error Filtering
     * @return
     */
    public List<Class<?>> getErrorFilters() {
        return this.errorFilters;
    }

    /**
     * Set error Filtering
     * @param errorFilters
     */
    public void setErrorFilters(List<Class<?>> errorFilters) {
        this.errorFilters = errorFilters;
    }  

    /**
     * Filtering dynamic packages
     * @param resourceName
     * @return
     */
    public boolean filteringDynamicPackages(String resourceName) {
        return this.dynamicPackagesFiltering.include(resourceName);
    }

    /**
     * Filtering Spring JPA packages
     * @param resourceName
     * @return
     */
    public boolean filteringSpringJPAPackages(String resourceName) {
        return this.springJpaPackagesFiltering.include(resourceName);
    }

    /**
     * Filtering in-memory resources with specified resourceName
     * @param resourceName
     * @return
     */
    public boolean filteringInMemory(String resourceName) {
        return this.inMemoryFiltering.include(resourceName);
    }

    /**
     * Filtering in-disk resources with specified resourceName
     * @param resourceName
     * @return
     */
    public boolean filteringInAccess(String resourceName) {
        return this.accessFiltering.include(resourceName);
    }

    /**
     * Filtering forbidden resources
     * @param resourceName
     * @return
     */
    public boolean filteringInForbidden(String resourceName) {
        return this.forbiddenFiltering.include(resourceName);
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
     * Get resource for host object
     * @return
     */
    public Resources getResource() {
        return this.resource;
    }

    /**
     * Set resource for host object
     */
    public void setResource(Resources resource) {
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
            ", serverName='" + hostId + "'" +
            ", protocol='" + protocol + "'" +
            ", charset='" + charset + "'" +
            ", host='" + host + "'" +
            ", port='" + port + "'" +
            ", users='" + users + "'" +
            ", dynamicClasspaths='" + dynamicClasspaths + "'" +
            ", dynamicPackages='" + dynamicPackagesFiltering + "'" +
            ", springJpaPackages='" + springJpaPackagesFiltering + "'" +
            ", inMemoryFilter='" + inMemoryFiltering + "'" +
            ", accessFiltering='" + accessFiltering + "'" +
            ", forbiddenFiltering='" + forbiddenFiltering + "'" +
            ", ipAllowedFiltering='" + ipAllowedFiltering + "'" +
            ", ipForbiddenFiltering='" + ipForbiddenFiltering + "'" +
            ", errorFilters='" + errorFilters + "'" +
            ", docroot='" + docroot + "'" +
            ", webapp='" + webapp + "'" +
            ", webinf='" + webinf + "'" +
            ", statics='" + statics + "'" +
            ", services='" + services + "'" +
            ", template='" + template + "'" +
            ", welcomeFile='" + welcomeFile + "'" +
            ", logPath='" + logPath + "'" +
            ", logLevel='" + logLevel + "'" +
            ", resource='" + resource + "'" +
            "}";
    }
}
