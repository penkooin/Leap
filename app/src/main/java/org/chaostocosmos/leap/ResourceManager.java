package org.chaostocosmos.leap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.WEB_PATH;
import org.chaostocosmos.leap.resource.ResourceHelper;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.resource.WatchResources;

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
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    private ResourceManager() throws IOException, URISyntaxException, InterruptedException {
        this.resourceMap = new HashMap<>();
        for(Host<?> host : Context.get().hosts().getAllHost()) {
            //initalize host environment
            String path = WEB_PATH.WEBAPP.name().toLowerCase();
            ResourceHelper.extractResource(path, host.getDocroot());
            if(!this.resourceMap.containsKey(host.getHostId())) {
                this.resourceMap.put(host.getHostId(), new WatchResources(host, WATCH_KIND));
            }
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
    public static ResourceManager initialize() {
        if(manager == null) {
            try {
                manager = new ResourceManager();
            } catch (IOException | URISyntaxException | InterruptedException e) {
                LoggerFactory.getLogger().error(e.getMessage(), e);
            }
        }        
        return manager;
    }

    /**
     * Get static WatchResource object
     * @return
     */
    public static ResourcesModel get(String hostId) {
        if(manager == null) {
            try {
                manager = new ResourceManager();
            } catch (IOException | InterruptedException | URISyntaxException e) {
                LoggerFactory.getLogger(hostId).error(e.getMessage(), e);
            }
        }
        return manager.getResource(hostId);
    }

    /**
     * Get Resource object for host
     * @param hostId
     * @return
     */
    public ResourcesModel getResource(String hostId) {
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
