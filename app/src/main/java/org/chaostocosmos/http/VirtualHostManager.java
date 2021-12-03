package org.chaostocosmos.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * @throws WASException
     */
    private VirtualHostManager() {        
        List<Map<String, Object>> vList = (List<Map<String, Object>>)context.getConfigValue("server.virtual-host");
        for(Map<String, Object> m : vList) {
            String host = m.get("host").toString();
            if(host.length() - host.replace(":", "").length() != 1) {
                throw new RuntimeException(context.getErrorMsg("error015"));
            }
            String[] hp = host.split("\\:");
            InetSocketAddress addr = new InetSocketAddress(hp[0].trim(), Integer.parseInt(hp[1].trim()));
            Path vDocroot = Paths.get(m.get("doc-root").toString());
            try {
                UtilBox.extractEnvironment(vDocroot);
            } catch (IOException e) {
                LoggerFactory.getLogger(UtilBox.class).error("Error found in extracting environment process", e);
            }
            this.virtualHosts.add(new VirtualHost(addr, m.get("serverName").toString().trim(), vDocroot));
        }  
    }

    /**
     * Get VirtualHostManager instance
     * 
     * @return
     * @throws WASException
     */
    public static VirtualHostManager getInstance() {
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
    public VirtualHost getVirtualHost(String serverName) {
        return this.virtualHosts.stream().filter(v -> v.getServerName().equals(serverName)).findAny().orElse(null);
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
        String serverName;
        Path docroot;
    
        public VirtualHost(InetSocketAddress host, String serverName, Path docroot) {
            this.host = host;
            this.serverName = serverName;
            this.docroot = docroot;
        }
    
        public InetSocketAddress getHost() {
            return this.host;
        }
    
        public void setHost(InetSocketAddress host) {
            this.host = host;
        }
    
        public String getServerName() {
            return this.serverName;
        }
    
        public void setServerName(String serverName) {
            this.serverName = serverName;
        }
    
        public Path getDocroot() {
            return this.docroot;
        }
    
        public void setDocroot(Path docroot) {
            this.docroot = docroot;
        }
    
        @Override
        public String toString() {
            return "{" +
                " host='" + getHost() + "'" +
                ", serverName='" + getServerName() + "'" +
                ", docroot='" + getDocroot() + "'" +
                "}";
        }
    }           
}
