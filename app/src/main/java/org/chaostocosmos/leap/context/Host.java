package org.chaostocosmos.leap.context;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.LeapApp;
import org.chaostocosmos.leap.common.data.Filtering;
import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.log.LEVEL;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.enums.AUTH;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.STATUS;
import org.chaostocosmos.leap.resource.ResourceProvider;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;

import com.google.gson.Gson;

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
     * Get server name
     * @return
     */
    public String getId() {
        return super.<String> getValue("id");
    }

    /**
     * Get locale
     * @param <V>
     * @return
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Get time zone String
     * @return
     */
    public String getTimeZone() {
        return super.<String> getValue("timezone");
    }

    /**
     * Get ZoneId
     * @return
     */
    public ZoneId getZoneId() {
        return ZoneId.of(super.<String> getValue("timezone"));
    }

    /**
     * Get ZoneDateTime
     * @return
     */
    public ZonedDateTime getNowZoneDateTime() {
        return ZonedDateTime.now(getZoneId());
    }

    /**
     * Get current time formatted
     * @param pattern
     * @return
     */
    public String getCurrentDateTime(String pattern) {
        return getNowZoneDateTime().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Get specified web protocol
     * @return
     */
    public PROTOCOL getProtocol() {        
        return PROTOCOL.valueOf(super.<String> getValue("protocol"));
    }

    /**
     * Get protocol version
     * @return
     */
    public String getProtocolVersion() {
        return super.<String> getValue("protocol-version");
    }

    /**
     * Get charset of the host
     * @return
     */
    public String charset() {
        return super.getValue("charset");
    }

    /**
     * Get host name
     * @return
     */
    public String getHost() {
        return super.getValue("host");
    }

    /**
     * Get port;
     * @return
     */
    public int getPort() {
        return super.getValue("port");
    }

    /**
     * Get connection timeout
     * @return
     */
    public int getConnectionTimeout() {
        return super.getValue("connection-timeout");
    }

    /**
     * Get server backlog
     * @return
     */
    public int getBackLog() {
        return super.getValue("backlog");
    }

    /**
     * Get client request interval for blocking client that be has malicuous.
     * @return
     */ 
    public int getRequestBlockingInterval() {
        return super.<Integer> getValue("request-blocking-interval");
    }

    /**
     * Get limitation of response body
     * @return
     */
    public int getResponseLimitBytes() {
        return super.<Integer> getValue("reponse-limit-bytes");
    }

    /**
     * Get users
     * @return
     */
    public List<Map<String, Object>> getUsers() {        
        return super.getValue("users");
    }

    /**
     * Get access filters
     * @return
     */
    public List<String> getAccessFilters() {
        return super.<List<String>>getValue("context-filters.access-filters");
    }

    /**
     * Get allowed resource filters
     * @return
     */
    public Filtering getAccessFiltering() {
        return new Filtering(super.getValue("context-filters.access-filters"));
    }

    /**
     * Get forbidden filters
     * @return
     */
    public List<String> getForbiddenFilters() {
        return super.getValue("context-filters.forbidden-filters");
    }

    /**
     * Get forbidden Filtering object
     * @return
     */
    public Filtering getForbiddenFiltering() {
        return new Filtering(getForbiddenFilters());
    }

    /**
     * Get IP allowed filters 
     * @return
     */
    public List<String> getIpAllowedFilters() {
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
    public List<String> getIpForbbidenFilters() {
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
     * Get whether show error-details content to client ( true or false)
     * @return
     */
    public boolean getLogsDetails() {
        return super.getValue("logs.details");
    }

    /**
     * Get index file name
     * @return
     */
    public String getIndexPage() {
        return super.getValue("index-page");
    }

    /**
     * Get error page
     * @return
     */
    public String getErrorPage() {
        return super.getValue("error-page");
    }

    /**
     * Get authetication method
     * @return
     */
    public AUTH getAuthentication() {
        return AUTH.valueOf(super.getValue("authentication"));
    }

    /**
     * Whether authorization
     * @return
     */
    public boolean isAuthentication() {
        return getAuthentication() == AUTH.NONE ? false : true;
    }

    /**
     * Get session id encription algorithm
     * @return
     */
    public String getSessionIDEncryption() {
        return super.getValue("session.encryption");
    }

    /**
     * Get session id length
     * @return 
     */
    public int getSessionIDLength() {
        return super.getValue("session.length");
    }

    /**
     * Get session timeout
     * @return
     */
    public int getSessionTimeoutSeconds() {
        return super.getValue("session.timeout-seconds");
    }

    /**
     * Get session filters
     * @return
     */
    public boolean isSessionApply() {
        return super.getValue("session.apply");
    }

    /**
     * Get session expire days
     * @return
     */
    public int getExpireDays() {
        return super.getValue("session.expire-days");
    }

    /**
     * Get max age hours in session
     * @return
     */
    public int getMaxAgeHours() {
        return super.getValue("session.max-age-hours");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////     

    /**
     * Get session path( including all subdirectories)
     * @return
     */
    public String getPath() {
        return super.getValue("path");
    }

    /**
     * Get host server status
     * @return
     */
    public STATUS getHostStatus() {        
        return STATUS.valueOf(super. getValue("status"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////     

    /**
     * Get dynamic class Path
     * @return
     */
    public List<Path> getDynamicClassPaths() {
        return super.<List<String>> getValue("dynamic-classpath").stream().map(d -> Paths.get(d)).collect(Collectors.toList());
    }

    /**
     * Get dynamic packages list
     * @return
     */
    public List<String> getDynamicPackages() {
        return super.getValue("dynamic-classpath") == null ? new ArrayList<String>() : super.<List<String>> getValue("dynamic-classpath");
    }

    /**
     * Get dynamic package Filtering object
     * @return
     */
    public Filtering getDynamicPackageFiltering() {
        return new Filtering(getDynamicPackages());
    }

    /**
     * Get home path
     * @return
     */
    public Path getHomePath() {
        return Context.get().server().getHosts().get(getId()).normalize().toAbsolutePath();
    }

    /**
     * Get docroot
     * @return
     */    
    public Path getDocroot() {
        return getHomePath().resolve("static");
    }

    /**
     * Get web-inf path
     * @return
     */
    public Path getWebInf() {
        return getHomePath().resolve("WEB-INF");
    }

    /**
     * Get static path
     * @return
     */
    public Path getStatic() {
        return getDocroot();
    }

    /**
     * Get view content path
     * @return
     */
    public Path getViews() {
        return getWebInf().resolve("views");
    }

    /**
     * Get services and classes path
     * @return
     */
    public Path getClasses() {
        return getWebInf().resolve("classes");
    }

    /**
     * Get templates path
     */
    public Path getTemplates() {
        return getHomePath().resolve("templates");
    }

    /**
     * Get index file
     * @return
     */
    public File getIndexFile() {
        return getDocroot().resolve(super.<String> getValue("index-page")).toFile();
    }

    /** 
     * Get logPath
     * @return
     */
    public Path getLogPath() {
        return getHomePath().resolve(super.<String> getValue("logs.path"));
    }

    /**
     * Get log level
     * @return
     */
    public LEVEL getLogLevel() {
        return LEVEL.valueOf(super.getValue("logs.level"));
    }

    /**
     * Get resource for host object
     * @return
     */
    public ResourcesWatcherModel getResource() {
        return LeapApp.getResourceProvider().get(getId());
    }

    /**
     * Get InetSocketAddress
     */
    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress(getHost(), (int) getPort());
    }

    /**
     * Get SSL protocol
     * @return
     */
    public String getEncryptionMethod() {
        return super.getValue("security.encryption");
    }

    /**
     * Get SSL key store Path
     * @return
     */
    public String getKeyStore() {
        return super.<String> getValue("security.keystore");
    }

    /**
     * Get SSL key store password
     * @return
     */
    public String getPassphrase() {
        return super.<String> getValue("security.passphrase");
    }

    /**
     * Set host status
     * @param status
     */
    public void setHostStatus(STATUS status) {
        super.setValue("status", status.name());
    }    

    /**
     * Get Logger
     * @return
     */
    public Logger getLogger() {        
        return LoggerFactory.getLogger(getId());
    }

    /**
     * Make resources Json
     * @param dirRoot
     * @return
     */
    public String buildDirectoryJson(String dirRoot) {
        String path = dirRoot.charAt(dirRoot.length() - 1) == '/' ? dirRoot.substring(0, dirRoot.lastIndexOf('/')) : dirRoot;
        final String path1 = path.equals("") ? "/" : path;
        File[] fs = getStatic().resolve(path1.substring(1)).toFile().listFiles();
        
        List<File> resourceInfos = Arrays.asList(getStatic().resolve(path1.substring(1)).toFile().listFiles())
                                         .stream()
                                         .sorted(Comparator.comparing(f -> f.isDirectory() ? -1 : 1))
                                         .filter(f -> getForbiddenFiltering().exclude(f.getName()))
                                         .collect(Collectors.toList());
        int pathCnt = path.length() - path.replace("/", "").length();
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("parent", pathCnt > 1 ? path.substring(0, path.lastIndexOf("/")): "/directory?reqPath=%2F");
        params.put("host", getHost()+":"+getPort()+"/");
        params.put("elements", resourceInfos.stream().map(f -> {
                        String img = f.isFile() ? "/img/file.png" : "/img/dir.png";
                        String file = f.isFile() ? f.getName() : f.getName()+"/";
                        String uri = path+"/"+file;
                        long lastModified = f.lastModified();
                        String size = f.isFile() ? SIZE.MB.get(f.length())+" "+SIZE.MB.name() : "-";
                        String inMemory = f.isDirectory() ? "-" : getResource().isInMemory(f.toPath()) ? "In-Memory resource" : "File resource";
                        return Map.of("img", img, "file", file, "uri", uri, "lastModified", new Date(lastModified).toString(), "size", size, "desc", inMemory);
        }).collect(Collectors.toList()));
        return new Gson().toJson(params);
    }
    
    /**
     * To string lteral of this object
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
