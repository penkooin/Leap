package org.chaostocosmos.leap.context;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.log.LEVEL;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.spring.datasource.LeapDataSource;

/**
 * Server configuration object
 * 
 * @author 9ins
 */
public class Server <T> extends Metadata <T> {

    /**
     * Constructor
     * @param serverConfigMap
     */
    public Server(T serverConfigMap) {
        super(serverConfigMap);
    }

    /**
     * Get server id
     * @return
     */
    public String getId() {
        return super.getValue("server.id");
    }

    /**
     * Get host name & path object
     * @return
     */
    public Map<String, Path> getHosts() {
        return super.<Map<String, String>> getValue("server.hosts")        
                                            .entrySet()
                                            .stream()
                                            .map(e -> new Object[]{e.getKey(), Paths.get(e.getValue())})
                                            .collect(Collectors.toMap(k -> (String)k[0], v -> (Path)v[1]));
    }

    /**
     * Get threadpool core size
     * @return
     */
    public int getThreadPoolCoreSize() {
        return super.<Integer> getValue("server.threadpool.core");
    }

    /**
     * Get threadpool max size
     * @return
     */
    public int getThreadPoolMaxSize() {
        return super.<Integer> getValue("server.threadpool.max");
    }

    /**
     * Get threadpool keep-alive 
     * @return
     */
    public int getThreadPoolKeepAlive() {
        return super.<Integer> getValue("server.threadpool.keep-alive");
    }

    /**
     * Get threadpool queue size
     * @return
     */
    public int getThreadQueueSize() {
        return super.<Integer> getValue("server.threadpool.queue-size");
    }

    /**
     * Get monitor context
     * @return
     */
    public String getMonitorContext() {
        return super.<String> getValue("server.monitor.context-path");
    }

    /**
     * Whether supporting monitoring
     * @return
     */
    public boolean isSupportMonitoring() {
        return super.getValue("server.monitor.support-monitoring");
    }

    /**
     * Get monitoring interval
     * @return
     */
    public int getMonitoringInterval() {
        return super.getValue("server.monitor.probing-interval");
    }

    /**
     * Get monitoring image path
     * @return
     */
    public Path getMonitoringImagePath() {
        return Paths.get(super.<String> getValue("server.monitor.image-path"));
    }

    /**
     * Get monitoring log path
     * @return
     */
    public Path getLogs() {
        return Context.get().getHome().resolve(super.<String> getValue("server.logs.path"));
    }

    /**
     * Get monitoring log level
     * @return
     */
    public LEVEL getLogsLevel() {
        return LEVEL.valueOf(super.getValue("server.logs.level"));
    }

    /**
     * Get whether supporting Spring-JPA
     * @return
     */
    public boolean isSupportSpringJPA() {
        return super.<Boolean> getValue("server.spring-jpa-support");
    }

    /**
     * Get Spring JPA scan packages
     * @return
     */
    public List<String> getSpringJPAPackages() {
        return super.getValue("server.spring-jpa-packages") == null ? new ArrayList<String>() : super.<List<String>> getValue("server.spring-jpa-packages");
    }

    /**
     * Get data sources Map
     * @return
     */
    public List<Map<String, String>> getDataSources() {
        return super.<List<Map<String, String>>> getValue("server.data-source");
    }

    /**
     * Get Leap data source list
     * @return
     */
    public List<LeapDataSource> getLeapDataSources() {
        return getDataSources().stream().map(m -> new LeapDataSource(m)).collect(Collectors.toList());
    }

    /**
     * Get Leap data source object
     * @param dataSourceId
     * @return
     */
    public LeapDataSource getLeapDataSource(String dataSourceId) {
        return new LeapDataSource(getDataSource(dataSourceId));
    }

    /**
     * Get data source
     * @param dataSourceId
     * @return
     */
    public Map<String, String> getDataSource(String dataSourceId) {
        return getDataSources().stream().filter(m -> m.get("id").equals(dataSourceId)).findFirst().orElseThrow();
    }

    /**
     * Get Spring JPA datasource driver class name
     * @param dataSourceId
     * @return
     */
    public String getDataSourceClassName(String dataSourceId) {
        return getDataSource(dataSourceId).get("driver-class");
    }

    /**
     * Get Spring JPA datasource url
     * @param dataSourceId
     * @return
     */
    public String getDataSourceURL(String dataSourceId) {
        return getDataSource(dataSourceId).get("url");
    }

    /**
     * Get Spring JPA datasource schema
     * @param dataSourceId
     * @return
     */
    public String getDataSourceSchema(String dataSourceId) {
        return getDataSource(dataSourceId).get("schema");
    }

    /**
     * Get Spring JPA datasource user
     * @param dataSourceId
     * @return
     */
    public String getDataSourceUser(String dataSourceId) {
        return getDataSource(dataSourceId).get("user");
    }

    /**
     * Get Spring JPA datasource password
     * @param dataSourceId
     * @return
     */
    public String getDataSourcePassword(String dataSourceId) {
        return getDataSource(dataSourceId).get("password");
    }

    /**
     * Get Load-Balance redirect Map
     * @return
     */
    public Map<String, Integer> getRedirectLBRatio() {
        return super.getValue("server.redirect-lb");
    }

    /**
     * Get Logger
     * @return
     */
    public Logger getLogger() {        
        return LoggerFactory.getLogger(getId());
    }
}
