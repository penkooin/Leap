package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;

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
     * Resource object Map by Hosts
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
        for(String host : Context.getHostsMap().keySet()) {
            this.resourceMap.put(host, new Resource(host));
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
     * Get static resource manager object
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
            this.resourceMap.put(host, new Resource(host));
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

    /**
     * Resource object for host
     * 
     * @author 9ins
     */
    public class Resource extends Thread {

        String host;        
        Path staticResourcePath;
        //List<Path> inDiskResource;
        //Map<Path, byte[]> inMemoryResourceMap;
        //Map<Path, Object> resourceTree;
        List<Path> resourceDirs;
        WatchService watchService;
        Map<WatchKey, Object> hostResourceMap;

        /**
         * Construct with host name
         * @param host
         * @throws IOException
         */
        public Resource(String host) throws IOException {
            this.host = host;
            this.staticResourcePath = ResourceHelper.getStaticPath(this.host);
            //this.inDiskResource = getInDiskResourcePaths();
            //this.inMemoryResourceMap = loadInMemoryResources();
            //this.resourceTree = new LinkedHashMap<>();
            ////////////////////////////////////////////////////////////////////////////////////////////
            //Critical section... Below code is setting Resource object to host object in Context.
            //This must be implemented at this point after all of in-memory and resource be loaded.
            ////////////////////////////////////////////////////////////////////////////////////////////
            Context.getHosts(host).setResource(this);

            //Starting resource files watcher 
            this.watchService = FileSystems.getDefault().newWatchService();
            this.resourceDirs = Files.walk(this.staticResourcePath).filter(p -> p.toFile().isDirectory()).collect(Collectors.toList());
            for(Path p : resourceDirs) { 
                this.watchMap.put(p.register(this.watchService, WATCH_KIND), p);
                LoggerFactory.getLogger(this.host).info("Path: "+p.toString());
            }
            //this.resourceTree = loadResoureTree(this.resourceTree, this.staticResourcePath);

            start();
            LoggerFactory.getLogger(this.host).info("[RESOUCE INITIALIZED] Resource object for host: "+this.host+" started. Watching Paths: ");
        }

        Map<WatchKey, Path> watchMap = new HashMap<>();

        /**
         * Watch static resources on host
         */
        public void run() {
            WatchKey key = null;
            while(true) {
                try {
                    key = this.watchService.take();
                    for(WatchEvent<?> event : key.pollEvents()) {
                        System.out.println("KIND: "+event.kind()+"   Context: "+event.context()+"   Path: "+this.watchMap.get(key));
                        Path eventPath = this.watchMap.get(key);
                        if(eventPath == null) continue;
                        eventPath = eventPath.resolve((Path)event.context());
                        if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            if(eventPath.toFile().isDirectory()) {
                                watchMap.put(eventPath.register(this.watchService, WATCH_KIND), eventPath);
                                this.resourceTree.put(eventPath, new LinkedHashMap<>());
                            } else {
                                if(HostsManager.get().filteringInMemory(this.host, eventPath.toFile().getName())) {
                                    this.resourceTree.put(eventPath, Files.readAllBytes(eventPath));
                                } else if(HostsManager.get().filteringInAccess(this.host, eventPath.toFile().getName())) {
                                    this.resourceTree.put(eventPath, eventPath.toFile());
                                }
                            }
                        } else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            this.resourceTree.remove(eventPath);
                            watchMap.remove(key);
                            LoggerFactory.getLogger(this.host).debug("[WATCH] Resource removed: "+eventPath.toString());
                        } else {
                            LoggerFactory.getLogger(this.host).debug("[WATCH] Watch service overflow detected: "+event.toString());
                        }
                    }
                    ObjectMapper om = new ObjectMapper();
                    String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(this.resourceTree);
                    System.out.println(json);
                } catch(Exception e) {
                    LoggerFactory.getLogger(this.host).error(e.getMessage(), e);
                } finally {
                    if(key != null) {
                        key.reset();
                    }
                }
            }
        }

        /**
         * Get welcome page matching with each host and virtual host
         * @param params
         * @return
         */
        public String getWelcomePage(Map<String, Object> params) {
            return getStaticPage(HostsManager.get().getHosts(this.host).getWelcomeFile().getName(), params);
        }

        /**
         * Get response page with code
         * @param code
         * @return
         */
        public String getResponsePage(Map<String, Object> params) {
            return getStaticPage("response.html", params);
        }

        /**
         * Get in-disk resources path
         * @return
         * @throws IOException
        public List<Path> getInDiskResourcePaths() throws IOException {
            return FileUtils.searchFiles(this.staticResourcePath, HostsManager.get().getHosts(this.host).getAccessFilters()).stream().map(f -> f.toPath()).collect(Collectors.toList());
        }
         */

        /**
         * Get resources to be in memory
         * @return
         * @throws IOException
        public List<Path> getInMemoryResourcePaths() throws IOException {
            return FileUtils.searchFiles(this.staticResourcePath, HostsManager.get().getHosts(this.host).getInMemoryFilters()).stream().map(f -> f.toPath()).collect(Collectors.toList());
        }
         */

        /**
         * Load all in-memory resource by host
         * @return
         * @throws IOException
        public Map<Path, byte[]> loadInMemoryResources() throws IOException {
            List<Path> paths = getInMemoryResourcePaths();
            return paths.stream().map(p -> {
                try {
                    return new Object[]{p, Files.readAllBytes(p)};
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toMap(k -> (Path)k[0], v -> (byte[])v[1]));
        }
         */

        /**
         * Get in-memory resource which matching with specified resource name
         * @param resourcePath
         * @return
        public String getInMemoryPage(Path resourcePath) {
            return new String(getInMemoryData(resourcePath), HostsManager.get().charset(this.host));
        }
         */

        /**
         * Get in-memory bytes data
         * @param resourcePath
         * @return
        public byte[] getInMemoryData(Path resourcePath) {
            System.out.println(inMemoryResourceMap.get(this.staticResourcePath.resolve(resourcePath)));
            return this.inMemoryResourceMap.get(this.staticResourcePath.resolve(resourcePath));
        }
         */

        /**
         * Get in-disk resource which matching with specified resource name
         * @param resourcePath
         * @return
         * @throws IOException
        public String getInDiskPage(Path resourcePath) throws IOException {            
            return new String(getInDiskData(resourcePath), HostsManager.get().charset(this.host));            
        }
         */

        /**
         * Get in-disk resource bytes data
         * @param resourcePath
         * @return
         * @throws IOException
        public byte[] getInDiskData(Path resourcePath) throws IOException {
            return Files.readAllBytes(this.inDiskResource.stream().filter(p -> p.equals(resourcePath)).findAny().orElseThrow(() -> new WASException(MSG_TYPE.HTTP, 404, " Specified resource not found in Leap host")));
        }
         */

        /**
         * Load all resource of host
         * @param tree
         * @param path
         * @return
         * @throws IOException
        public Map<Path, Object> loadResoureTree(Map<Path, Object> tree, Path path) throws IOException {
            File[] files = path.toFile().listFiles();
            for(File file : files) {
                if(file.isDirectory()) {
                    tree.put(file.toPath(), loadResoureTree(new LinkedHashMap<Path, Object>(), file.toPath()));
                } else {
                    if(HostsManager.get().filteringInMemory(this.host, file.getName())) {
                        tree.put(file.toPath(), Files.readAllBytes(file.toPath()));
                    } else if(HostsManager.get().filteringInAccess(this.host, file.getName())) {
                        tree.put(file.toPath(), file);
                    }
                }
            }
            return tree;
        }
         */

        /**
         * Get resource data from resource tree map
         * @param path
         * @return
         */
        public Object getResourceData(Path path) {
            String[] paths = path.toString().substring(this.staticResourcePath.toString().length() + 1).split(Pattern.quote(File.separator));
            Object obj = getResourceData(paths);
            if(obj instanceof byte[]) {
                LoggerFactory.getLogger(this.host).debug("[RESOURCE] In-Memory resource requested: "+path);
            } else if(obj instanceof File) {
                LoggerFactory.getLogger(this.host).debug("[RESOURCE] In-Disk resource requested: "+path);
            }
            return obj;
        }

        /**
         * Get resource data from resource tree Map
         * @param path
         * @return
         */
        public Object getResourceData(String[] paths) {
            Map<Path, Object> map = this.resourceTree;
            for(String rs : paths) {
                Object obj = map.entrySet().stream().filter(e -> e.getKey().toFile().getName().equals(rs)).map(e -> e.getValue()).findAny().orElseThrow(() -> new IllegalStateException("Specfied path is not exist!!!"));
                if(obj instanceof Map == true) {
                    map = (Map<Path, Object>)obj;
                    continue;
                }
                return obj;
            }
            return null;
        }

        /**
         * Get static text resource by name
         * @param resourceName
         * @param params
         * @return
         */
        public String getStaticPage(String resourceName, Map<String, Object> params) {
            byte[] bytes = getResourceContent(resourceName);
            String page = new String(bytes, HostsManager.get().getHosts(this.host).charset());
            for(Entry<String, Object> e : params.entrySet()) {
                page = page.replace(e.getKey(), e.getValue().toString());
            }
            return page;
        }

        /**
         * Get resource replaced with specified params
         * @param contentName
         * @param param
         * @return
         */
        public byte[] getResourceContent(String contentName) {
            try {
                Path path = this.staticResourcePath.resolve(contentName);
                LoggerFactory.getLogger(this.host).debug("REQUEST RESOURCE: "+contentName+"   PATH: "+path.toString());
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new WASException(MSG_TYPE.ERROR, 38, contentName);
            }
        }
    }    
}
