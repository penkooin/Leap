package org.chaostocosmos.leap.resource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.context.META;
import org.chaostocosmos.leap.resource.config.ResourceConfig;
import org.chaostocosmos.leap.resource.filter.ResourceFilter;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;
import org.chaostocosmos.leap.resource.utils.ResourceUtils;

/**
 * StaticResourceManager
 * 
 * @author 9ins
 */
public class ResourceProvider {

    /**
     * Resource provider configuration Map
     */
    ResourceConfig<?> config = new ResourceConfig<>(ResourceUtils.loadConfig(META.RESOURCE.getMetaPath()));

    /**
     * WatchResource object Map by Hosts
     */
    Map<Path, ResourcesWatcherModel> resourceWatcherMap = 
                                     this.config.getWatchRoots()
                                                .stream()
                                                .map(p -> {                                                    
                                                    Path rootPath = Paths.get(p);
                                                    WatchEvent.Kind<?>[] kinds = getWatchEventKind(config.getWatchKind());
                                                    ResourceFilter accessFiltering = new ResourceFilter(config.getAccessFilters());
                                                    ResourceFilter inMemoryFiltering = new ResourceFilter(config.getInMemoryFilters());
                                                    int inMemorySplitUnit = config.getSplitUnitSize();
                                                    int fileSizeLimit = config.getFileSizeLimit();
                                                    int fileReadBufferSize = config.getFileReadBufferSize();
                                                    int fileWriteBufferSize = config.getFileWriteBufferSize();
                                                    long totalMemorySizeLimit = config.getTotalMemorySizeLimit();
                                                    return new Object[]{rootPath, new ResourceWatcher(
                                                                        rootPath, 
                                                                        kinds, 
                                                                        accessFiltering, 
                                                                        inMemoryFiltering, 
                                                                        inMemorySplitUnit, 
                                                                        fileSizeLimit, 
                                                                        fileReadBufferSize, 
                                                                        fileWriteBufferSize, 
                                                                        totalMemorySizeLimit)};
                                                }).collect(Collectors.toMap(k -> (Path) k[0], v -> (ResourceWatcher) v[1]));

    /**
     * ResourceProvider object
     */
    private static ResourceProvider resourceProvider = null;

    /**
     * Get Resource Provider instance
     * @return
     */
    public static ResourceProvider get() {
        if(resourceProvider == null) {
            resourceProvider = new ResourceProvider();
        }
        return resourceProvider;
    }

    /**
     * Default constructur
     */
    private ResourceProvider() {        
    }

    /**
     * Constructs with config Path
     * @param watchConfigPath
     */
    private ResourceProvider(Path watchConfigPath) {
        this(ResourceUtils.loadConfig(watchConfigPath));
    }

    /**
     * Constructs with config Map
     * @param configMap
     */
    private ResourceProvider(Map<String, Object> configMap) {
        this(new ResourceConfig<Map<String, Object>> (configMap));
    }

    /**
     * Constructs with config object
     * @param config
     */
    private ResourceProvider(ResourceConfig<Map<String, Object>> config) {
        this.config = config;
    }

    /**
     * Get static WatchResource object
     * @param watchPath
     * @return
     */
    public ResourcesWatcherModel get(Path watchPath) {
        if(!resourceWatcherMap.containsKey(watchPath)) {
            throw new RuntimeException("There is no ResourceWatcher matching with specified watcher ID: " + watchPath);
        }
        return resourceWatcherMap.get(watchPath);
    }

    /**
     * Add Resource Watcher with specfic Path
     * @param watchPath
     * @return
     */
    private ResourcesWatcherModel createResourceWatcher(Path watchPath) {
        if(!this.resourceWatcherMap.containsKey(watchPath) ) {
            this.config.getWatchRoots().add(watchPath.toString());
            WatchEvent.Kind<?>[] kinds = getWatchEventKind(config.getWatchKind());
            ResourceFilter accessFiltering = new ResourceFilter(config.getAccessFilters());
            ResourceFilter inMemoryFiltering = new ResourceFilter(config.getInMemoryFilters());
            int inMemorySplitUnit = config.getSplitUnitSize();
            int fileSizeLimit = config.getFileSizeLimit();
            int fileReadBufferSize = config.getFileReadBufferSize();
            int fileWriteBufferSize = config.getFileWriteBufferSize();
            long totalMemorySizeLimit = config.getTotalMemorySizeLimit();
            ResourceWatcher resourceWatcher = new ResourceWatcher(watchPath, kinds, accessFiltering, inMemoryFiltering, inMemorySplitUnit, fileSizeLimit, fileReadBufferSize, fileWriteBufferSize, totalMemorySizeLimit);
            this.resourceWatcherMap.put(watchPath, resourceWatcher);            
        }
        return this.resourceWatcherMap.get(watchPath);
    }

    /**
     * Add specific Path to watch resource if privous watcher is not exist, creating new one.
     * @param watchPath
     * @return
     * @throws Exception
     */
    public boolean addPath(Path watchPath) throws Exception {
        if(this.resourceWatcherMap.containsKey(watchPath)) {
            return false;
            //throw new IllegalArgumentException("Specified Path is already applied. Path: "+watchPath.toAbsolutePath());
        }
        ResourcesWatcherModel watcher = createResourceWatcher(watchPath);
        watcher.start();
        this.resourceWatcherMap.put(watchPath, watcher);
        return true;        
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
     * @param watchPath
     * @return
     */
    public ResourcesWatcherModel getResource(Path watchPath) {
        return this.resourceWatcherMap.get(watchPath);
    }

    /**
     * Get Resources Map
     * @return
     */
    public Map<Path, ResourcesWatcherModel> getResourceWatcherMap() {
        return this.resourceWatcherMap;
    } 

    /**
     * Start all resource watcher
     * @throws Exception 
     */
    public void startWatchers() throws Exception {
        for(Entry<Path, ResourcesWatcherModel> e : this.resourceWatcherMap.entrySet()) {
            e.getValue().start();
            LoggerFactory.getLogger().info("START RESOURCE WATCHER ---------- PATH: "+e.getKey().toAbsolutePath());
        }
    }

    public void restartWatch(Path watchPath) throws Exception {
        ResourcesWatcherModel watcher = null;
        if(this.resourceWatcherMap.containsKey(watchPath)) {
            watcher = this.resourceWatcherMap.get(watchPath);
            watcher.terminate();
        } else {
            watcher = createResourceWatcher(watchPath);
        }
        watcher.start();
    }

    /**
     * Terminate all resource watcher
     * @throws Exception 
     */
    public void terminates() throws Exception {
        for(ResourcesWatcherModel watcher : this.resourceWatcherMap.values()) {
            watcher.terminate();
        }
    }
}
