package org.chaostocosmos.leap.context;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.common.Filtering;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.STATUS;
import org.chaostocosmos.leap.resource.ResourcesModel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Host object
 * 
 * @author 9ins
 */
public class Host <T> extends Metadata<T> {
    /**
     * Default constructor
     * @param map
     * @throws IOException
     */
    public Host(T metaMap) {
        super(metaMap);
    }

    /**
     * Whether main host
     * @return
     */
    public <V> V isDefaultHost() {
        return super.getValue("default");
    }

    /**
     * Get server name
     * @return
     */
    public <V> V getHostId() {
        return super.getValue("id");
    }

    /**
     * Get server name
     * @param hostId
     */
    public <V> void setHostId(V hostId) {
        super.setValue("id", hostId);
    }

    /**
     * Get locale
     * @param <V>
     * @return
     */
    public <V> V getLocale() {
        return super.getValue("locale");
    }

    /**
     * Set locale
     * @param <V>
     * @param locale
     */
    public <V> void setLocale(V locale) {
        super.setValue("locale", locale);
    }

    /**
     * Get specified web protocol
     * @return
     */
    public <V> V getProtocol() {
        return super.getValue("protocol");
    }

    /**
     * Set protocol
     * @param protocol
     */
    public <V> void setProtocol(V protocol) {
        super.setValue("protocol", protocol);
    }

    /**
     * Get charset of the host
     * @return
     */
    public <V> V charset() {
        return super.getValue("charset");
    }

    /**
     * Set charset 
     * @param charset
     */
    public <V> void setCharset(V charset) {
        super.setValue("charset", charset);
    }

    /**
     * Get host name
     * @return
     */
    public <V> V getHost() {
        return super.getValue("host");
    }

    /**
     * Set host name
     * @param host
     */
    public <V> void setHost(V host) {
        super.setValue("host", host);
    }

    /**
     * Get port;
     * @return
     */
    public <V> V getPort() {
        return super.getValue("port");
    }

    /**
     * Set port
     * @param port
     */
    public <V> void setPort(V port) {
        super.setValue("port", port);
    }

    /**
     * Get users
     * @return
     */
    public <V> V getUsers() {        
        return super.getValue("users");
    }

    /**
     * Set users
     * @param users
     */    
    public <V> void setUsers(V users) {
        super.setValue("users", users);
    }

    /**
     * Get streaming buffer size
     * @param <V>
     * @return
     */
    public <V> V getStreamingBufferSize() {
        return super.getValue("resources.streaming-buffer-size");
    }

    /**
     * Get In-Memory unit size
     * @return
     */
    public <V> V getInMemorySplitUnit() {
        return super.getValue("resources.in-memory-split-unit");
    }

    /**
     * Get IP allowed filters 
     * @return
     */
    public <V> V getIpAllowedFilters() {
        return super.getValue("ip-filters.allowed");
    }

    /**
     * Set IP forbbiden filters
     * @param ipAllowedFilter
     */
    public <V> void setIpAllowedFilters(V ipAllowedFilter) {
        super.setValue("ip-filters.allowed", ipAllowedFilter);
    }

    /**
     * Get IP allowed Filtering objects
     * @return
     */
    public Filtering getIpAllowedFiltering() {
        return new Filtering(this.getIpAllowedFilters());
    }

    /**
     * Get IP forbbiden filters
     * @return
     */
    public <V> V getIpForbbidenFilters() {
        return super.getValue("ip-filters.forbidden");
    }
    
    /**
     * Set IP forbbiden filters
     * @param ipForbiddenFilters
     */
    public <V> void setIpForbbidenFilters(V ipForbiddenFilters) {
        super.setValue("ip-filters.forbidden", ipForbiddenFilters);
    }

    /**
     * Get forbidden Filtering
     * @return
     */
    public Filtering getIpForbiddenFiltering() {
        return new Filtering(getIpForbbidenFilters());
    }

    /**
     * Get dynamic class path
     * @return
     */
    public <V> V getDynamicClasspaths() {
        return !super.getValue("dynamic-classpath").equals("") ? super.getValue("dynamic-classpath") : null;
    }

    /**
     * Set synamic class path 
     * @param dynamicClasspaths
     */
    public <V> void setDynamicClasspaths(V dynamicClaspaths) {
        super.setValue("dynamic-classpath", dynamicClaspaths);
    }

    /**
     * Get dynamic packages list
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V getDynamicPackages() {
        return super.getValue("dynamic-classpath") == null ? (V) new ArrayList<String>() : super.getValue("dynamic-classpath");
    }

    /**
     * Set dynamic packages 
     * @param dynamicClasspath
     */
    public <V> void setDynamicPackages(V dynamicClasspath) {
        super.setValue("dynamic-classpath", dynamicClasspath);
    }

    /**
     * Get dynamic package Filtering object
     * @return
     */
    public Filtering getDynamicPackageFiltering() {
        return new Filtering(getDynamicPackages());
    }

    /**
     * Get in-memory resource filters
     * @return
     */
    public <V> V getInMemoryFilters() {
        return super.getValue("resources.in-memory-filters");
    }

    /**
     * Set in-memory filters to config Map
     * @param inMemoryFilters
     */
    public <V> void setInMemoryFilters(V inMemoryFilters) {
        super.setValue("resources.in-memory-filters", inMemoryFilters);
    }

    /**
     * Get Filtering for being loaded resources to memory
     * @return
     */
    public Filtering getInMemoryFiltering() {
        return new Filtering(getInMemoryFilters());
    }

    /**
     * Get access filters
     * @return
     */
    public <V> V getAccessFilters() {
        return super.getValue("resources.access-filters");
    }

    /**
     * Set access filters
     * @param accessFilters
     */
    public <V> void setAccessFilters(V accessFilters) {
        super.setValue("resources.access-filters", accessFilters);
    }

    /**
     * Get allowed resource filters
     * @return
     */
    public Filtering getAccessFiltering() {
        return new Filtering(super.getValue("resources.access-filters"));
    }

    /**
     * Get forbidden filters
     * @return
     */
    public <V> V getForbiddenFilters() {
        return super.getValue("resources.forbidden-filters");
    }

    /**
     * Set forbidden filters
     * @param forbiddenFilters
     */
    public <V> void setForbiddenFilters(V forbiddenFilters) {
        super.setValue("resources.forbidden-filters", forbiddenFilters);
    }

    /**
     * Get forbidden Filtering object
     * @return
     */
    public Filtering getForbiddenFiltering() {
        return new Filtering(getForbiddenFilters());
    }

    /**
     * Get whether show error-details content to client ( true or false)
     */
    public <V> V getErrorDetails() {
        return super.getValue("error-details");
    }

    /**
     * Set whether show error-details content to client ( true or false)
     */
    public <V> void setErrorDetails(V errorDetails) {
        super.setValue("error-details", errorDetails);
    }

    /**
     * Get error Filtering
     * @return
     */
    public <V> V getErrorFilters() {
        return super.getValue("error-filters");
    }

    /**
     * Set error Filtering
     * @param errorFilters
     */
    public <V> void setErrorFilters(V errorFilters) {
        super.setValue("error-filters", errorFilters);
    }

    /**
     * Set docroot
     * @param docroot
     */
    public <V> void setDocroot(V docroot) {
        super.setValue("doc-root", docroot);
    }

    /**
     * Get welcome file name
     * @param <V>
     * @return
     */
    public <V> V getWelcome() {
        return super.getValue("welcome");
    }

    /**
     * Set welcome file name
     * @param <V>
     * @param welcome
     */
    public <V> void setWelcome(V welcome) {
        super.setValue("welcome", welcome);
    }

    /**
     * Get authetication method
     * @param <V>
     * @return
     */
    public <V> V getAuthentication() {
        return super.getValue("authentication");
    }

    /**
     * Set authentication method
     * @param <V>
     * @param authMethod
     */
    public <V> void setAuthentication(V authMethod) {
        super.setValue("authentication", authMethod);
    }

    /**
     * Get session id encription algorithm
     * @param <V>
     * @return
     */
    public <V> V getSessionIDEncryption() {
        return super.getValue("session.encryption");
    }

    /**
     * Set session id encryption algorithm
     * @param <V>
     * @param sessionIDencryption
     */
    public <V> void setSessionIDEncryption(V sessionIDencryption) {
        super.setValue("session.encryption", sessionIDencryption);
    }

    /**
     * Get session id length
     * @param <V>
     * @return 
     */
    public <V> V getSessionIDLength() {
        return super.getValue("session.length");
    }

    /**
     * Set seesion id length
     * @param sessionIdLength
     */
    public <V> void setSessionIDLength(V sessionIdLength) {
        super.setValue("session.length", sessionIdLength);
    }

    /**
     * Get session timeout
     * @param <V>
     * @return
     */
    public <V> V getSessionTimeoutSeconds() {
        return super.getValue("session.timeout-seconds");
    }

    /**
     * Set session timeout
     * @param <V>
     * @param sessionTimeout
     */
    public <V> void setSessionTimeoutSeconds(V sessionTimeout) {
        super.setValue("session.timeout-seconds", sessionTimeout);
    }

    /**
     * Get session filters
     * @param <V>
     * @return
     */
    public <V> V isSessionApply() {
        return super.getValue("session.apply");
    }

    /**
     * Set session filters
     * @param <V>
     * @param applySession
     */
    public <V> void setSessionApply(V applySession) {
        super.setValue("session.apply", applySession);
    }

    /**
     * Get session expire days
     * @param <V>
     * @return
     */
    public <V> V getExpireDays() {
        return super.getValue("session.expire-days");
    }

    /**
     * Set session expire days
     * @param <V>
     * @param expires
     */
    public <V> void setExpireDays(V expires) {
        super.setValue("session.expire-days", expires);
    }

    /**
     * Get max age hours in session
     * @param <V>
     * @return
     */
    public <V> V getMaxAgeHours() {
        return super.getValue("session.max-age-hours");
    }

    /**
     * Set max age hours in session
     * @param <V>
     * @param maxAge
     */
    public <V> void setMaxAgeHours(V maxAge) {
        super.setValue("session.max-age-hours", maxAge);
    }

    /**
     * Get session path( including all subdirectories)
     * @param <V>
     * @return
     */
    public <V> V getPath() {
        return super.getValue("path");
    }

    /**
     * Set session path
     * @param <V>
     * @param path
     */
    public <V> void setPath(V path) {
        super.setValue("path", path);
    }

    /**
     * Get host server status
     * @return
     */
    public STATUS getHostStatus() {        
        return STATUS.valueOf(super.<String> getValue("status"));
    }

    /**
     * Set host server status
     * @param status
     */
    public void setHostStatus(STATUS status) {
        synchronized(super.meta) {
            super.setValue("status", status.name());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////    

    /**
     * Get dynamic class Path
     * @return
     */
    public Path getDynamicClassPaths() {
        return Paths.get(this.<String> getDynamicClasspaths());
    }

    /**
     * Get docroot
     * @return
     */    
    public Path getDocroot() {
        return Paths.get((String)super.getValue("docroot")).normalize().toAbsolutePath();
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
        return getStatic().resolve((String) super.getValue("welcome")).toFile();
    }

    /**
     * Set welcome file
     * @param welcomeFile
     */
    public <V> void setWelcomeFile(File welcomeFile) {
        super.setValue("welcome", welcomeFile.getName());
    }

    /**
     * Get logPath
     * @return
     */
    public Path getLogPath() {
        return getDocroot().resolve((String) super.getValue("logs.path"));
    }

    /**
     * Set logPath
     * @param logPath
     */
    public <V> void setLogPath(Path logPath) {
        super.setValue("logs", logPath.subpath(getDocroot().getNameCount(), logPath.getNameCount()).toString());
    }

    /**
     * Get log level
     * @return
     */
    public List<Level> getLogLevel() {
        return Arrays.asList(super.getValue("logs.level").toString().split(",")).stream().map(l -> Level.toLevel(l.trim())).collect(Collectors.toList());
    }

    /**
     * Set log level
     * @param logLevel
     */
    public <V> void setLogLevel(List<Level> logLevel) {
        super.setValue("logs.level", logLevel.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));
    }

    /**
     * Get resource for host object
     * @return
     */
    public ResourcesModel getResource() {
        return super.getValue("resources.resource");
    }

    /**
     * Set resource for host object
     */
    public void setResource(ResourcesModel resource) {
        super.setValue("resources.resource", resource);
    }

    /**
     * Get InetSocketAddress
     */
    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress((String) getHost(), (int) getPort());
    }

    /**
     * Get Logger
     * @return
     */
    public Logger getLogger() {
        return LoggerFactory.getLogger(getHostId());
    }

    /**
     * For blocking request attack (ip = timestemp)
     */
    private Map<String, Map<String, Long>> requestAttackBlockingMap = new ConcurrentHashMap<>();    

    /**
     * Check too many request attack on short time period.
     * @param connection
     * @return
     * @throws IOException
     */
    public boolean checkRequestAttack(String ip, String url) throws IOException {
        if(requestAttackBlockingMap.containsKey(ip)) {
            Map<String, Long> map = requestAttackBlockingMap.get(ip);
            for(Map.Entry<String, Long> entry : map.entrySet()) {
                String preContext = entry.getKey();
                long preTimestemp = entry.getValue();
                if(url.equals(preContext) && System.currentTimeMillis() - preTimestemp < Context.server().<Integer>getRequestBlockingInterval().longValue()) {
                    requestAttackBlockingMap.remove(ip);
                    throw new LeapException(HTTP.RES429, new Exception("You requested too many on short period!!!  URI: "+url));
                }
            }
            map.put(url, System.currentTimeMillis());
        } else {
            requestAttackBlockingMap.put(ip, new ConcurrentHashMap<String, Long>());
        }
        requestAttackBlockingMap.get(ip).put(url, System.currentTimeMillis());
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
