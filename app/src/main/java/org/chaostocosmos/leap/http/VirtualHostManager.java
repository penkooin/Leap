package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

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
    Logger logger = (Logger)LoggerFactory.getLogger(VirtualHostManager.class);

    /**
     * Virtual hosts
     */
    private List<Hosts> virtualHosts = new ArrayList<>();

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
        List<?> vList = (List<?>)Context.getConfigValue("server.virtual-host");
        for(Object obj : vList) {
            Map<?, ?> m = (Map<?, ?>)obj;
            String serverName = (String)m.get("server-name");
            String host = (String)m.get("host");
            int port = Integer.parseInt(m.get("port")+"");
            Path vDocroot = Paths.get((String)m.get("doc-root"));
            String logPath = (String)m.get("logs");
            Level logLevel = Level.toLevel((String)m.get("log-level"));
            this.virtualHosts.add(new Hosts(serverName, host, port, vDocroot, logPath, logLevel));            
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
    public Hosts getVirtualHost(String host) {
        return this.virtualHosts.stream().filter(v -> v.getHost().equals(host)).findAny().orElse(null);
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
    public List<Hosts> getVirtualHosts() {
        return this.virtualHosts; 
    }
}
