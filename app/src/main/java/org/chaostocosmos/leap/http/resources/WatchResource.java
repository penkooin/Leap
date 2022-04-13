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
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.Unit;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * WatchResource object
 * 
 * @author 9ins
 */
public class WatchResource extends Thread implements Resource {
    String host;
    Path watchPath;
    Kind<?>[] watchKind;
    Filtering accessFiltering, forbiddenFiltering, inMemoryFiltering;
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
        this(hosts.getHost(), hosts.getStatic(), watchKinds, hosts.getAccessFiltering(), hosts.getForbiddenFiltering(), hosts.getInMemoryFiltering(), 1024 * 1000);
    }

    /**
     * Create with params
     * @param hosts
     * @param watchPath
     * @param watchKinds
     * @param accessFilters
     * @param forbiddenFilters
     * @param inMemoryFilters
     * @throws IOException
     */
    public WatchResource(String host, Path watchPath, Kind<?>[] watchKinds, Filtering accessFiltering, Filtering forbiddenFiltering, Filtering inMemoryFiltering, int inMemoryLimitSize) throws IOException {
        this.host = host;
        this.watchPath = watchPath;
        this.watchKind = watchKinds;
        this.accessFiltering = accessFiltering;
        this.forbiddenFiltering = forbiddenFiltering;
        this.inMemoryFiltering = inMemoryFiltering;
        this.inMemoryLimitSize = inMemoryLimitSize;
        this.resourceTree = new LinkedHashMap<>();
        this.accessFiltering = accessFiltering;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchMap = Files.walk(this.watchPath).sorted().filter(p -> p.toFile().isDirectory()).map(p -> {
            try {
                return new Object[]{ p.register(this.watchService, this.watchKind), p };
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
                    LoggerFactory.getLogger(this.host).debug("[WATCH EVENT] KIND: "+event.kind()+"   Context: "+event.context()+"   Path: "+this.watchMap.get(key)+"   CNT: "+event.count());
                    if(event.kind() == StandardWatchEventKinds.OVERFLOW || path == null) {
                        continue;
                    } 
                    if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        path = path.resolve(event.context().toString());
                        Object data = null;
                        if(path.toFile().isDirectory()) {
                            LoggerFactory.getLogger(this.host).debug("[RESOURCE CREATED] Directory resource created: "+path.toAbsolutePath());
                            this.watchMap.put(path.register(this.watchService, this.watchKind), path);
                            data = new LinkedHashMap<>();
                        } else {
                            if(this.accessFiltering.include(path.toFile().getName()) && this.forbiddenFiltering.exclude(path.toFile().getName())) {
                                if(this.inMemoryFiltering.include(path.toFile().getName())) {
                                    try {
                                        Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);
                                        if(path.toFile().length() <= this.inMemoryLimitSize) {
                                            LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] Loaded to memory: "+((byte[])data).length);
                                            data = Files.readAllBytes(path);
                                        } else {
                                            LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] In-Memory file size overflow. Limit: "+Unit.MB.get(this.inMemoryLimitSize, 2)+"  File size: "+Unit.MB.get(path.toFile().length(), 2));
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
                        if(path.toFile().isFile() && this.accessFiltering.include(path.toFile().getName())) {
                            Object data = null;
                            if(this.accessFiltering.include(path.toFile().getName()) && this.forbiddenFiltering.exclude(path.toFile().getName())) {
                                try {
                                    Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);                                    
                                    if(path.toFile().length() <= this.inMemoryLimitSize) {
                                        LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] Loaded to memory: "+((byte[])data).length);
                                        data = Files.readAllBytes(path);
                                    } else {
                                        LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] In-Memory file size overflow. Limit: "+Unit.MB.get(this.inMemoryLimitSize, 2)+"  File size: "+Unit.MB.get(path.toFile().length(), 2));
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
                    if(file.getParentFile().getName().equals("template") || this.accessFiltering.include(file.getName())) {
                        if(this.forbiddenFiltering.exclude(file.getName())) {
                            if(file.length() <= this.inMemoryLimitSize && this.inMemoryFiltering.include(file.getName())) {
                                tree.put(file.getName(), Files.readAllBytes(file.toPath()));
                                LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][LOAD] In-Memory resource loaded: "+file.getName());
                            } else {
                                tree.put(file.getName(), file);
                                LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][LOAD] File resource loaded: "+file.getName());
                            }    
                        } else {
                            LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][EXCLUDING] Forbidden resource: "+file.getName());
                        }
                    } else {
                        LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][NOT-INCLUDED] Not included in access resources: "+file.getName());
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
    private void addResource(Map<String, Object> tree, String[] res, Object data) throws IOException {        
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
    private Object getResource(Map<String, Object> tree, String[] res) {
        if(res.length == 1) {
            return tree.get(res[0]);
        } else {
            return getResource((Map<String, Object>)tree.get(res[0]), Arrays.copyOfRange(res, 1, res.length));
        }
    }

    @Override
    public void addResource(Path resourcePath) throws IOException {
        if(resourcePath.toFile().isFile() 
            && this.accessFiltering.include(resourcePath.toFile().getName()) 
            && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {
            
                Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                LoggerFactory.getLogger(this.host).debug("[ADD IN-MEMORY RESOURCE] Watch: "+this.watchPath.toString()+"  Resource: "+path.toString());
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), Files.readAllBytes(resourcePath));
            } else {
                LoggerFactory.getLogger(this.host).debug("[ADD FILE RESOURCE] Watch: "+this.watchPath.toString()+"  Resource: "+path.toString());
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), resourcePath);
            }
        }
    }

    @Override
    public Object getContextResource(String contextPath) throws IOException {
        return getResource(this.watchPath.resolve(contextPath));
    }

    @Override
    public Object getResource(Path resourcePath) {
        if(this.accessFiltering.include(resourcePath.toFile().getName()) && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {    
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            String[] paths = path.toString().split(Pattern.quote(File.separator));
            return getResource(this.resourceTree, paths);
        }
        throw new WASException(MSG_TYPE.ERROR, 20, resourcePath);
    }

    @Override
    public String getStaticPage(String contextPath, Map<String, Object> params) throws IOException {        
        Object data = getContextResource(contextPath);
        String page = "";
        if(data instanceof byte[]) {
            page = new String((byte[])data, HostsManager.get().getHosts(this.host).charset());
        } else if(data instanceof File) {
            page = Files.readString(((File)data).toPath());
        } else {
            new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.getCode(), " Static page not found in Resource manager.");
        }
        for(Entry<String, Object> e : params.entrySet()) {
            page = page.replace(e.getKey(), e.getValue()+"");
        }
        return page;
    }

    @Override
    public String getTemplatePage(String templateName, Map<String, Object> params) throws IOException {
        return getStaticPage("template/"+templateName, params);
    }

    @Override
    public String getWelcomePage(Map<String, Object> params) throws IOException {
        String page = getStaticPage(HostsManager.get().getHosts(this.host).getWelcomeFile().getName(), params);
        return page;
    }

    @Override
    public String getResponsePage(Map<String, Object> params) throws IOException {
        return getTemplatePage("response.html", params);
    }

    @Override
    public String getErrorPage(Map<String, Object> params) throws IOException {
        return getTemplatePage("error.html", params);
    }

    @Override
    public String getResourcePage(Map<String, Object> params) throws IOException {        
        return getTemplatePage("resource.html", params);
    }

    @Override
    public boolean exists(Path resourcePath) {
        return resourcePath.toFile().exists();
    }
}
