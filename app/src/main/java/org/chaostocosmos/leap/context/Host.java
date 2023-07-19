package org.chaostocosmos.leap.context;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.Filtering;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.enums.AUTH;
import org.chaostocosmos.leap.enums.DATASOURCE;
import org.chaostocosmos.leap.enums.STATUS;
import org.chaostocosmos.leap.manager.ResourceManager;
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
     * Get locale
     * @param <V>
     * @return
     */
    public <V> V getLocale() {
        return super.getValue("locale");
    }
    /**
     * Get specified web protocol
     * @return
     */
    public <V> V getProtocol() {
        return super.getValue("protocol");
    }
    /**
     * Get protocol version
     * @param <V>
     * @return
     */
    public <V> V getProtocolVersion() {
        return super.getValue("protocol-version");
    }
    /**
     * Get charset of the host
     * @return
     */
    public <V> V charset() {
        return super.getValue("charset");
    }
    /**
     * Get host name
     * @return
     */
    public <V> V getHost() {
        return super.getValue("host");
    }
    /**
     * Get port;
     * @return
     */
    public <V> V getPort() {
        return super.getValue("port");
    }
    /**
     * Get connection timeout
     * @return
     */
    public <V> V getConnectionTimeout() {
        return super.getValue("connection-timeout");
    }
    /**
     * Get server backlog
     * @return
     */
    public <V> V getBackLog() {
        return super.getValue("backlog");
    }
    /**
     * Get client request interval for blocking client that be has malicuous.
     * @return
     */ 
    public <V> V getRequestBlockingInterval() {
        return super.getValue("request-blocking-interval");
    }
    /**
     * Get upload file buffer flush size
     * @return
     */
    public <V> V getFileBufferSize() {
        return super.getValue("file-buffer-size");
    }    
    /**
     * Get users
     * @return
     */
    public <V> V getUsers() {        
        return super.getValue("users");
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
     * Get dynamic packages list
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V getDynamicPackages() {
        return super.getValue("dynamic-classpath") == null ? (V) new ArrayList<String>() : super.getValue("dynamic-classpath");
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
     * Get forbidden Filtering object
     * @return
     */
    public Filtering getForbiddenFiltering() {
        return new Filtering(getForbiddenFilters());
    }
    /**
     * Get whether show error-details content to client ( true or false)
     */
    public <V> V getLogsDetails() {
        return super.getValue("logs.details");
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
     * Get authetication method
     * @param <V>
     * @return
     */
    public <V> V getAuthentication() {
        return super.getValue("authentication");
    }
    /**
     * Whether authorization
     * @return
     */
    public boolean isAuthentication() {
        return AUTH.valueOf(this.<String>getAuthentication()) == AUTH.NONE ? false : true;
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
     * Get session id length
     * @param <V>
     * @return 
     */
    public <V> V getSessionIDLength() {
        return super.getValue("session.length");
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
     * Get session filters
     * @param <V>
     * @return
     */
    public <V> V isSessionApply() {
        return super.getValue("session.apply");
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
     * Get max age hours in session
     * @param <V>
     * @return
     */
    public <V> V getMaxAgeHours() {
        return super.getValue("session.max-age-hours");
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
     * Get host server status
     * @return
     */
    public STATUS getHostStatus() {        
        return super.<STATUS> getValue("status");
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
     * Get view content path
     * @return
     */
    public Path getView() {
        return getDocroot().resolve("views");
    }
    /**
     * Get services and classes path
     */
    public Path getClasses() {
        return getDocroot().resolve("classes");
    }
    /**
     * get templates path
     */
    public Path getTemplates() {
        return getDocroot().resolve("templates");
    }
    /**
     * Get welcome file
     * @return
     */
    public File getWelcomeFile() {
        return getTemplates().resolve((String) super.getValue("welcome")).toFile();
    }
    /**
     * Get logPath
     * @return
     */
    public Path getLogPath() {
        return getDocroot().resolve((String) super.getValue("logs.path"));
    }
    /**
     * Get log level
     * @return
     */
    public List<Level> getLogLevel() {
        return Arrays.asList(super.getValue("logs.level").toString().split(",")).stream().map(l -> Level.toLevel(l.trim())).collect(Collectors.toList());
    }
    /**
     * Get resource for host object
     * @return
     */
    public ResourcesModel getResource() {
        ResourcesModel model = ResourceManager.get(getHostId());
        return model;
    }
    /**
     * Get InetSocketAddress
     */
    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress((String) getHost(), (int) getPort());
    }
    /**
     * Get Load-Balance redirect Map
     * @return
     */
    public <V> V getTrafficRedirects() {
        return super.getValue("traffic-redirect");
    }
    /**
     * Get SSL protocol
     * @return
     */
    public <V> V getEncryptionMethod() {
        return super.getValue("security.encryption");
    }
    /**
     * Get SSL key store Path
     * @return
     */
    public <V> V getKeyStore() {
        return super.getValue("security.keystore");
    }
    /**
     * Get SSL key store password
     * @return
     */
    public <V> V getPassphrase() {
        return super.getValue("security.passphrase");
    }
    /**
     * Set host status
     * @param status
     */
    public void setHostStatus(STATUS status) {
        super.setValue("status", status);
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
