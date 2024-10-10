package org.chaostocosmos.leap.resource;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.enums.TIME;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.resource.config.ResourceConfig;
import org.chaostocosmos.leap.resource.filter.ResourceFilter;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;

import com.google.gson.Gson;

/**
 * WatchResource object
 * 
 * @author 9ins
 */
public class ResourceWatcher implements ResourcesWatcherModel {    

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
    ResourceFilter accessFiltering;
    
    /**
     * In-Memory filtering
     */
    ResourceFilter inMemoryFiltering;

    /**
     * In memory split unit
     */
    int inMemorySplitUnit;

    /**
     * In memory file size limit
     */
    int fileSizeLimit;

    /**
     * Read file buffer size
     */
    int fileReadBufferSize;

    /**
     * Write file buffer size
     */
    int fileWriteBufferSize;

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
     * Watch thread object
     */
    Thread watchThread;
    
    /**
     * Gson object
     */
    Gson gson = new Gson();

    /**
     * Constructor
     * @param watchPath
     * @param watchKinds
     * @param accessFiltering
     * @param inMemoryFiltering
     * @param inMemorySplitUnit
     * @param fileSizeLimit
     * @param fileReadBufferSize
     * @param fileWriteBufferSize
     * @param inMemoryLimitSize
     */
    public ResourceWatcher(
                            Path watchPath, 
                            Kind<?>[] watchKinds, 
                            ResourceFilter accessFiltering,
                            ResourceFilter inMemoryFiltering,
                            int inMemorySplitUnit,
                            int fileSizeLimit,
                            int fileReadBufferSize,
                            int fileWriteBufferSize,
                            long inMemoryLimitSize
                            ) {
        this.watchPath = watchPath.toAbsolutePath().normalize();
        this.watchKind = watchKinds;
        this.accessFiltering = accessFiltering;
        this.inMemoryFiltering = inMemoryFiltering;
        this.inMemorySplitUnit = inMemorySplitUnit;
        this.fileSizeLimit = fileSizeLimit;
        this.fileReadBufferSize = fileReadBufferSize;
        this.fileWriteBufferSize = fileWriteBufferSize;
        this.inMemoryLimitSize = inMemoryLimitSize;

        LoggerFactory.getLogger().info("[WATCH PATH] Watch Path: "+this.watchPath.toAbsolutePath().normalize().toString());
        try {
            this.resourceTree = new Resource(true, this.watchPath, false, this.inMemorySplitUnit);
            this.watchService = FileSystems.getDefault().newWatchService();
            this.watchMap = Files.walk(this.watchPath).sorted().filter(p -> p.toFile().isDirectory()).map(p -> {
                try {
                    return new Object[]{ p.register(this.watchService, this.watchKind), p };
                } catch (IOException e) {
                    LoggerFactory.getLogger().error(e.getMessage(), e);
                    return null;
                }
            }).filter(arr -> arr != null).collect(Collectors.toMap(k -> (WatchKey)k[0], v -> (Path)v[1]));

            // Load host resources
            long startMillis = System.currentTimeMillis();
            //this.resourceTree = loadResoureTree(this.watchPath, this.resourceTree);
            this.resourceTree = loadForkJoinResources(this.watchPath);
            LoggerFactory.getLogger().info("[RESOURCE-LOAD] Path: "+this.watchPath.toString()+" is complated: "+TIME.SECOND.duration(System.currentTimeMillis() - startMillis, TimeUnit.SECONDS));        
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } 
    }

    /**
     * Load resources by ForkJoin framework
     * @return
     * @throws Exception
     * @throws InterruptedException
     */
    protected Resource loadForkJoinResources(Path rootPath) throws InterruptedException, IOException {
        ForkJoinPool pool = new ForkJoinPool((int)((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors());
        ResourceLoadProcessor proc = new ResourceLoadProcessor(new Resource(true, rootPath, false, this.inMemorySplitUnit));
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
                        Resource fileRes = new Resource(true, file.toPath(), false, inMemorySplitUnit);    
                        ResourceLoadProcessor task = new ResourceLoadProcessor(fileRes);
                        task.fork();
                        this.res.put(file.getName(), task.join());
                    } else {
                        String fileSize = (float) SIZE.MB.get(file.length())+" MB";
                        if(accessFiltering.include(file.getName())) {
                            if(file.length() <= inMemoryLimitSize && inMemoryFiltering.include(file.getName())) {
                                LoggerFactory.getLogger().debug("[LOAD] In-Memory resource loading: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                                this.res.put(file.getName(), new Resource(false, file.toPath(), true, inMemorySplitUnit));
                            } else {
                                LoggerFactory.getLogger().debug("[LOAD] File resource loading: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                                this.res.put(file.getName(), new Resource(false, file.toPath(), false, inMemorySplitUnit));
                            }
                        } else {
                            LoggerFactory.getLogger().debug("[NOT-INCLUDED] Not included in access resources: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                        }                    
                    }
                }    
            } catch(Exception e) {
                LoggerFactory.getLogger().error(e.getMessage(), e);
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
                        LoggerFactory.getLogger().debug("[RESOURCE EVENT] KIND: "+event.kind()+" EVENT RESOURCE SIZE: "+SIZE.MB.get(pathCreated.toFile().length())+"  TOTAL RESOURCE SIZE: "+getResourceTotalSize(SIZE.MB));
                        Resource data = null;
                        if(pathCreated.toFile().isDirectory()) {
                            LoggerFactory.getLogger().debug("[RESOURCE CREATED] Directory resource created: "+pathCreated.toAbsolutePath());
                            this.watchMap.put(pathCreated.register(this.watchService, this.watchKind), pathCreated);
                            data = new Resource(true, pathCreated, false, this.inMemorySplitUnit);
                        } else {
                            if(this.accessFiltering.include(pathCreated.toFile().getName())) {                                
                                try {
                                    if(this.inMemoryFiltering.include(pathCreated.toFile().getName())) {
                                        //When In-Memory resource
                                        Files.move(pathCreated, pathCreated, StandardCopyOption.ATOMIC_MOVE);                                        
                                        if(pathCreated.toFile().length() <= this.inMemoryLimitSize) {
                                            LoggerFactory.getLogger().debug("[IN-MEMORY CREATED LOADING] Loading to memory: "+pathCreated.toAbsolutePath()+"  [SIZE: "+SIZE.MB.get(size)+"]");
                                            data = new Resource(false, pathCreated, true, this.inMemorySplitUnit);
                                        } else {
                                            //When In-Memory limit and reject
                                            LoggerFactory.getLogger().debug("[IN-MEMORY CREATED LOADING] In-Memory file size overflow. Limit: "+SIZE.MB.get(this.inMemoryLimitSize)+"  File size: "+SIZE.MB.get(pathCreated.toFile().length()));
                                            data = new Resource(false, pathCreated, false, this.inMemorySplitUnit);
                                        }
                                    } else {                        
                                        // When File resource
                                        data = new Resource(false, pathCreated, false, this.inMemorySplitUnit);
                                    }
                                } catch(FileSystemException e) {
                                    LoggerFactory.getLogger().warn("[FILE SYSTEM ALERT] "+e.getMessage());
                                    continue;
                                }
                            }
                        }
                        String[] paths = pathCreated.subpath(this.watchPath.getNameCount(), pathCreated.getNameCount()).toString().split(Pattern.quote(File.separator));
                        addResource(this.resourceTree, paths, data);
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path pathModified = eventPath.resolve(event.context().toString());
                        if(pathModified.toFile().isFile() && this.accessFiltering.include(pathModified.toFile().getName())) {
                            Resource data  = null;
                            try {
                                //When In-Memory resource
                                if(this.inMemoryFiltering.include(pathModified.toFile().getName())) {
                                    Files.move(pathModified, pathModified, StandardCopyOption.ATOMIC_MOVE);
                                    if(pathModified.toFile().length() <= this.inMemoryLimitSize) {
                                        data = new Resource(false, pathModified, true, this.inMemorySplitUnit);
                                        LoggerFactory.getLogger().debug("[IN-MEMORY MODIFIED LOADING] Loading to memory: "+pathModified.toAbsolutePath()+"  [SIZE: "+SIZE.MB.get(pathModified.toFile().length())+"]");
                                    } else {
                                        //When In-Memory limit and reject
                                        LoggerFactory.getLogger().debug("[IN-MEMORY MODIFIED LOADING] In-Memory file size overflow. Limit: "+SIZE.MB.get(this.inMemoryLimitSize)+"  File size: "+SIZE.MB.get(pathModified.toFile().length()));
                                        data = new Resource(false, pathModified, false, this.inMemorySplitUnit);
                                    }
                                } else {
                                    // When File resource   
                                    LoggerFactory.getLogger().debug("[EXCLUDE IN-MEMORY FILTER] Exclude on In-Memory resources: "+pathModified.toAbsolutePath());
                                    data = new Resource(false, pathModified, false, this.inMemorySplitUnit);
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
    protected Resource loadResoureTree(Path path, Resource tree) throws Exception {
        File[] files = path.toFile().listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                tree.put(file.getName(), loadResoureTree(file.toPath(), new Resource(true, file.toPath(), false, this.inMemorySplitUnit)));
            } else {
                if(file.isFile()) {
                    String fileSize = (float)SIZE.MB.get(file.length())+" MB";
                    if(this.accessFiltering.include(file.getName())) {
                        if(file.length() <= this.inMemoryLimitSize && this.inMemoryFiltering.include(file.getName())) {
                            //System.out.println(file.getAbsolutePath());
                            LoggerFactory.getLogger().debug("[LOAD] In-Memory resource loading: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                            tree.put(file.getName(), new Resource(false, file.toPath(), true, this.inMemorySplitUnit));
                        } else {
                            LoggerFactory.getLogger().debug("[LOAD] File resource loading: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                            tree.put(file.getName(), new Resource(false, file.toPath(), false, this.inMemorySplitUnit));
                        }
                    } else {
                        LoggerFactory.getLogger().debug("[NOT-INCLUDED] Not included in access resources: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                    }
                }
            }
        }
        return tree;
    }

    @Override
    public void addResource(Path resourcePath) throws Exception {
        if(this.accessFiltering.include(resourcePath.toFile().getName()) && this.accessFiltering.include(resourcePath.toFile().getName())) {            
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, true, this.inMemorySplitUnit));
                LoggerFactory.getLogger().debug("[ADD IN-MEMORY RESOURCE] Resource Path: "+resourcePath.toString());
            } else {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, false, this.inMemorySplitUnit));
                LoggerFactory.getLogger().debug("[ADD FILE RESOURCE] Resource Path: "+resourcePath.toString());
            }
        } else {
            LoggerFactory.getLogger().debug("[RESOURCE REGISTRATION FAIL] Specified resource registration fail: "+resourcePath);
        }
    }

    @Override
    public void addResource(Path resourcePath, byte[] resourceRawData, boolean inMemoryFlag) throws Exception {
        if(this.accessFiltering.include(resourcePath.toFile().getName()) && this.accessFiltering.include(resourcePath.toFile().getName())) {
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, resourceRawData, inMemoryFlag, this.inMemorySplitUnit));
                LoggerFactory.getLogger().debug("[ADD IN-MEMORY RESOURCE] Resource Path: "+resourcePath.toString());
            } else {
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new Resource(false, resourcePath, resourceRawData, inMemoryFlag, this.inMemorySplitUnit));
                LoggerFactory.getLogger().debug("[ADD FILE RESOURCE] Resource Path: "+resourcePath.toString());
            }
        } else {
            LoggerFactory.getLogger().debug("[RESOURCE REGISTRATION FAIL] Specified raw resource registration fail: "+resourcePath);
        }
    }

    @Override
    public void removeResource(Path resourcePath) {
        String subPath = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString();
        removeResource(this.resourceTree, subPath.split(Pattern.quote(File.separator)));
    }

    @Override
    public Resource getResource(Path resourcePath) throws IOException {
        resourcePath = resourcePath.toAbsolutePath().normalize();
        if(resourcePath.getNameCount() == watchPath.getNameCount()) {
            return this.resourceTree;
        } else if(resourcePath.getNameCount() < watchPath.getNameCount()) {
            throw new LeapException(HTTP.RES403, "Requested path is not allowed.");
        }
        Resource res = getResource(this.resourceTree, resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString().split(Pattern.quote(File.separator)));
        return res;
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
    public Resource filter(MIME mimeType) throws Exception {
        return filterResourceTree(this.resourceTree, mimeType);
    }

    @Override
    public Path resolveRealPath(String contextPath) throws Exception {
        contextPath = contextPath.charAt(0) == '/' ? contextPath.substring(0) : contextPath;
        contextPath = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.lastIndexOf('/')) : contextPath;
        return this.watchPath.resolve(contextPath);
    }

    @Override
    public Resource getContextResource(String contextPath) throws IOException {      
        return getResource(this.watchPath.resolve(contextPath.charAt(0) == '/' ? contextPath.substring(1) : contextPath));
    }

    @Override
    public String getContextPath(Path resourcePath) {
        resourcePath = resourcePath.normalize().toAbsolutePath();
        return resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString().replace("\\", "/");
    }

    @Override
    public boolean exists(Path resourcePath) {
        try {
            Resource resourceInfo = getResource(resourcePath.normalize());
            if(resourceInfo != null || resourcePath.toFile().exists()) {
                return true;
            }
        } catch (Exception e) {
            throw new LeapException(HTTP.RES404, e);
        }
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

    @Override
    public void terminate() throws IOException, InterruptedException {        
        this.watchService.close();
        this.watchService = null;
        this.watchThread.interrupt();
        this.watchThread.join();
    }

    @Override
    public void start() {
        this.watchThread = new Thread(this);
        this.watchThread.start();
    }

    /**
     * Add resource to resource tree
     * @param tree
     * @param res
     * @throws Exception
     */
    protected void addResource(Resource resourceTree, String[] res, Resource data) throws Exception {        
        if(res.length == 1) {            
            resourceTree.put(res[0], data);
        } else {
            Resource val = (Resource)resourceTree.get(res[0]);
            if(val == null) {
                resourceTree.put(res[0], new Resource(true, this.watchPath.resolve(res[0]), false, this.inMemorySplitUnit));
            }
            addResource((Resource)resourceTree.get(res[0]), Arrays.copyOfRange(res, 1, res.length), data);
        }
    }

    /**
     * Filtering resource tree and get filtered resource List
     * @param resourceTree
     * @param mimeType
     * @return
     * @throws Exception
     */
    private Resource filterResourceTree(Resource resourceTree, MIME mimeType) throws Exception {
        Resource infos = new Resource(true, resourceTree.getPath(), false, this.inMemorySplitUnit);
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
        if(tree == null) {
            return tree;
        }
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
     * @param resourceName
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
