package org.chaostocosmos.leap.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.service.datasource.LeapDataSource;

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
     * Get leap version
     * @return
     */
    public <V> V getLeapVersion() {
        return super.getValue("server.version");
    }
    /**
     * Get threadpool core size
     * @return
     */
    public <V> V getThreadPoolCoreSize() {
        return super.getValue("server.threadpool.core");
    }
    /**
     * Get threadpool max size
     * @return
     */
    public <V> V getThreadPoolMaxSize() {
        return super.getValue("server.threadpool.max");
    }
    /**
     * Get threadpool keep-alive 
     * @return
     */
    public <V> V getThreadPoolKeepAlive() {
        return super.getValue("server.threadpool.keep-alive");
    }
    /**
     * Get threadpool queue size
     * @return
     */
    public <V> V getThreadQueueSize() {
        return super.getValue("server.threadpool.queue-size");
    }
    /**
     * Whether supporting monitoring
     * @param <V>
     * @return
     */
    public <V> V isSupportMonitoring() {
        return super.getValue("server.monitoring.support-monitoring");
    }
    /**
     * Get monitoring interval
     * @return
     */
    public <V> V getMonitoringInterval() {
        return super.getValue("server.monitoring.interval");
    }
    /**
     * Get monitoring unit
     * @return
     */
    public <V> V getMonitoringUnit() {
        return super.getValue("server.monitoring.unit");
    }
    /**
     * Get monitoring log path
     * @return
     */
    public <V> V getMonitoringLogs() {
        return super.getValue("server.monitoring.logs");
    }
    /**
     * Get monitoring log level
     * @return
     */
    public <V> V getMonitoringLogLevel() {
        return super.getValue("server.monitoring.log-level");
    }
    /**
     * Get whether supporting Spring-JPA
     * @param <V>
     * @return
     */
    public <V> V isSupportSpringJPA() {
        return super.getValue("server.spring-jpa-support");
    }
    /**
     * Get Spring JPA scan packages
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V getSpringJPAPackage() {
        return super.getValue("server.spring-jpa-packages") == null ? (V) new ArrayList<>() : super.getValue("server.spring-jpa-packages");
    }
    /**
     * Get data sources Map
     * @return
     */
    public List<Map<String, String>> getDataSources() {
        return super.getValue("server.data-source");
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
}
