package org.chaostocosmos.leap.http.context;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.resources.Resources;
import org.chaostocosmos.leap.http.user.GRANT;
import org.chaostocosmos.leap.http.user.User;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Host object
 * 
 * @author 9ins
 */
public class Host <M> extends Metadata <M> {
    /**
     * Filtering objects
     */
    Filtering ipAllowedFiltering, ipForbiddenFiltering, inMemoryFiltering, accessFiltering, forbiddenFiltering, dynamicPackagesFiltering, springJpaPackagesFiltering, errorFiltering;
    /**
     * Resource object for host
     */
    private Resources resource;
    /**
     * weather host is default;
     */
    private boolean isDefaultHost;
    /**
     * Logger for Host
     */
    private Logger logger;
    /**
     * Default constructor
     * 
     * @param map
     * @throws IOException
     */
    public Host(M hostMap, boolean isDefaultHost) {
        super(hostMap);
        this.isDefaultHost = isDefaultHost;
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
        return super.getValue("id");
    }
    /**
     * Get server name
     * @param hostId
     */
    public void setHostId(String hostId) {
        super.setValue("id", hostId);
    }
    /**
     * Get specified web protocol
     * @return
     */
    public PROTOCOL getProtocol() {
        return PROTOCOL.protocol(super.getValue("protocol"));
    }
    /**
     * Set protocol
     * @param protocol
     */
    public void setProtocol(PROTOCOL protocol) {
        super.setValue("protocol", protocol.protocol());
    }
    /**
     * Get charset of the host
     * @return
     */
    public Charset charset() {
        return Charset.forName(super.getValue("charset"));
    }
    /**
     * Set charset 
     * @param charset
     */
    public void setCharset(Charset charset) {
        super.setValue("charset", charset.name());
    }
    /**
     * Get host name
     * @return
     */
    public String getHost() {
        return super.getValue("host");
    }
    /**
     * Set host name
     * @param host
     */
    public void setHost(String host) {
        super.setValue("host", host);
    }
    /**
     * Get port;
     * @return
     */
    public int getPort() {
        return super.getValue("port");
    }
    /**
     * Set port
     * @param port
     */
    public void setPort(int port) {
        super.setValue("port", port);
    }
    /**
     * Get users
     * @return
     */
    public List<User> getUsers() {        
        return (List<User>)super.<List<Map<String, Object>>>getValue("users")
                .stream()
                .map(m -> new User(m.get("username").toString(), m.get("password").toString(), GRANT.valueOf(m.get("grant").toString())))
                .collect(Collectors.toList());
    }
    /**
     * Set users
     * @param users
     */
    public void setUsers(List<User> users) {
        users.stream().map(u -> {
            Map<String, String> map = new HashMap<String, String>();
            map.put("username", u.getUsername());
            map.put("password", u.getPassword());
            map.put("grant", u.getGrant().name());
            return map;
        }).forEach(u -> super.<Map<String, String>>setValue("users", u));
    }
    /**
     * Get In-Memory unit size
     * @return
     */
    public int getInMemorySplitUnit() {
        return super.<Integer> getValue("resources.in-memory-split-unit");
    }
    /**
     * Get IP allowed filters 
     * @return
     */
    public List<String> getIpAllowedFilters() {
        return super.<List<String>>getValue("ip-filters.allowed");
    }
    /**
     * Set IP forbbiden filters
     * @param ipAllowedFilter
     */
    public void setIpAllowedFilters(List<String> ipAllowedFilter) {
        super.<List<String>>setValue("ip-filters.allowed", ipAllowedFilter);
    }
    /**
     * Get IP allowed Filtering objects
     * @return
     */
    public Filtering getIpAllowedFiltering() {
        if(this.ipAllowedFiltering == null) {
            this.ipAllowedFiltering = new Filtering(getIpAllowedFilters());
        }
        return this.ipAllowedFiltering;
    }
    /**
     * Get IP forbbiden filters
     * @return
     */
    public List<String> getIpForbbidenFilters() {
        return super.<List<String>>getValue("ip-filters.forbidden");
    }
    /**
     * Set IP forbbiden filters
     * @param ipForbiddenFilters
     */
    public void setIpForbbidenFilters(List<String> ipForbiddenFilters) {
        super.<List<String>>setValue("ip-filters.forbidden", ipForbiddenFilters);
    }
    /**
     * Get forbidden Filtering
     * @return
     */
    public Filtering getIpForbiddenFiltering() {
        if(ipForbiddenFiltering == null) {
            this.ipForbiddenFiltering = new Filtering(getIpForbbidenFilters());
        }
        return this.ipForbiddenFiltering;
    }
    /**
     * Get dynamic class path
     * @return
     */
    public Path getDynamicClasspaths() {
        return !super.<String>getValue("dynamic-classpath").equals("") ? Paths.get(super.<String>getValue("dynamic-classpath")) : null;
    }
    /**
     * Set synamic class path 
     * @param dynamicClasspaths
     */
    public void setDynamicClasspaths(Path dynamicClaspaths) {
        super.<String>setValue("dynamic-classpath", dynamicClaspaths.toAbsolutePath().toString());
    }
    /**
     * Get dynamic packages list
     * @return
     */
    public List<String> getDynamicPackages() {
        return super.<List<String>>getValue("dynamic-packages") == null ? new ArrayList<String>() : super.<List<String>>getValue("dynamic-packages");
    }
    /**
     * Set dynamic packages 
     * @param dynamicPackages
     */
    public void setDynamicPackages(List<String> dynamicPackages) {
        super.<List<String>>setValue("dynamic-packages", dynamicPackages);
    }
    /**
     * Get dynamic package Filtering object
     * @return
     */
    public Filtering getDynamicPackageFiltering() {
        if(this.dynamicPackagesFiltering == null) {
            this.dynamicPackagesFiltering = new Filtering(getDynamicPackages());
        }
        return this.dynamicPackagesFiltering;
    }
    /**
     * Get in-memory resource filters
     * @return
     */
    public List<String> getInMemoryFilters() {
        return super.<List<String>>getValue("resources.in-memory-filters");
    }
    /**
     * Set in-memory filters to config Map
     * @param inMemoryFilters
     */
    public void setInMemoryFilters(List<String> inMemoryFilters) {
        super.<List<String>>setValue("resources.in-memory-filters", inMemoryFilters);
    }
    /**
     * Get Filtering for being loaded resources to memory
     * @return
     */
    public Filtering getInMemoryFiltering() {
        if(inMemoryFiltering == null) {
            this.inMemoryFiltering = new Filtering(getInMemoryFilters());
        }        
        return this.inMemoryFiltering;
    }
    /**
     * Get access filters
     * @return
     */
    public List<String> getAccessFilters() {
        return super.<List<String>>getValue("resources.access-filters");
    }
    /**
     * Set access filters
     * @param accessFilters
     */
    public void setAccessFilters(List<String> accessFilters) {
        super.<List<String>>setValue("resources.access-filters", accessFilters);
    }
    /**
     * Get allowed resource filters
     * @return
     */
    public Filtering getAccessFiltering() {
        if(this.accessFiltering == null) {
            this.accessFiltering = new Filtering(super.<List<String>>getValue("resources.access-filters"));
        }
        return this.accessFiltering;
    }
    /**
     * Get forbidden filters
     * @return
     */
    public List<String> getForbiddenFilters() {
        return super.<List<String>>getValue("resources.forbidden-filters");
    }
    /**
     * Set forbidden filters
     * @param forbiddenFilters
     */
    public void setForbiddenFilters(List<String> forbiddenFilters) {
        super.<List<String>>setValue("resources.forbidden-filters", forbiddenFilters);
    }
    /**
     * Get forbidden Filtering object
     * @return
     */
    public Filtering getForbiddenFiltering() {
        if(this.forbiddenFiltering == null) {
            this.forbiddenFiltering = new Filtering(getForbiddenFilters());
        }
        return this.forbiddenFiltering;
    }
    /**
     * Get error Filtering
     * @return
     */
    public List<String> getErrorFilters() {
        return super.<List<String>>getValue("error-filters");
    }
    /**
     * Set error Filtering
     * @param errorFilters
     */
    public void setErrorFilters(List<String> errorFilters) {
        super.<List<String>>setValue("error-filters", errorFilters);
    }
    /**
     * Get docroot
     * @return
     */    
    public Path getDocroot() {
        return Paths.get(super.<String>getValue("doc-root")).toAbsolutePath();
    }
    /**
     * Set docroot
     * @param docroot
     */
    public void setDocroot(Path docroot) {
        super.<String>setValue("doc-root", docroot.toString());
    }
    /**
     * Get web app path
     * @return
     */
    public Path getWebApp() {
        return getDocroot().resolve("webapp");
    }
    /**
     * Get web inf path
     * @return
     */
    public Path getWebInf() {
        return getWebApp().resolve("WEB-INF");
    }
    /**
     * Get static content path
     * @return
     */
    public Path getStatic() {
        return getWebInf().resolve("static");
    }
    /**
     * Get services path
     */
    public Path getServices() {
        return getWebApp().resolve("services");
    }
    /**
     * get templates path
     */
    public Path getTemplates() {
        return getStatic().resolve("templates");
    }
    /**
     * Get welcome file
     * @return
     */
    public File getWelcomeFile() {
        return getStatic().resolve(super.<String>getValue("welcome")).toFile();
    }
    /**
     * Set welcome file
     * @param welcomeFile
     */
    public void setWelcomeFile(File welcomeFile) {
        super.<String>setValue("welcome", welcomeFile.getName());
    }
    /**
     * Get logPath
     * @return
     */
    public Path getLogPath() {
        return getDocroot().resolve(super.<String>getValue("logs"));
    }
    /**
     * Set logPath
     * @param logPath
     */
    public void setLogPath(Path logPath) {
        super.<String>setValue("logs", logPath.subpath(getDocroot().getNameCount(), logPath.getNameCount()).toString());
    }
    /**
     * Get log level
     * @return
     */
    public List<Level> getLogLevel() {
        return Arrays.asList(super.<String>getValue("log-level").split(",")).stream().map(l -> Level.toLevel(l.trim())).collect(Collectors.toList());
    }
    /**
     * Set log level
     * @param logLevel
     */
    public void setLogLevel(List<Level> logLevel) {
        super.<String>setValue("log-level", logLevel.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));
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
     * Get Logger
     * @return
     */
    public Logger getLogger() {
        return LoggerFactory.getLogger(getHostId());
    }
    @Override
    public String toString() {
        return super.toString();
    }
}
