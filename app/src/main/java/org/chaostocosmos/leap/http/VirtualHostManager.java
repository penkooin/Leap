package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual host manager object
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class VirtualHostManager {
    /**
     * Logger
     */
    Logger logger = LoggerFactory.getLogger(VirtualHostManager.class);

    /**
     * Context
     */
    private static Context context = Context.getInstance();

    /**
     * Virtual hosts
     */
    private List<VirtualHost> virtualHosts = new ArrayList<>();

    /**
     * VirtualHostManager
     */
    private static VirtualHostManager virtualHostManager = null;

    /**
     * Constructor
     * 
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    private VirtualHostManager() throws WASException, IOException, URISyntaxException {        
        List<Map<String, Object>> vList = (List<Map<String, Object>>)context.getConfigValue("server.virtual-host");
        for(Map<String, Object> m : vList) {
            String host = m.get("host").toString();
            int port = Integer.parseInt(m.get("port").toString());
            InetSocketAddress addr = new InetSocketAddress(host, port);
            Path vDocroot = Paths.get(m.get("doc-root").toString());

            //build environment each virtual host 
            ResourceHelper.buildEnv(vDocroot);

            Path logPath = Paths.get(m.get("logPath").toString());
            String logLevel = m.get("logLevel").toString();
            this.virtualHosts.add(new VirtualHost(addr, vDocroot, logPath, logLevel));
        }
    }

    /**
     * Get VirtualHostManager instance
     * 
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */
    public static VirtualHostManager getInstance() throws WASException, IOException, URISyntaxException {
        if(virtualHostManager == null) {
            virtualHostManager = new VirtualHostManager();
        }
        return virtualHostManager;
    }

    /**
     * Get virtual host
     * @param serverName
     * @return
     */
    public VirtualHost getVirtualHost(String host) {
        return this.virtualHosts.stream().filter(v -> v.getHost().getHostName().equals(host)).findAny().orElse(null);
    } 

    /**
     * Whether virtual host
     * @param serverName
     * @return
     */
    public boolean isVirtualHost(String serverName) {
        if(getVirtualHost(serverName) == null) {
            return false;
        }
        return true;
    }

    /**
     * Get virtual host list
     * @return
     */
    public List<VirtualHost> getVirtualHosts() {
        return this.virtualHosts;
    }

    /**
     * Virtual host object
     */
    public class VirtualHost {

        InetSocketAddress host;
        Path docroot, logPath;
        String logLevel;
    
        public VirtualHost(InetSocketAddress host, Path docroot, Path logPath, String logLevel) {
            this.host = host;
            this.docroot = docroot;
            this.logPath = logPath;
            this.logLevel = logLevel;
        }
    
        public InetSocketAddress getHost() {
            return this.host;
        }
    
        public int getPort() {
            return this.host.getPort();
        }
    
        public Path getDocroot() {
            return this.docroot;
        }
    
        public Path getLogPath() {
            return this.logPath;
        }

        public String getLogLevel() {
            return this.logLevel;
        }
    
        @Override
        public String toString() {
            return "{" +
                " host='" + getHost() + "'" +
                ", getPort='" + getPort() + "'" +
                ", docroot='" + getDocroot() + "'" +
                ", logPath='" + getLogPath() + "'" +
                ", logLevel='" + getLogLevel() + "'" +
                "}";
        }
    }           
}
