package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.Hosts;

/**
 * StaticResourceManager
 * 
 * @author 9ins
 */
public class StaticResourceManager {
    /**
     * Watch service kind
     */
    public static final Kind[] WATCH_KIND = {StandardWatchEventKinds.ENTRY_CREATE,  
                                             StandardWatchEventKinds.ENTRY_DELETE, 
                                             StandardWatchEventKinds.ENTRY_MODIFY, 
                                             StandardWatchEventKinds.OVERFLOW};

    /**
     * WatchResource object Map by Hosts
     */
    Map<String, Resource> resourceMap;

    /**
     * StaticResourceManager object
     */
    private static StaticResourceManager manager;

    /**
     * Default constructor
     * @throws IOException
     */
    private StaticResourceManager() throws IOException {
        this.resourceMap = new HashMap<>();
        for(Hosts hosts : Context.getHostsMap().values()) {
            this.resourceMap.put(hosts.getHost(), new WatchResource(hosts, WATCH_KIND));
        }
    }

    /**
     * Initialize StaticResourceManager
     * @throws IOException
     * @return
     */
    public static StaticResourceManager initialize() throws IOException {
        manager = new StaticResourceManager();
        return manager;
    }

    /**
     * Get static WatchResource object
     * @return
     * @throws IOException
     */
    public static Resource get(String host) throws IOException {
        if(manager == null) {
            manager = new StaticResourceManager();
        }
        return manager.getResource(host);
    }

    /**
     * Get Resource object for host
     * @param host
     * @return
     * @throws IOException
     */
    public Resource getResource(String host) throws IOException {
        if(!this.resourceMap.containsKey(host)) {
            this.resourceMap.put(host, new WatchResource(Context.getHosts(host), WATCH_KIND));
        }
        return this.resourceMap.get(host);
    }

    /**
     * Get Resources Map
     * @return
     */
    public Map<String, Resource> getResourceMap() {
        return this.resourceMap;
    } 
}
