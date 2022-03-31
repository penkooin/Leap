package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.Unit;

/**
 * WatchResource object
 * 
 * @author 9ins
 */
public class WatchResource extends Thread implements Resource {
    String host;
    Path watchPath;
    Kind<?>[] watchKind;
    List<String> accessFilters, inMemoryFilters;
    int inMemoryLimitSize;
    Map<String, Object> resourceTree;
    Map<WatchKey, Path> watchMap;
    WatchService watchService;
    Gson gson = new Gson();

    /**
     * Create with Hosts object, Watch kinds
     * @param hosts
     * @param watchKinds
     * @throws IOException
     */
    public WatchResource(Hosts hosts, Kind<?>[] watchKinds) throws IOException {
        this(hosts.getHost(), hosts.getStatic(), watchKinds, hosts.getAccessFilters(), hosts.getInMemoryFilters(), 1024 * 1000);
    }

    /**
     * Create with params
     * @param hosts
     * @param watchPath
     * @param watchKinds
     * @param accessFilters
     * @param inMemoryFilters
     * @throws IOException
     */
    public WatchResource(String host, Path watchPath, Kind<?>[] watchKinds, List<String> accessFilters, List<String> inMemoryFilters, int inMemoryLimitSize) throws IOException {
        this.host = host;
        this.watchPath = watchPath;
        this.watchKind = watchKinds;
        this.accessFilters = accessFilters;
        this.inMemoryFilters = inMemoryFilters;
        this.inMemoryLimitSize = inMemoryLimitSize;
        this.resourceTree = new LinkedHashMap<>();
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchMap = Files.walk(this.watchPath).sorted().filter(p -> p.toFile().isDirectory()).map(p -> {
            try {
                return new Object[]{p.register(this.watchService, this.watchKind), p};
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(arr -> arr != null).collect(Collectors.toMap(k -> (WatchKey)k[0], v -> (Path)v[1]));
        loadResoureTree(this.watchPath, this.resourceTree);

        //Have to set WatchResource to Hosts
        Context.getHosts(this.host).setResource(this);
        //Start watch thread
        start();
    }

    @Override
    public void run() {
        try {
            while(this.watchService != null) {
                final WatchKey key = this.watchService.take();   
                for(WatchEvent<?> event : key.pollEvents()) {
                    Path path = this.watchMap.get(key);
                    LoggerFactory.getLogger(this.host).debug("KIND: "+event.kind()+"   Context: "+event.context()+"   Path: "+this.watchMap.get(key)+"   CNT: "+event.count());
                    if(event.kind() == StandardWatchEventKinds.OVERFLOW || path == null) {
                        continue;
                    } 
                    if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        path = path.resolve(event.context().toString());
                        Object data = null;
                        if(path.toFile().isDirectory()) {
                            System.out.println(path+" )))))))))))))))))))))"+path.subpath(this.watchPath.getNameCount(), path.getNameCount()).toString());
                            this.watchMap.put(path.register(this.watchService, this.watchKind), path);
                            data = new LinkedHashMap<>();
                        } else {
                            if(filtering(path.toFile().getName(), this.accessFilters)) {                  
                                if(filtering(path.toFile().getName(), this.inMemoryFilters)) {
                                    try {
                                        Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);
                                        if(path.toFile().length() <= this.inMemoryLimitSize) {
                                            LoggerFactory.getLogger(this.host).debug("Loaded to memory.........."+((byte[])data).length);
                                            data = Files.readAllBytes(path);
                                        } else {
                                            LoggerFactory.getLogger(this.host).debug("In-Memory file size overflow. Limit: "+Unit.MB(this.inMemoryLimitSize, 2)+"  File size: "+Unit.MB(path.toFile().length(), 2));
                                            data = path.toFile();
                                        }
                                    } catch(Exception e) {
                                        continue;
                                    }
                                } else {
                                    data = path.toFile();
                                }
                            }
                        }
                        String[] paths = path.subpath(this.watchPath.getNameCount(), path.getNameCount()).toString().split(Pattern.quote(File.separator));
                        addResource(this.resourceTree, paths, data);
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {                        
                        path = path.resolve(event.context().toString());
                        if(path.toFile().isFile() && filtering(path.toFile().getName(), this.accessFilters)) {
                            Object data = null;
                            if(filtering(path.toFile().getName(), this.inMemoryFilters)) {
                                try {
                                    Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);                                    
                                    if(path.toFile().length() <= this.inMemoryLimitSize) {
                                        LoggerFactory.getLogger(this.host).debug("Loaded to memory.........."+((byte[])data).length);
                                        data = Files.readAllBytes(path);
                                    } else {
                                        LoggerFactory.getLogger(this.host).debug("In-Memory file size overflow. Limit: "+Unit.MB(this.inMemoryLimitSize, 2)+"  File size: "+Unit.MB(path.toFile().length(), 2));
                                        data = path.toFile();
                                    }
                                } catch(Exception e) {
                                    continue;
                                }
                            } else {
                                data = path.toFile();
                            }
                            String[] paths = path.subpath(this.watchPath.getNameCount(), path.getNameCount()).toString().split(Pattern.quote(File.separator));
                            addResource(this.resourceTree, paths, data);
                        }                                                        
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        final Path p = path.resolve(event.context().toString());
                        Map.Entry<WatchKey, Path> entry = this.watchMap.entrySet().stream().filter(e -> e.getValue().equals(p)).findAny().orElse(null);
                        if(entry != null) {
                            this.watchMap.remove(entry.getKey());
                        }                            
                        String[] paths = p.subpath(this.watchPath.getNameCount(), p.getNameCount()).toString().split(Pattern.quote(File.separator));
                        removeResource(this.resourceTree, paths);
                    }                    
                }
                key.reset();
            }
            this.watchService.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load resource tree from specified Path
     * @param path
     * @param tree
     * @return
     * @throws IOException
     */
    protected Map<String, Object> loadResoureTree(Path path, Map<String, Object> tree) throws IOException {
        File[] files = path.toFile().listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                tree.put(file.getName(), loadResoureTree(file.toPath(), new LinkedHashMap<String, Object>()));
            } else {
                if(file.isFile()) {
                    if(filtering(file.getName(), this.accessFilters)) {
                        if(file.length() <= this.inMemoryLimitSize && filtering(file.getName(), this.inMemoryFilters)) {
                            tree.put(file.getName(), Files.readAllBytes(file.toPath()));
                        } else {
                            tree.put(file.getName(), file);
                        }
                    }        
                }
            }
        }
        return tree;
    }

    /**
     * Add resource to resurce tree
     * @param tree
     * @param res
     * @throws IOException
     */
    protected void addResource(Map<String, Object> tree, String[] res, Object data) throws IOException {
        if(!filtering(res[res.length-1], this.accessFilters)) {
            return;
        }        
        if(res.length == 1) {
            tree.put(res[0], data);
        } else {
            Object val = tree.get(res[0]);
            if(val == null) {
                tree.put(res[0], new LinkedHashMap<>());
            }
            addResource((Map<String, Object>)tree.get(res[0]), Arrays.copyOfRange(res, 1, res.length), data);
        }
    }

    /**
     * Remove resource
     * @param tree
     * @param res
     */
    protected void removeResource(Map<String, Object> tree, String[] res) {
        if(res.length == 1) {
            tree.remove(res[0]);
        } else {
            removeResource((Map<String, Object>)tree.get(res[0]), Arrays.copyOfRange(res, 1, res.length));
        }
    }

    /**
     * Get resource data
     * @param tree
     * @param res
     * @return
     */
    protected Object getResource(Map<String, Object> tree, String[] res) {
        if(res.length == 1) {
            return tree.get(res[0]);
        } else {
            return getResource((Map<String, Object>)tree.get(res[0]), Arrays.copyOfRange(res, 1, res.length));
        }
    }

    /**
     * Filtering resources
     * @param resourceName
     * @param filters
     * @return
     */
    public boolean filtering(String resourceName, List<String> filters) {
        return filters.stream().anyMatch(f -> !f.trim().equals("") && resourceName.matches(Arrays.asList(f.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*"));
    }

    @Override
    public Object getResource(Path resourcePath) {
        Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
        LoggerFactory.getLogger(this.host).debug("WATCH PATH: "+this.watchPath.toString()+"  REQUEST RESOURCE: "+path.toString());
        String[] paths = path.toString().split(Pattern.quote(File.separator));
        return getResource(this.resourceTree, paths);
    }

    @Override
    public boolean exists(Path resourcePath) {
        return resourcePath.toFile().exists();
    }

    @Override
    public String getWelcomePage(Map<String, Object> params) throws IOException {
        return getStaticPage(HostsManager.get().getHosts(this.host).getWelcomeFile().getName(), params);
    }

    @Override
    public String getResponsePage(Map<String, Object> params) throws IOException {
        return getStaticPage("response.html", params);
    }

    @Override
    public String getErrorPage(Map<String, Object> params) throws IOException {
        return getStaticPage("error.html", params);
    }

    @Override
    public String getStaticPage(String resourcePath, Map<String, Object> params) throws IOException {        
        Object data = getStaticContent(resourcePath);
        String page = "";
        if(data instanceof byte[]) {
            page = new String((byte[])data, HostsManager.get().getHosts(this.host).charset());            
        } else if(data instanceof File) {
            page = Files.readString(((File)data).toPath());
        } else {
            return null;
        }       
        for(Entry<String, Object> e : params.entrySet()) {
            page = page.replace(e.getKey(), e.getValue()+"");
        }
        return page;
    }

    @Override
    public String getResourcePage(Map<String, Object> params) throws IOException {        
        return getStaticPage("resource.html", params);
    }

    @Override
    public Object getStaticContent(String contextPath) throws IOException {
        Path path = this.watchPath.resolve(contextPath);        
        return getResource(path);
    }
}
