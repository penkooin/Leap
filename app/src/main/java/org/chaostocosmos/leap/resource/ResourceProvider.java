package org.chaostocosmos.leap.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.META;
import org.chaostocosmos.leap.resource.config.ConfigUtils;
import org.chaostocosmos.leap.resource.config.ResourceProviderConfig;
import org.chaostocosmos.leap.resource.filter.ResourceFilter;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;

/**
 * StaticResourceManager
 * 
 * @author 9ins
 */
public class ResourceProvider {

    /**
     * WatchResource object Map by Hosts
     */
    Map<String, ResourcesWatcherModel> resourceWatcherMap = new HashMap<>();

    /**
     * Resource provider configuration Map
     */
    ResourceProviderConfig<?> config;

    private static ResourceProvider resourceProvider = null;

    /**
     * Get Resource Provider instance
     * @return
     * @throws IOException
     * @throws NotSupportedException
     */
    public static ResourceProvider get() throws IOException, NotSupportedException {
        if(resourceProvider == null) {
            resourceProvider = new ResourceProvider(META.RESOURCE.getMetaPath());
        }
        return resourceProvider;
    }

    /**
     * Constructs with config Path
     * @param configPath
     * @throws IOException
     * @throws NotSupportedException 
     */
    private ResourceProvider(Path configPath) throws IOException, NotSupportedException {
        this(ConfigUtils.loadConfig(configPath));
    }

    /**
     * Constructs with config Map
     * @param configMap
     */
    private ResourceProvider(Map<String, Object> configMap) {
        this(new ResourceProviderConfig<Map<String, Object>> (configMap));
    }

    /**
     * Constructs with config object
     * @param config
     */
    private ResourceProvider(ResourceProviderConfig<Map<String, Object>> config) {
        this.config = config;
        this.resourceWatcherMap = this.config.getWatchRoots().entrySet().stream().map(e -> {
            String watchId = e.getKey();
            Path rootPath = Paths.get(e.getValue());
            WatchEvent.Kind<?>[] kinds = getWatchEventKind(config.getWatchKind());
            ResourceFilter accessFiltering = new ResourceFilter(config.getAccessFilters());
            ResourceFilter inMemoryFiltering = new ResourceFilter(config.getInMemoryFilters());
            int inMemorySplitUnit = config.getSplitUnitSize();
            int fileSizeLimit = config.getFileSizeLimit();
            int fileReadBufferSize = config.getFileReadBufferSize();
            int fileWriteBufferSize = config.getFileWriteBufferSize();
            long totalMemorySizeLimit = config.getTotalMemorySizeLimit();
            return new Object[] {watchId, new ResourceWatcher(watchId, rootPath, kinds, accessFiltering, inMemoryFiltering, inMemorySplitUnit, fileSizeLimit, fileReadBufferSize, fileWriteBufferSize, totalMemorySizeLimit)};
        }).collect(Collectors.toMap(k -> (String)k[0], v -> (ResourceWatcher)v[1]));
                
    }

    /**
     * Get static WatchResource object
     * @param watcherId
     * @return
     */
    public ResourcesWatcherModel get(String watcherId) {
        if(!resourceWatcherMap.containsKey(watcherId)) {
            throw new RuntimeException("There is no ResourceWatcher matching with specified watcher ID: "+watcherId);
        }
        return resourceWatcherMap.get(watcherId);
    }

    /**
     * Get watch event kind
     * @param watchEventKindList
     * @return
     */
    public Kind<?>[] getWatchEventKind(List<String> watchEventKindList) {
        return watchEventKindList.stream().map(s -> {
            switch(s) {                
                case "ENTRY_CREATE" :
                return StandardWatchEventKinds.ENTRY_CREATE;
                case "ENTRY_DELETE" :
                return StandardWatchEventKinds.ENTRY_DELETE;
                case "ENTRY_MODIFY" :
                return StandardWatchEventKinds.ENTRY_MODIFY;
                case "OVERFLOW" :
                return StandardWatchEventKinds.OVERFLOW;
            }
            return null;
        }).filter(k -> k != null).collect(Collectors.toList()).toArray(new Kind<?>[0]);
    }

    /**
     * Get Resource object for host
     * @param hostId
     * @return
     */
    public ResourcesWatcherModel getResource(String hostId) {
        return this.resourceWatcherMap.get(hostId);
    }

    /**
     * Get Resources Map
     * @return
     */
    public Map<String, ResourcesWatcherModel> getResourceWatcherMap() {
        return this.resourceWatcherMap;
    } 

    /**
     * Terminate all resource watcher
     */
    public void terminates() {
        this.resourceWatcherMap.values().stream().forEach(w -> w.terminate());        
    }
}
