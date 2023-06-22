package org.chaostocosmos.leap.resource;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystemException;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.common.Filtering;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.common.SIZE;
import org.chaostocosmos.leap.common.TIME;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;

import com.google.gson.Gson;

/**
 * WatchResource object
 * 
 * @author 9ins
 */
public class WatchResources extends Thread implements ResourcesModel {    
    /**
     * host ID
     */
    String hostId;
    /**
     * Host object
     */
    Host<?> host;
    /**
     * Watch path
     */
    Path watchPath;
    /**
     * Watch kind
     */
    Kind<?>[] watchKind;
    /**
     * Filters
     */
    Filtering accessFiltering, forbiddenFiltering, inMemoryFiltering;
    /**
     * In-Memory resource limit size
     */
    long inMemoryLimitSize;
    /**
     * Resource root object
     */
    Resource resourceTree;
    /**
     * Watch path Map
     */
    Map<WatchKey, Path> watchMap;
    /**
     * Watch service object
     */
    WatchService watchService;
    /**
     * Gson object
     */
    Gson gson = new Gson();

    /**
     * Create with params
     * @param host
     * @param watchKinds
     * @throws IOException
     * @throws InterruptedException
     */
    public WatchResources(Host<?> host, Kind<?>[] watchKinds) throws IOException, InterruptedException {
        this(host, watchKinds, Constants.IN_MEMORY_LIMIT_SIZE);
    }

    /**
     * Create with params
     * @param hosts
     * @param watchKinds
     * @param inMemoryFilters
     * @throws IOException
     * @throws InterruptedException
     */
    public WatchResources(Host<?> host, Kind<?>[] watchKinds, long inMemoryLimitSize) throws IOException, InterruptedException {
        this.host = host;
        this.watchKind = watchKinds;
        this.inMemoryLimitSize = inMemoryLimitSize;
        this.hostId = host.getHostId();
        this.watchPath = host.getStatic().normalize();
        this.accessFiltering = host.getAccessFiltering();
        this.accessFiltering.addFilter(host.getWelcomeFile().getName());
        this.forbiddenFiltering = host.getForbiddenFiltering();
        this.inMemoryFiltering = host.getInMemoryFiltering();
        this.resourceTree = new Resource(true, this.watchPath, false, this.host.getInMemorySplitUnit()); 
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchMap = Files.walk(this.watchPath).sorted().filter(p -> p.toFile().isDirectory()).map(p -> {
            try {
                return new Object[]{ p.register(this.watchService, this.watchKind), p };
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(arr -> arr != null).collect(Collectors.toMap(k -> (WatchKey)k[0], v -> (Path)v[1]));
        // Load host resources
        long startMillis = System.currentTimeMillis();
        //this.resourceTree = loadResoureTree(this.watchPath, this.resourceTree);
        this.resourceTree = loadForkJoinResources();
        this.host.getLogger().info("[RESOURCE-LOAD] Host "+this.host.getHost()+" is complated: "+TIME.SECOND.duration(System.currentTimeMillis() - startMillis, TimeUnit.SECONDS));        
        //Have to set WatchResource to Hosts
        Context.get().hosts().getHost(this.hostId).setResource(this);
        //Start watch thread
        start();
    }

    /**
     * Load resources by ForkJoin framework
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected synchronized Resource loadForkJoinResources() throws IOException, InterruptedException {
        ForkJoinPool pool = new ForkJoinPool((int)((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors());
        ResourceLoadProcessor proc = new ResourceLoadProcessor(new Resource(true, this.watchPath, false, this.host.getInMemorySplitUnit()));
        pool.execute(proc);
        while(!proc.isDone()) {
            TimeUnit.SECONDS.sleep(1);
        }
        pool.shutdown();
        return proc.join();
    }

    /**
     * ResourceLoadProcessor
     * 
     * @author 9ins
     */
    public class ResourceLoadProcessor extends RecursiveTask<Resource> {
        /**
         * ResourceInfo  
         */        
        private Resource res;

        /**
         * Constructe with ResourceInfo object
         * 
         * @param res
         */
        public ResourceLoadProcessor(Resource res) {
            this.res = res;
        }

        @Override
        protected Resource compute() {
            try {
                File[] files = this.res.getFile().listFiles();
                for(File file : files) {
                    if(file.isDirectory()) {
                        Resource fileRes = new Resource(true, file.toPath(), false, host.getInMemorySplitUnit());    
                        ResourceLoadProcessor task = new ResourceLoadProcessor(fileRes);
                        task.fork();
                        this.res.put(file.getName(), task.join());
                    } else {
                        String fileSize = (float) SIZE.MB.get(file.length())+" MB";
                        if(forbiddenFiltering.exclude(file.getName())) {
                            if(file.length() <= inMemoryLimitSize && inMemoryFiltering.include(file.getName())) {
                                this.res.put(file.getName(), new Resource(false, file.toPath(), true, host.getInMemorySplitUnit()));
                                LoggerFactory.getLogger(hostId).debug("[HOST:"+hostId+"][LOAD] In-Memory resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                            } else {
                                this.res.put(file.getName(), new Resource(false, file.toPath(), false, host.getInMemorySplitUnit()));
                                LoggerFactory.getLogger(hostId).debug("[HOST:"+hostId+"][LOAD] File resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                            }
                        } else {
                            LoggerFactory.getLogger(hostId).debug("[HOST:"+hostId+"][NOT-INCLUDED] Not included in access resources: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                        }                    
                    }
                }    
            } catch(Exception e) {
                e.printStackTrace();
            }
            return this.res;
        }            
    }    

    @Override
    public void run() {
        try {
            while(this.watchService != null) {
                final WatchKey key = this.watchService.take();
                for(WatchEvent<?> event : key.pollEvents()) {
                    final Path eventPath = this.watchMap.get(key);
                    final long size = eventPath.toFile().length();
                    //LoggerFactory.getLogger(this.hostId).debug("[WATCH EVENT] KIND: "+event.kind()+"   Context: "+event.context()+"   Path: "+this.watchMap.get(key)+"   CNT: "+event.count());
                    if(event.kind() == StandardWatchEventKinds.OVERFLOW || eventPath == null) {
                        continue;
                    } 
                    if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path pathCreated = eventPath.resolve(event.context().toString());
                        LoggerFactory.getLogger(this.hostId).debug("[RESOURCE EVENT] KIND: "+event.kind()+" EVENT RESOURCE SIZE: "+SIZE.MB.get(pathCreated.toFile().length())+"  TOTAL RESOURCE SIZE: "+getResourceTotalSize(SIZE.MB));
                        Resource data = null;
                        if(pathCreated.toFile().isDirectory()) {
                            LoggerFactory.getLogger(this.hostId).debug("[RESOURCE CREATED] Directory resource created: "+pathCreated.toAbsolutePath());
                            this.watchMap.put(pathCreated.register(this.watchService, this.watchKind), pathCreated);
                            data = new Resource(true, pathCreated, false, this.host.getInMemorySplitUnit());
                        } else {
                            if(this.forbiddenFiltering.exclude(pathCreated.toFile().getName())) {                                
                                try {
                                    if(this.inMemoryFiltering.include(pathCreated.toFile().getName())) {
                                        //When In-Memory resource
                                        Files.move(pathCreated, pathCreated, StandardCopyOption.ATOMIC_MOVE);                                        
                                        if(pathCreated.toFile().length() <= this.inMemoryLimitSize) {
                                            data = new Resource(false, pathCreated, true, this.host.getInMemorySplitUnit());
                                            LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY CREATED LOADING] Loaded to memory: "+pathCreated.toAbsolutePath()+"  [SIZE: "+SIZE.MB.get(size)+"]");
                                        } else {
                                            //When In-Memory limit and reject
                                            data = new Resource(false, pathCreated, false, this.host.getInMemorySplitUnit());
                                            LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY CREATED LOADING] In-Memory file size overflow. Limit: "+SIZE.MB.get(this.inMemoryLimitSize)+"  File size: "+SIZE.MB.get(pathCreated.toFile().length()));
                                        }
                                    } else {                        
                                        // When File resource
                                        data = new Resource(false, pathCreated, false, this.host.getInMemorySplitUnit());                                
                                    }
                                } catch(FileSystemException e) {
                                    LoggerFactory.getLogger(this.hostId).warn("[FILE SYSTEM ALERT] "+e.getMessage());
                                    continue;
                                }
                            }
                        }
                        String[] paths = pathCreated.subpath(this.watchPath.getNameCount(), pathCreated.getNameCount()).toString().split(Pattern.quote(File.separator));
                        addResource(this.resourceTree, paths, data);
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path pathModified = eventPath.resolve(event.context().toString());
                        if(pathModified.toFile().isFile() && this.forbiddenFiltering.exclude(pathModified.toFile().getName())) {
                            Resource data  = null;
                            try {
                                //When In-Memory resource
                                if(this.inMemoryFiltering.include(pathModified.toFile().getName())) {
                                    Files.move(pathModified, pathModified, StandardCopyOption.ATOMIC_MOVE);
                                    if(pathModified.toFile().length() <= this.inMemoryLimitSize) {
                                        LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY MODIFIED LOADING] Loaded to memory: "+pathModified.toAbsolutePath()+"  [SIZE: "+SIZE.MB.get(pathModified.toFile().length())+"]");
                                        data = new Resource(false, pathModified, true, this.host.getInMemorySplitUnit());
                                    } else {
                                        //When In-Memory limit and reject
                                        LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY MODIFIED LOADING] In-Memory file size overflow. Limit: "+SIZE.MB.get(this.inMemoryLimitSize)+"  File size: "+SIZE.MB.get(pathModified.toFile().length()));
                                        data = new Resource(false, pathModified, false, this.host.getInMemorySplitUnit());
                                    }
                                } else {
                                    // When File resource   
                                    LoggerFactory.getLogger(this.hostId).debug("[EXCLUDE IN-MEMORY FILTER] Exclude on In-Memory resources: "+pathModified.toAbsolutePath());
                                    data = new Resource(false, pathModified, false, this.host.getInMemorySplitUnit());
                                }
                            } catch(FileSystemException e) {
                                //e.printStackTrace();
                                //LoggerFactory.getLogger(this.hostId).warn("[FILE SYSTEM ALERT] "+e.getMessage());
                                continue;
                            }
                            String[] paths = pathModified.subpath(this.watchPath.getNameCount(), pathModified.getNameCount()).toString().split(Pattern.quote(File.separator));
                            addResource(this.resourceTree, paths, data);
                        }
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        final Path pathDelete = eventPath.resolve(event.context().toString());
                        Map.Entry<WatchKey, Path> entry = this.watchMap.entrySet().stream().filter(e -> e.getValue().equals(pathDelete)).findAny().orElse(null);
                        if(entry != null) {
                            this.watchMap.remove(entry.getKey());
                        }
                        String[] paths = pathDelete.subpath(this.watchPath.getNameCount(), pathDelete.getNameCount()).toString().split(Pattern.quote(File.separator));
                        removeResource(this.resourceTree, paths);
                    }
                }
                key.reset();
            }
            this.watchService.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load resource tree from specified Path
     * @param path
     * @param tree
     * @return
     * @throws IOException
     * @throws ImageProcessingException
     */
    protected Resource loadResoureTree(Path path, Resource tree) throws IOException {
        File[] files = path.toFile().listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                tree.put(file.getName(), loadResoureTree(file.toPath(), new Resource(true, file.toPath(), false, this.host.getInMemorySplitUnit())));
            } else {
                if(file.isFile()) {
                    String fileSize = (float)SIZE.MB.get(file.length())+" MB";
                    if(this.forbiddenFiltering.exclude(file.getName())) {
                        if(file.length() <= this.inMemoryLimitSize && this.inMemoryFiltering.include(file.getName())) {
                            //System.out.println(file.getAbsolutePath());
                            tree.put(file.getName(), new Resource(false, file.toPath(), true, this.host.getInMemorySplitUnit()));
                            LoggerFactory.getLogger(this.hostId).debug("[HOST:"+this.hostId+"][LOAD] In-Memory resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                        } else {
                            tree.put(file.getName(), new Resource(false, file.toPath(), false, this.host.getInMemorySplitUnit()));
                            LoggerFactory.getLogger(this.hostId).debug("[HOST:"+this.hostId+"][LOAD] File resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                        }
                    } else {
                        LoggerFactory.getLogger(this.hostId).debug("[HOST:"+this.hostId+"][NOT-INCLUDED] Not included in access resources: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                    }
                }
            }
        }
        return tree;
    }

    @Override
    public void addResource(Path resourcePath) throws Exception {
        if(this.accessFiltering.include(resourcePath.toFile().getName()) && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {            
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, true, this.host.getInMemorySplitUnit()));
                LoggerFactory.getLogger(this.hostId).debug("[ADD IN-MEMORY RESOURCE] Resource Path: "+resourcePath.toString());
            } else {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, false, this.host.getInMemorySplitUnit()));
                LoggerFactory.getLogger(this.hostId).debug("[ADD FILE RESOURCE] Resource Path: "+resourcePath.toString());
            }
        } else {
            LoggerFactory.getLogger(this.hostId).debug("[RESOURCE REGISTRATION FAIL] Specified resource registration fail: "+resourcePath);
        }
    }

    @Override
    public void addResource(Path resourcePath, byte[] resourceRawData, boolean inMemoryFlag) throws Exception {
        if(this.accessFiltering.include(resourcePath.toFile().getName()) && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, resourceRawData, inMemoryFlag, this.host.getInMemorySplitUnit()));
                LoggerFactory.getLogger(this.hostId).debug("[ADD IN-MEMORY RESOURCE] Resource Path: "+resourcePath.toString());
            } else {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, resourceRawData, inMemoryFlag, this.host.getInMemorySplitUnit()));
                LoggerFactory.getLogger(this.hostId).debug("[ADD FILE RESOURCE] Resource Path: "+resourcePath.toString());
            }
        } else {
            LoggerFactory.getLogger(this.hostId).debug("[RESOURCE REGISTRATION FAIL] Specified raw resource registration fail: "+resourcePath);
        }
    }

    @Override
    public void removeResource(Path resourcePath) throws Exception {
        String subPath = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString();
        removeResource(this.resourceTree, subPath.split(Pattern.quote(File.separator)));
    }

    @Override
    public Resource getResource(Path resourcePath) throws Exception {
        resourcePath = resourcePath.normalize().toAbsolutePath();
        if(resourcePath.getNameCount() == watchPath.getNameCount()) {
            return this.resourceTree;
        } else if(resourcePath.getNameCount() < watchPath.getNameCount()) {
            throw new LeapException(HTTP.RES403, "Requested path is not allowed.");
        }
        return getResource(this.resourceTree, resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString().split(Pattern.quote(File.separator)));
    }

    @Override
    public byte[] getResourceData(Path resourcePath, long position, int length) throws Exception {        
        return getResource(resourcePath).getBytes1(position, length);
    }

    @Override
    public byte[] getFilePartial(Path resourcePath, long position, int length) throws Exception {
        return getResource(resourcePath).getFilePartial(position, length);
    }   

    @Override
    public Resource filter(MIME mimeType) throws IOException {
        return filterResourceTree(this.resourceTree, mimeType);
    }

    @Override
    public Path resolveRealPath(String contextPath) {
        Path staticPath = this.host.getStatic();
        contextPath = contextPath.charAt(0) == '/' ? contextPath.substring(0) : contextPath;
        contextPath = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.lastIndexOf('/')) : contextPath;
        return staticPath.resolve(contextPath);
    }

    @Override
    public Resource getContextResource(String contextPath) throws Exception { 
        return getResource(this.watchPath.resolve(contextPath));
    }

    @Override
    public String getContextPath(Path resourcePath) {
        resourcePath = resourcePath.normalize().toAbsolutePath();
        return resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString().replace("\\", "/");
    }

    @Override
    public String getStaticPage(String contextPath, Map<String, Object> params) throws Exception {                
        Resource resourceInfo = getContextResource(contextPath);
        if(resourceInfo == null) {
            throw new LeapException(HTTP.RES404, " Static page not found in Resource manager: "+contextPath);
        }
        String page = new String((resourceInfo).getBytes(), Context.get().hosts().getHost(this.hostId).<String> charset());
        if(params != null) {
            page = resolvePage(page, params);
        }
        return page;
    }

    @Override
    public String getWelcomePage(Map<String, Object> params) throws Exception {
        return getStaticPage(Context.get().hosts().getHost(this.hostId).getWelcomeFile().getName(), params);
    }

    @Override
    public String getTemplatePage(String templatePath, Map<String, Object> params) throws Exception {
        String page = getStaticPage(templatePath, params);
        if(params != null) {
            for(Entry<String, Object> e : params.entrySet()) {
                page = page.replace(e.getKey(), e.getValue()+"");
            }
        }
        return page;
    }

    @Override
    public String getResponsePage(Map<String, Object> params) throws Exception {
        return getTemplatePage("templates/response.html", params);
    }

    @Override
    public String getErrorPage(Map<String, Object> params) throws Exception {
        return getTemplatePage("templates/error.html", params);
    }

    @Override
    public String getResourcePage(Map<String, Object> params) throws Exception {        
        return getTemplatePage("templates/resource.html", params);
    }

    @Override
    public String resolvePage(String html, Map<String, Object> params) {
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            String regex = Constants.TAG_REGEX_PREFIX+entry.getKey()+Constants.TAG_REGEX_SUFFIX;
            Pattern ptrn = Pattern.compile(regex);
            Matcher matcher = ptrn.matcher(html);
            while(matcher.find()) {
                html = html.replace(matcher.group(), entry.getValue()+"");
            }
        }
        return html;
    }

    @Override
    public boolean exists(Path resourcePath) {
        try {
            Resource resourceInfo = getResource(resourcePath.normalize());
            if(resourceInfo != null || resourcePath.toFile().exists()) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    @Override
    public boolean isInMemory(Path resourcePath) {
        Resource resInfo = null;
        try {
            resInfo = getResource(resourcePath.normalize());
        } catch (Exception e) {
            return false;
        }
        return resInfo == null ? false : resInfo.isInMemory();
    }

    /**
     * Add resource to resource tree
     * @param tree
     * @param res
     * @throws IOException
     */
    protected synchronized void addResource(Resource resourceTree, String[] res, Resource data) throws IOException {        
        if(res.length == 1) {            
            resourceTree.put(res[0], data);
        } else {
            Resource val = (Resource)resourceTree.get(res[0]);
            if(val == null) {
                resourceTree.put(res[0], new Resource(true, this.watchPath.resolve(res[0]), false, this.host.getInMemorySplitUnit()));
            }
            addResource((Resource)resourceTree.get(res[0]), Arrays.copyOfRange(res, 1, res.length), data);
        }
    }

    /**
     * Filtering resource tree and get filtered resource List
     * @param resourceTree
     * @param mimeType
     * @return
     * @throws IOException
     */
    private Resource filterResourceTree(Resource resourceTree, MIME mimeType) throws IOException {
        Resource infos = new Resource(true, resourceTree.getPath(), false, this.host.getInMemorySplitUnit());
        for(Object obj : resourceTree.values()) {
            Resource info = (Resource) obj;
            if(!info.isNode()) {
                if(info.getMimeType() == mimeType) {
                    infos.put(info.getContextPath(), info);
                }
            } else {
                infos.putAll(filterResourceTree(info, mimeType));
            }
        }
        return infos;
    }

    /**
     * Get resource data
     * @param tree
     * @param res
     * @return
     */
    private final Resource getResource(Resource tree, String[] res) {
        if(res.length == 1) {
            return tree.get(res[0]);
        } else {
            return getResource(tree.get(res[0]), Arrays.copyOfRange(res, 1, res.length));
        }
    }

    /**
     * Remove resource
     * @param tree
     * @param res
     */
    private final void removeResource(Resource resourceTree, String[] res) {
        if(res.length == 1) {
            resourceTree.remove(res[0]);
        } else {
            removeResource((Resource)resourceTree.get(res[0]), Arrays.copyOfRange(res, 1, res.length));
        }
    }

    /**
     * Get resource total size
     * @param sizeUnit
     * @return
     */
    public double getResourceTotalSize(SIZE sizeUnit) {
        return sizeUnit.get(getResourceTotalSize(this.resourceTree));
    }

    /**
     * Get total resource size
     * @param res
     * @return
     */
    public long getResourceTotalSize(Resource tree) {
        long size = 0;
        for(Object obj : tree.values()) {
            Resource info = (Resource) obj;
            if(!info.isNode()) {
                size += info.getResourceSize();
            } else {
                size += getResourceTotalSize(info);
            } 
        }
        return size;
    }
}
