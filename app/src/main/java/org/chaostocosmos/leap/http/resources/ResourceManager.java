package org.chaostocosmos.leap.http.resources;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;

/**
 * StaticResourceManager
 * 
 * @author 9ins
 */
public class ResourceManager {
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
    Map<String, Resources> resourceMap;

    /**
     * StaticResourceManager object
     */
    private static ResourceManager manager;

    /**
     * Default constructor
     * @throws IOException
     * @throws InterruptedException
     * @throws ImageProcessingException
     */
    private ResourceManager() throws IOException, InterruptedException {
        this.resourceMap = new HashMap<>();
        for(Host<?> host : Context.getHosts().getHostMap().values()) {
            this.resourceMap.put(host.getHost(), new WatchResources(host, WATCH_KIND));
        }
    }

    /**
     * Initialize StaticResourceManager
     * @throws IOException
     * @return
     * @throws InterruptedException
     * @throws ImageProcessingException
     */
    public static ResourceManager initialize() throws IOException, InterruptedException {
        manager = new ResourceManager();
        return manager;
    }

    /**
     * Get static WatchResource object
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ImageProcessingException
     */
    public static Resources get(String hostId) throws IOException, InterruptedException {
        if(manager == null) {
            manager = new ResourceManager();
        }
        return manager.getResource(hostId);
    }

    /**
     * Get Resource object for host
     * @param hostId
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ImageProcessingException
     */
    public Resources getResource(String hostId) throws IOException, InterruptedException {
        if(!this.resourceMap.containsKey(hostId)) {
            this.resourceMap.put(hostId, new WatchResources(Context.getHosts().getHost(hostId), WATCH_KIND));
        }
        return this.resourceMap.get(hostId);
    }

    /**
     * Get Resources Map
     * @return
     */
    public Map<String, Resources> getResourceMap() {
        return this.resourceMap;
    } 
}
