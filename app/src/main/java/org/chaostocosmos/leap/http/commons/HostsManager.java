package org.chaostocosmos.leap.http.commons;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.WASException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Virtual host manager object
 * @author 9ins
 * @since 2021.09.18
 */
public class HostsManager {
    /**
     * Logger
     */
    Logger logger = (Logger)LoggerFactory.getLogger(Context.getDefaultHost());

    /**
     * Virtual hosts
     */
    private List<Hosts> hosts = null;

    /**
     * HostsManager
     */
    private static HostsManager hostsManager = null;

    /**
     * Constructor
     * 
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    private HostsManager() {        
        this.hosts =  new ArrayList<>();
        this.hosts.add(Context.getDefaultHosts());
        this.hosts.addAll(Context.getVirtualHosts().values());
    }

    /**
     * Get VirtualHostManager instance
     * 
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */
    public static HostsManager getInstance() {
        if(hostsManager == null) {
            hostsManager = new HostsManager();
        }
        return hostsManager;
    }

    /**
     * Get virtual host
     * @param serverName
     * @return
     */
    public Hosts getHosts(String host) {
        return this.hosts.stream().filter(v -> v.getHost().equals(host)).findAny().orElse(null);
    } 

    /**
     * Whether virtual host
     * @param serverName
     * @return
     */
    public boolean isVirtualHost(String serverName) {
        return this.hosts.stream().filter(h -> h.getServerName().equals(serverName)).anyMatch(h -> h.isDefaultHost());
    }

    /**
     * Get virtual host list
     * @return
     */
    public List<Hosts> getHosts() {
        return this.hosts; 
    }
}
