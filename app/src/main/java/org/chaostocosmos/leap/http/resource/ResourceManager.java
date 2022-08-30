package org.chaostocosmos.leap.http.resource;

import java.io.IOException;
import java.net.URISyntaxException;
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
    Map<String, ResourcesModel> resourceMap;
    /**
     * StaticResourceManager object
     */
    private static ResourceManager manager;
    /**
     * Default constructor
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     * @throws ImageProcessingException
     */
    private ResourceManager() throws IOException, InterruptedException, URISyntaxException {
        this.resourceMap = new HashMap<>();
        for(Host<?> host : Context.getHosts().getAllHost()) {
            //initalize host environment
            ResourceHelper.extractResource("webapp", host.getDocroot());
            this.resourceMap.put(host.getHost(), new WatchResources(host, WATCH_KIND));
        }
    }

    /**
     * Initialize StaticResourceManager
     * @throws IOException
     * @return
     * @throws InterruptedException
     * @throws URISyntaxException
     * @throws ImageProcessingException
     */
    public static ResourceManager initialize() throws IOException, InterruptedException, URISyntaxException {
        if(manager == null) {
            manager = new ResourceManager();
        }        
        return manager;
    }

    /**
     * Get static WatchResource object
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     * @throws ImageProcessingException
     */
    public static ResourcesModel get(String hostId) throws IOException, InterruptedException, URISyntaxException {
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
    public ResourcesModel getResource(String hostId) throws IOException, InterruptedException {
        if(!this.resourceMap.containsKey(hostId)) {
            this.resourceMap.put(hostId, new WatchResources(Context.getHosts().getHost(hostId), WATCH_KIND));
        }
        return this.resourceMap.get(hostId);
    }

    /**
     * Get Resources Map
     * @return
     */
    public Map<String, ResourcesModel> getResourceMap() {
        return this.resourceMap;
    } 
}
