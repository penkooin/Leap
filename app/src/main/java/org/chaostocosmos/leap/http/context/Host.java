package org.chaostocosmos.leap.http.context;

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

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.Resources;

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
     * 
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
        return super.getValue("dynamic-packages") == null ? (V) new ArrayList<String>() : super.getValue("dynamic-packages");
    }

    /**
     * Set dynamic packages 
     * @param dynamicPackages
     */
    public <V> void setDynamicPackages(V dynamicPackages) {
        super.setValue("dynamic-packages", dynamicPackages);
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
        return Paths.get((String)super.getValue("doc-root")).normalize().toAbsolutePath();
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
        return getDocroot().resolve((String) super.getValue("logs"));
    }

    /**
     * Set logPath
     * @param logPath
     */
    @SuppressWarnings("unchecked")
    public <V> void setLogPath(Path logPath) {
        super.setValue("logs", logPath.subpath(getDocroot().getNameCount(), logPath.getNameCount()).toString());
    }

    /**
     * Get log level
     * @return
     */
    public List<Level> getLogLevel() {
        return Arrays.asList(super.getValue("log-level").toString().split(",")).stream().map(l -> Level.toLevel(l.trim())).collect(Collectors.toList());
    }

    /**
     * Set log level
     * @param logLevel
     */
    public <V> void setLogLevel(List<Level> logLevel) {
        super.setValue("log-level", logLevel.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));
    }

    /**
     * Get resource for host object
     * @return
     */
    public Resources getResource() {
        return super.getValue("resources.resource");
    }

    /**
     * Set resource for host object
     */
    public void setResource(Resources resource) {
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

    @Override
    public String toString() {
        return super.toString();
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
                if(url.equals(preContext) && System.currentTimeMillis() - preTimestemp < Context.getServer().<Integer>getRequestBlockingInterval().longValue()) {
                    requestAttackBlockingMap.remove(ip);
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES429.code(), "You requested too many on short period!!!  URI: "+url);
                }
            }
            map.put(url, System.currentTimeMillis());
        } else {
            requestAttackBlockingMap.put(ip, new ConcurrentHashMap<String, Long>());
        }
        requestAttackBlockingMap.get(ip).put(url, System.currentTimeMillis());
        return true;
    }
}
