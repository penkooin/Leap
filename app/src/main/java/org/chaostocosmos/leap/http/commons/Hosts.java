package org.chaostocosmos.leap.http.commons;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    String serverName;

    /**
     * Web protocol type
     */
    PROTOCOL protocol;

    /**
     * Host charset
     */
    Charset charset;

    /**
     * Host name
     */
    String host;
    
    /**
     * Host port
     */
    int port;

    /**
     * User Map list
     */
    List<User> users;

    /**
     * Dynamic class path object
     */
    Path dynamicClaspaths;

    /**
     * Allowed resource filter string
     */
    List<String> allowedResourceFilters;

    /**
     * forbidden resource filter string
     */
    List<String> forbiddenResourceFilters;

    /**
     * Error filter classes
     */
    List<Class<?>> errorFilters;

    /**
     * Document root path object
     */
    Path docroot;

    /**
     * Welcome file object
     */
    File welcomeFile;

    /**
     * Log path object
     */
    Path logPath;

    /**
     * Log levels liste
     */
    List<Level> logLevel;

    /**
     * Host map object
     */
    Map<Object, Object> hostMap;

    /**
     * Default constructor
     */
    public Hosts(Map<Object, Object> map) {
        this.isDefaultHost = map.get("default") != null ? (boolean)map.get("default") : false;
        this.serverName = (String)map.get("server-name");
        this.protocol = PROTOCOL.getProtocol((String)map.get("protocol"));
        this.charset = Charset.forName((String)map.get("charset"));
        this.host = (String)map.get("host");
        this.port = (int)map.get("port");
        this.users = (List<User>)((List<Map<?, ?>>)map.get("users")).stream().map(m -> new User(m.get("username").toString(), m.get("password").toString(), GRANT.valueOf(m.get("grant").toString()))).collect(Collectors.toList());
        this.dynamicClaspaths = !map.get("dynamic-classpaths").equals("") ? Paths.get((String)map.get("dynamic-classpaths")) : null;
        this.allowedResourceFilters = (List<String>)((List<?>)((Map<?, ?>)map.get("resource-filters")).get("allowed")).stream().map(f -> f.toString()).collect(Collectors.toList());
        this.forbiddenResourceFilters = (List<String>)((List<?>)((Map<?, ?>)map.get("resource-filters")).get("forbidden")).stream().map(f -> f.toString()).collect(Collectors.toList());
        this.errorFilters = ((List<?>)map.get("error-filters")).stream().map(f -> ClassUtils.getClass(ClassLoader.getSystemClassLoader(), f.toString().trim())).collect(Collectors.toList());
        this.docroot = Paths.get((String)map.get("doc-root"));
        this.welcomeFile = Paths.get((String)map.get("doc-root")).resolve((String)map.get("welcome")).toFile();
        this.logPath = Paths.get((String)map.get("logs"));
        this.logLevel = UtilBox.getLogLevels((String)map.get("log-level"), ",");    
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
     * @param allowedResourceFilters
     * @param forbiddenResourceFilters
     * @param errorFilters
     * @param docroot
     * @param welcomeFile
     * @param logPath
     * @param logLevel
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
                 List<String> allowedResourceFilters, 
                 List<String> forbiddenResourceFilters,
                 List<Class<?>> errorFilters, 
                 Path docroot, 
                 File welcomeFile,
                 Path logPath,  
                 List<Level> logLevel)  {
        this.isDefaultHost = isDefaultHost;
        this.serverName = serverName;
        this.protocol = protocol;
        this.charset = charset;
        this.host = host;
        this.port = port;
        this.users = users;
        this.dynamicClaspaths = dynamicClaspaths;
        this.allowedResourceFilters = allowedResourceFilters;
        this.forbiddenResourceFilters = forbiddenResourceFilters;
        this.errorFilters = errorFilters;
        this.docroot = docroot.normalize();
        this.welcomeFile = welcomeFile;
        this.logPath = logPath;
        this.logLevel = logLevel;
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
    public Charset getCharset() {
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
        return this.dynamicClaspaths;
    }

    /**
     * Set synamic class path 
     * @param dynamicClasspaths
     */
    public void setDynamicClasspaths(Path dynamicClaspaths) {
        this.dynamicClaspaths = dynamicClaspaths;
    }

    /**
     * Get allowed resource filters
     * @return
     */
    public List<String> getAllowedResourceFilters() {
        return this.allowedResourceFilters;
    }

    /**
     * Set allowed resource filters
     * @param allowedResourceFilters
     */
    public void setAllowedResourceFilters(List<String> allowedResourceFilters) {
        this.allowedResourceFilters = allowedResourceFilters;
    }

    /**
     * Get forbidden resource filters
     * @return
     */
    public List<String> getForbiddenResourceFilters() {
        return this.allowedResourceFilters;
    }

    /**
     * Set forbidden resource filter
     * @param forbiddenResourceFilters
     */
    public void setForbiddenResourceFilters(List<String> forbiddenResourceFilters) {
        this.forbiddenResourceFilters = forbiddenResourceFilters;
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
            " isDefaultHost='" + isDefaultHost() + "'" +
            ", serverName='" + getServerName() + "'" +
            ", protocol='" + getProtocol() + "'" +
            ", charset='" + getCharset() + "'" +
            ", host='" + getHost() + "'" +
            ", port='" + getPort() + "'" +
            ", users='" + getUsers() + "'" +
            ", dynamicClaspaths='" + getDynamicClasspaths() + "'" +
            ", allowedResourceFilters='" + getAllowedResourceFilters() + "'" +
            ", forbiddenResourceFilters='" + getForbiddenResourceFilters() + "'" +
            ", errorFilters='" + getErrorFilters() + "'" +
            ", docroot='" + getDocroot() + "'" +
            ", welcomeFile='" + getWelcomeFile() + "'" +
            ", logPath='" + getLogPath() + "'" +
            ", logLevel='" + getLogLevel() + "'" +
            "}";
    }
}
