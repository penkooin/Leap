package org.chaostocosmos.leap.http.context;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
}
