package org.chaostocosmos.leap.http.context;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.UNIT;

import ch.qos.logback.classic.Level;

/**
 * Server
 * 
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
    public String getLeapVersion() {
        return super.getValue("server.version");
    }
    /**
     * Get connection timeout
     * @return
     */
    public int getConnectionTimeout() {
        return super.getValue("server.connection-timeout");
    }
    /**
     * Get server backlog
     * @return
     */
    public int getBackLog() {
        return super.getValue("server.backlog");
    }
    /**
     * Get client request interval for blocking client that be has malicuous.
     * @return
     */ 
    public long getRequestBlockingInterval() {
        return (long)super.<Integer>getValue("server.request-blocking-interval");
    }
    /**
     * Get upload file buffer flush size
     * @return
     */
    public int getFileBufferSize() {
        return super.getValue("server.file-buffer-size");
    }
    /**
     * Get streaming service buffer size
     * @return
     */
    public int getStreamingBufferSize() {
        return super.getValue("server.streaming-buffer-size");
    }
    /**
     * Get threadpool core size
     * @return
     */
    public int getThreadPoolCoreSize() {
        return super.getValue("server.threadpool.core");
    }
    /**
     * Get threadpool max size
     * @return
     */
    public int getThreadPoolMaxSize() {
        return super.getValue("server.threadpool.max");
    }
    /**
     * Get threadpool keep-alive 
     * @return
     */
    public int getThreadPoolKeepAlive() {
        return super.getValue("server.threadpool.keep-alive");
    }
    /**
     * Get threadpool queue size
     * @return
     */
    public int getThreadQueueSize() {
        return super.getValue("server.threadpool.queue-size");
    }
    /**
     * Get monitoring interval
     * @return
     */
    public long getMonitoringInterval() {
        return (long)super.<Integer>getValue("server.monitoring.interval");
    }
    /**
     * Set monitoring interval
     * @param monitoringInterval
     */
    public void setMonitoringInterval(long monitoringInterval) {
        super.<Long>setValue("server.monitoring.interval", monitoringInterval);
    }
    /**
     * Get monitoring unit
     * @return
     */
    public UNIT getMonitoringUnit() {
        return UNIT.valueOf(super.<String>getValue("server.monitoring.unit"));
    }
    /**
     * Set monitoring unit
     * @param monitoringUnit
     */
    public void setMonitoringUnit(UNIT monitoringUnit) {
        super.<String>setValue("server.monitoring.unit", monitoringUnit.name());
    }
    /**
     * Get monitoring log path
     * @return
     */
    public String getMonitoringLogs() {
        return super.<String>getValue("server.monitoring.logs");
    }
    /**
     * Set monitoring log path
     * @param monitoringLogs
     */
    public void setMonitoringLogs(String monitoringLogs) {
        super.<String>setValue("server.monitoring.logs", monitoringLogs);
    }
    /**
     * Get monitoring log level
     * @return
     */
    public List<Level> getMonitoringLogLevel() {
        return Arrays.asList(super.<String>getValue("server.monitoring.log-level").split(",")).stream().map(l -> Level.toLevel(l.trim())).collect(Collectors.toList());
    }
    /**
     * Set monitoring log level
     * @param monitoringLogLevel
     */
    public void setMonitoringLogLevel(List<Level> monitoringLogLevel) {
        super.<String>setValue("server.monitoring.log-level", monitoringLogLevel.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));
    }
    /**
     * Get Load-Balance redirect Map
     * @return
     */
    public Map<String, Integer> getLoadBalanceRedirects() {
        return super.getValue("server.redirect");
    }
    /**
     * Get SSL protocol
     * @return
     */
    public String getEncryptionMethod() {
        return super.getValue("server.security.encryption");
    }
    /**
     * Get SSL key store Path
     * @return
     */
    public Path getKeyStore() {
        return Paths.get(super.getValue("server.security.keystore"));
    }
    /**
     * Get SSL key store password
     * @return
     */
    public String getPassphrase() {
        return super.getValue("server.security.passphrase");
    }
    /**
     * Get use of Spring JPA feature
     * @return
     */
    public boolean getUseSpringJPA() {
        return super.<Boolean>getValue("server.spring-jpa.use-spring-jpa");
    }
    /**
     * Set use of Spring JPA feature
     * @param useSpringJPA
     */
    public void setUseSpringJPA(boolean useSpringJPA) {
        super.<Boolean>setValue("server.spring-jpa.use-spring-jpa", useSpringJPA);
    }
    /**
     * Get Spring JPA datasource driver class name
     * @return
     */
    public String getDataSourceClassName() {
        return super.<String>getValue("server.spring-jpa.datasource.driver-class");
    }
    /**
     * Set Spring JPA datasource driver class name
     * @param driverClassName
     */
    public void setDataSourceClassName(String driverClassName) {
        super.<String>setValue("server.spring-jpa.datasource.driver-class", driverClassName);
    }
    /**
     * Get Spring JPA datasource url
     * @return
     */
    public String getDataSourceURL() {
        return super.<String>getValue("server.spring-jpa.datasource.url");
    }
    /**
     * Set Spring JPA datasource url
     * @param url
     */
    public void setDatasourceURL(String url) {
        super.<String>setValue("server.spring-jpa.datasource.url", url);
    }
    /**
     * Get Spring JPA datasource schema
     * @return
     */
    public String getDataSourceSchema() {
        return super.<String>getValue("server.spring-jpa.datasource.schema");
    }
    /**
     * Set Spring JPA dataSource schema
     * @param schema
     */
    public void setDataSourceSchema(String schema) {
        super.<String>setValue("server.spring-jpa.datasource.schema", schema);
    }
    /**
     * Get Spring JPA datasource user
     * @return
     */
    public String getDataSourceUser() {
        return super.<String>getValue("server.spring-jpa.datasource.user");
    }
    /**
     * Set Spring JPA datasource user
     * @param user
     */
    public void setDataSourceUser(String user) {
        super.<String>setValue("server.spring-jpa.datasource.user", user);
    }
    /**
     * Get Spring JPA datasource password
     * @return
     */
    public String getDataSourcePassword() {
        return super.<String>getValue("server.spring-jpa.datasource.password");
    }
    /**
     * Set Spring JPA datasource password
     * @param password
     */
    public void setDataSourcePassword(String password) {
        super.<String>setValue("server.spring-jpa.datasource.password", password);
    }
    /**
     * Get Spring JPA scan packages
     * @return
     */
    public List<String> getSpringJPAPackage() {
        return super.<List<String>>getValue("server.spring-jpa.spring-jpa-packages") == null ? new ArrayList<>() : super.<List<String>>getValue("server.spring-jpa.spring-jpa-packages");
    }
    /**
     * Set Spring JPA scan packages
     * @param springJpaPackages
     */
    public void setSpringJPAPackage(List<String> springJpaPackages) {
        super.<List<String>>setValue("server.spring-jpa.spring-jpa-packages", springJpaPackages);
    }
}
