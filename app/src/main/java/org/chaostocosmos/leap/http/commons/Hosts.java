package org.chaostocosmos.leap.http.commons;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;

import ch.qos.logback.classic.Level;

/**
 * Host object
 */
public class Hosts {
    /**
     * Whether main host
     */
    private boolean isDefaultHost;

    /**
     * Server name
     */
    private String serverName;

    /**
     * Host name
     */
    private String host;    

    /**
     * port
     */
    private int port;

    /**
     * Dynamic class path list
     */
    private List<Path> dynamicClasspaths;

    /**
     * Document root
     */
    private Path docroot;

    /**
     * log path
     */
    private String logPath;

    /**
     * Logging level
     */
    private List<Level> logLevel;

    /**
     * Constructor
     * @param isDefaultHost
     * @param serverName
     * @param host
     * @param port
     * @param dynamicClasspaths
     * @param docroot
     * @param logPath
     * @param logLevel
     */
    public Hosts(boolean isDefaultHost, String serverName, String host, int port, List<Path> dynamicClaspaths, Path docroot, String logPath, List<Level> logLevel)  {
        this.isDefaultHost = isDefaultHost;
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.dynamicClasspaths = dynamicClaspaths;
        this.docroot = docroot.normalize();
        this.logPath = logPath;
        this.logLevel = logLevel;
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
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Get server name
     * @param serverName
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Get host name
     * @return
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Set host name
     * @param hostName
     */
    public void setHost(String hostName) {
        this.host = hostName;
    }

    /**
     * Get port;
     * @return
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Set port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get dynamic class path list
     * @return
     */
    public List<Path> getDynamicClasspaths() {
        return this.dynamicClasspaths;
    }

    /**
     * Set synamic class path list
     * @param dynamicClasspaths
     */
    public void setDynamicClasspaths(List<Path> dynamicClasspaths) {
        this.dynamicClasspaths = dynamicClasspaths;
    }

    /**
     * Get docroot
     * @return
     */    
    public Path getDocroot() {
        return this.docroot;
    }

    /**
     * Set docroot
     * @param docroot
     */
    public void setDocroot(Path docroot) {
        this.docroot = docroot;
    }

    /**
     * Get logPath
     * @return
     */
    public String getLogPath() {
        return this.logPath;
    }

    /**
     * Set logPath
     * @param logPath
     */
    public void setLogger(String logPath) {
        this.logPath = logPath;
    }

    /**
     * Get log level
     * @return
     */
    public List<Level> getLogLevel() {
        return this.logLevel;
    }

    /**
     * Set log level
     * @param logLevel
     */
    public void setLogLevel(List<Level> logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Get InetSocketAddress
     */
    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    @Override
    public String toString() {
        return "{" +
            " isDefaultHost='" + isDefaultHost() + "'" +
            ", serverName='" + getServerName() + "'" +
            ", host='" + getHost() + "'" +
            ", port='" + getPort() + "'" +
            ", dynamicClasspaths='" + getDynamicClasspaths() + "'" +
            ", docroot='" + getDocroot() + "'" +
            ", logPath='" + getLogPath() + "'" +
            ", logLevel='" + getLogLevel() + "'" +
            "}";
    }
}
