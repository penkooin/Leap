package org.chaostocosmos.leap.http.resources;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;

import com.drew.imaging.ImageProcessingException;

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
    Map<String, Resources> resourceMap;

    /**
     * StaticResourceManager object
     */
    private static StaticResourceManager manager;

    /**
     * Default constructor
     * @throws IOException
     * @throws ImageProcessingException
     */
    private StaticResourceManager() throws IOException, ImageProcessingException {
        this.resourceMap = new HashMap<>();
        for(Hosts hosts : Context.getHostsMap().values()) {
            this.resourceMap.put(hosts.getHost(), new WatchResources(hosts, WATCH_KIND));
        }
    }

    /**
     * Initialize StaticResourceManager
     * @throws IOException
     * @return
     * @throws ImageProcessingException
     */
    public static StaticResourceManager initialize() throws IOException, ImageProcessingException {
        manager = new StaticResourceManager();
        return manager;
    }

    /**
     * Get static WatchResource object
     * @return
     * @throws IOException
     * @throws ImageProcessingException
     */
    public static Resources get(String host) throws IOException, ImageProcessingException {
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
     * @throws ImageProcessingException
     */
    public Resources getResource(String host) throws IOException, ImageProcessingException {
        if(!this.resourceMap.containsKey(host)) {
            this.resourceMap.put(host, new WatchResources(Context.getHosts(host), WATCH_KIND));
        }
        return this.resourceMap.get(host);
    }

    /**
     * Get Resources Map
     * @return
     */
    public Map<String, Resources> getResourceMap() {
        return this.resourceMap;
    } 
}
