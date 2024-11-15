package org.chaostocosmos.leap.context;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
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

import org.chaostocosmos.leap.common.data.Filtering;
import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.log.LEVEL;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.enums.AUTH;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.LEAP_STATUS;
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
     * Get host ID
     * @return
     */
    public String getId() {
        return super.<String> getValue("id");
    }

    /**
     * Get host name
     * @return
     */
    public String getHost() {
        return super.<String> getValue("global.host");
    }

    /**
     * Get host port
     * @return
     */
    public int getPort() {
        return super.<Integer> getValue("global.port");
    }

    /**
     * Get locale
     * @param <V>
     * @return
     */
    public Locale getLocale() {
        return Locale.forLanguageTag("global.locale");
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
        return PROTOCOL.valueOf(super.<String> getValue("global.protocol"));
    }

    /**
     * Get charset of the host
     * @return
     */
    public Charset charset() {
        return Charset.forName(super.getValue("global.charset"));
    }

    /**
     * Get allowed resource filters
     * @return
     */
    public Filtering getAllowedPathFiltering() {
        return new Filtering(super.<List<String>> getValue("security.context-filters.allowed"));
    }

    /**
     * Get forbidden Filtering
     * @return
     */
    public Filtering getIpForbiddenFiltering() {
        return new Filtering(super.<List<String>> getValue("security.ip-filters.forbidden"));
    }

    /**
     * Get authetication method
     * @return
     */
    public AUTH getAuthentication() {
        return AUTH.valueOf(super.getValue("security.authentication"));
    }

    /**
     * Whether authorization
     * @return
     */
    public boolean isAuthentication() {
        return getAuthentication() != AUTH.NONE ? true : false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////     

    /**
     * Get session path( including all subdirectories)
     * @return
     */
    public String getPath() {
        return super.getValue("path");
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
        String id = getId();
        return Context.get().server().getHosts().get(id).normalize().toAbsolutePath();
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
        return getDocroot().resolve(super.<String> getValue("global.index-page")).toFile();
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
        return ResourceProvider.get().get(getDocroot());
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
        //File[] fs = getStatic().resolve(path1.substring(1)).toFile().listFiles();
        
        List<File> resourceInfos = Arrays.asList(getStatic().resolve(path1.substring(1)).toFile().listFiles())
                                         .stream()
                                         .sorted(Comparator.comparing(f -> f.isDirectory() ? -1 : 1))
                                         .filter(f -> getAllowedPathFiltering().include(f.getName()))
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
