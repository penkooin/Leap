package org.chaostocosmos.leap.context;

import java.util.ArrayList;

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
     * Get connection timeout
     * @return
     */
    public <V> V getConnectionTimeout() {
        return super.getValue("server.connection-timeout");
    }
    /**
     * Get server backlog
     * @return
     */
    public <V> V getBackLog() {
        return super.getValue("server.backlog");
    }
    /**
     * Get client request interval for blocking client that be has malicuous.
     * @return
     */ 
    public <V> V getRequestBlockingInterval() {
        return super.getValue("server.request-blocking-interval");
    }
    /**
     * Get upload file buffer flush size
     * @return
     */
    public <V> V getFileBufferSize() {
        return super.getValue("server.file-buffer-size");
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
     * Set whether supporting monitoring
     * @param isSupportMonitoring
     */
    public <V> void setSupportMonitoring(V isSupportMonitoring) {
        super.setValue("server.monitoring.support-monitoring", isSupportMonitoring);
    }
    /**
     * Get monitoring interval
     * @return
     */
    public <V> V getMonitoringInterval() {
        return super.getValue("server.monitoring.interval");
    }
    /**
     * Set monitoring interval
     * @param monitoringInterval
     */
    public <V> void setMonitoringInterval(V monitoringInterval) {
        super.setValue("server.monitoring.interval", monitoringInterval);
    }
    /**
     * Get monitoring unit
     * @return
     */
    public <V> V getMonitoringUnit() {
        return super.getValue("server.monitoring.unit");
    }
    /**
     * Set monitoring unit
     * @param monitoringUnit
     */
    public <V> void setMonitoringUnit(V monitoringUnit) {
        super.setValue("server.monitoring.unit", monitoringUnit);
    }
    /**
     * Get monitoring log path
     * @return
     */
    public <V> V getMonitoringLogs() {
        return super.getValue("server.monitoring.logs");
    }
    /**
     * Set monitoring log path
     * @param monitoringLogs
     */
    public <V> void setMonitoringLogs(V monitoringLogs) {
        super.setValue("server.monitoring.logs", monitoringLogs);
    }
    /**
     * Get monitoring log level
     * @return
     */
    public <V> V getMonitoringLogLevel() {
        return super.getValue("server.monitoring.log-level");
    }
    /**
     * Set monitoring log level
     * @param monitoringLogLevel
     */
    public <V> void setMonitoringLogLevel(V monitoringLogLevel) {
        super.setValue("server.monitoring.log-level", monitoringLogLevel);
    }
    /**
     * Get Load-Balance redirect Map
     * @return
     */
    public <V> V getLoadBalanceRedirects() {
        return super.getValue("server.redirect");
    }
    /**
     * Get SSL protocol
     * @return
     */
    public <V> V getEncryptionMethod() {
        return super.getValue("server.security.encryption");
    }
    /**
     * Get SSL key store Path
     * @return
     */
    public <V> V getKeyStore() {
        return super.getValue("server.security.keystore");
    }
    /**
     * Get SSL key store password
     * @return
     */
    public <V> V getPassphrase() {
        return super.getValue("server.security.passphrase");
    }
    /**
     * Get whether supporting Spring-JPA
     * @param <V>
     * @return
     */
    public <V> V isSupportSpringJPA() {
        return super.getValue("server.spring-jpa.support-spring-jpa");
    }
    /**
     * Set whether supporting Spring-JPA
     * @param <V>
     * @param supportSpringJPA
     */
    public <V> void setSupportSpringJPA(V supportSpringJPA) {
        super.setValue("server.spring-jpa.supoort-spring-jpa", supportSpringJPA);
    }
    /**
     * Get use of Spring JPA feature
     * @return
     */
    public <V> V getUseSpringJPA() {
        return super.getValue("server.spring-jpa.use-spring-jpa");
    }
    /**
     * Set use of Spring JPA feature
     * @param useSpringJPA
     */
    public <V> void setUseSpringJPA(V useSpringJPA) {
        super.setValue("server.spring-jpa.use-spring-jpa", useSpringJPA);
    }
    /**
     * Get Spring JPA datasource driver class name
     * @return
     */
    public <V> V getDataSourceClassName() {
        return super.getValue("server.spring-jpa.datasource.driver-class");
    }
    /**
     * Set Spring JPA datasource driver class name
     * @param driverClassName
     */
    public <V> void setDataSourceClassName(V driverClassName) {
        super.setValue("server.spring-jpa.datasource.driver-class", driverClassName);
    }
    /**
     * Get Spring JPA datasource url
     * @return
     */
    public <V> V getDataSourceURL() {
        return super.getValue("server.spring-jpa.datasource.url");
    }
    /**
     * Set Spring JPA datasource url
     * @param url
     */
    public <V> void setDatasourceURL(V url) {
        super.setValue("server.spring-jpa.datasource.url", url);
    }
    /**
     * Get Spring JPA datasource schema
     * @return
     */
    public <V> V getDataSourceSchema() {
        return super.getValue("server.spring-jpa.datasource.schema");
    }
    /**
     * Set Spring JPA dataSource schema
     * @param schema
     */
    public <V> void setDataSourceSchema(V schema) {
        super.setValue("server.spring-jpa.datasource.schema", schema);
    }
    /**
     * Get Spring JPA datasource user
     * @return
     */
    public <V> V getDataSourceUser() {
        return super.getValue("server.spring-jpa.datasource.user");
    }
    /**
     * Set Spring JPA datasource user
     * @param user
     */
    public <V> void setDataSourceUser(V user) {
        super.setValue("server.spring-jpa.datasource.user", user);
    }
    /**
     * Get Spring JPA datasource password
     * @return
     */
    public <V> V getDataSourcePassword() {
        return super.getValue("server.spring-jpa.datasource.password");
    }
    /**
     * Set Spring JPA datasource password
     * @param password
     */
    public <V> void setDataSourcePassword(V password) {
        super.setValue("server.spring-jpa.datasource.password", password);
    }
    /**
     * Get Spring JPA scan packages
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V getSpringJPAPackage() {
        return super.getValue("server.spring-jpa.spring-jpa-packages") == null ? (V) new ArrayList<>() : super.getValue("server.spring-jpa.spring-jpa-packages");
    }
    /**
     * Set Spring JPA scan packages
     * @param springJpaPackages
     */
    public <V> void setSpringJPAPackage(V springJpaPackages) {
        super.setValue("server.spring-jpa.spring-jpa-packages", springJpaPackages);
    }
}
