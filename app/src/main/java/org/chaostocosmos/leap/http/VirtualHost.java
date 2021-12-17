package org.chaostocosmos.leap.http;

import java.net.InetSocketAddress;
import java.nio.file.Path;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Host object
 */
public class VirtualHost {
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
     * Document root
     */
    private Path docroot;

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Logging level
     */
    private Level logLevel;

    /**
     * Constructor
     * @param serverName
     * @param host
     * @param port
     * @param docroot
     * @param logger
     * @param logLevel
     */
    public VirtualHost(String serverName, String host, int port, Path docroot, Logger logger, Level logLevel)  {
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.docroot = docroot;
        this.logger = logger;
        this.logLevel = logLevel;
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
     * Get logger
     * @return
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Set logger
     * @param logger
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Get log level
     * @return
     */
    public Level getLogLevel() {
        return this.logLevel;
    }

    /**
     * Set log level
     * @param logLevel
     */
    public void setLogLevel(Level logLevel) {
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
            " serverName='" + getServerName() + "'" +
            ", host='" + getHost() + "'" +
            ", port='" + getPort() + "'" +
            ", docroot='" + getDocroot() + "'" +
            ", logger='" + getLogger() + "'" +
            ", logLevel='" + getLogLevel() + "'" +
            "}";
    }    
}
