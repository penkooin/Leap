package org.chaostocosmos.leap.http.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UNIT;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * WatchResource object
 * 
 * @author 9ins
 */
public class WatchResources extends Thread implements Resources {    
    String hostId;
    Host<?> host;
    Path watchPath;
    Kind<?>[] watchKind;
    Filtering accessFiltering, forbiddenFiltering, inMemoryFiltering;
    long inMemoryLimitSize;
    Map<String, Object> resourceTree;
    Map<WatchKey, Path> watchMap;
    WatchService watchService;
    Gson gson = new Gson();

    /**
     * Create with params
     * @param host
     * @param watchKinds
     * @throws IOException
     */
    public WatchResources(Host<?> host, Kind<?>[] watchKinds) throws IOException {
        this(host, watchKinds, Constants.IN_MEMORY_LIMIT_SIZE);
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
     * @throws ImageProcessingException
     */
    public WatchResources(Host<?> host, Kind<?>[] watchKinds, long inMemoryLimitSize) throws IOException {
        this.host = host;
        this.hostId = host.getHostId();
        this.watchPath = host.getStatic().normalize();
        this.accessFiltering = host.getAccessFiltering();
        this.accessFiltering.addFilter(host.getWelcomeFile().getName());
        this.forbiddenFiltering = host.getForbiddenFiltering();
        this.inMemoryFiltering = host.getInMemoryFiltering();
        this.watchKind = watchKinds;
        this.inMemoryLimitSize = inMemoryLimitSize;
        this.resourceTree = new LinkedHashMap<>();
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
        Context.getHosts().getHost(this.hostId).setResource(this);
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
                    long size = path.toFile().length();
                    //LoggerFactory.getLogger(this.hostId).debug("[WATCH EVENT] KIND: "+event.kind()+"   Context: "+event.context()+"   Path: "+this.watchMap.get(key)+"   CNT: "+event.count());
                    if(event.kind() == StandardWatchEventKinds.OVERFLOW || path == null) {
                        continue;
                    } 
                    if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        path = path.resolve(event.context().toString());
                        LoggerFactory.getLogger(this.hostId).debug("[RESOURCE EVENT] KIND: "+event.kind()+" EVENT RESOURCE SIZE: "+UNIT.MB.get(path.toFile().length())+"  TOTAL RESOURCE SIZE: "+getResourceTotalSize(UNIT.MB));

                        Object data = null;
                        if(path.toFile().isDirectory()) {
                            LoggerFactory.getLogger(this.hostId).debug("[RESOURCE CREATED] Directory resource created: "+path.toAbsolutePath());
                            this.watchMap.put(path.register(this.watchService, this.watchKind), path);
                            data = new LinkedHashMap<>();
                        } else {
                            if(this.forbiddenFiltering.exclude(path.toFile().getName())) {                                
                                try {
                                    if(this.inMemoryFiltering.include(path.toFile().getName())) {
                                        //When In-Memory resource
                                        Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);                                        
                                        if(path.toFile().length() <= this.inMemoryLimitSize) {
                                            data = new ResourceInfo(path, true);
                                            LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY CREATED] Loaded to memory: "+path.toAbsolutePath()+"  [SIZE: "+UNIT.MB.get(size)+"]");
                                        } else {
                                            //When In-Memory limit and reject
                                            data = new ResourceInfo(path, false);
                                            LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY CREATED] In-Memory file size overflow. Limit: "+UNIT.MB.get(this.inMemoryLimitSize)+"  File size: "+UNIT.MB.get(path.toFile().length()));
                                        }
                                    } else {                        
                                        // When File resource            
                                        data = new ResourceInfo(path, false);                                
                                    }
                                } catch(FileSystemException e) {
                                    LoggerFactory.getLogger(this.hostId).warn("[FILE SYSTEM ALERT] "+e.getMessage());
                                    continue;
                                }
                            }
                        }
                        String[] paths = path.subpath(this.watchPath.getNameCount(), path.getNameCount()).toString().split(Pattern.quote(File.separator));
                        addResource(this.resourceTree, paths, data);
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        path = path.resolve(event.context().toString());
                        if(path.toFile().isFile() && this.forbiddenFiltering.exclude(path.toFile().getName())) {
                            Object data = null;
                            try {
                                //When In-Memory resource
                                if(this.inMemoryFiltering.include(path.toFile().getName())) {
                                    Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);
                                    if(path.toFile().length() <= this.inMemoryLimitSize) {
                                        LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY CREATED] Loaded to memory: "+path.toAbsolutePath()+"  [SIZE: "+UNIT.MB.get(path.toFile().length())+"]");
                                        data = new ResourceInfo(path, true);
                                    } else {
                                        //When In-Memory limit and reject
                                        LoggerFactory.getLogger(this.hostId).debug("[IN-MEMORY CREATED] In-Memory file size overflow. Limit: "+UNIT.MB.get(this.inMemoryLimitSize)+"  File size: "+UNIT.MB.get(path.toFile().length()));
                                        data = new ResourceInfo(path, false);
                                    }
                                } else {
                                    // When File resource   
                                    LoggerFactory.getLogger(this.hostId).debug("[EXCLUDE IN-MEMORY FILTER] Exclude on In-Memory resources: "+path.toAbsolutePath());
                                    data = new ResourceInfo(path, false);
                                }
                            } catch(FileSystemException e) {
                                LoggerFactory.getLogger(this.hostId).warn("[FILE SYSTEM ALERT] "+e.getMessage());
                                continue;
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
     * @throws ImageProcessingException
     */
    protected Map<String, Object> loadResoureTree(Path path, Map<String, Object> tree) throws IOException {
        File[] files = path.toFile().listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                tree.put(file.getName(), loadResoureTree(file.toPath(), new LinkedHashMap<String, Object>()));
            } else {
                if(file.isFile()) {
                    String fileSize = (float)UNIT.MB.get(file.length())+" MB";
                    if(this.forbiddenFiltering.exclude(file.getName())) {
                        if(file.length() <= this.inMemoryLimitSize && this.inMemoryFiltering.include(file.getName())) {
                            //System.out.println(file.getAbsolutePath());
                            tree.put(file.getName(), new ResourceInfo(file.toPath(), true));
                            LoggerFactory.getLogger(this.hostId).debug("[HOST:"+this.hostId+"][LOAD] In-Memory resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                        } else {
                            tree.put(file.getName(), new ResourceInfo(file.toPath(), false));
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
     * Filtering resource tree and get filtered resource List
     * @param resourceTree
     * @param mimeType
     * @return
     */
    private Map<String, Object> filterResourceTree(Map<String, Object> resourceTree, MIME_TYPE mimeType) {
        Map<String, Object> infos = new HashMap<String, Object>();
        for(Object obj : resourceTree.values()) {
            if(obj instanceof ResourceInfo) {
                ResourceInfo info = (ResourceInfo) obj;
                if(info.getMimeType() == mimeType) {
                    infos.put(info.getContextPath(), info);
                }
            } else if(obj instanceof Map) {
                infos.putAll(filterResourceTree((Map<String, Object>)obj, mimeType));
            }
        }
        return infos;
    }

    /**
     * Filtering and get resource List by mime-type
     * @param mimeType
     * @return
     */
    public Map<String, Object> filter(MIME_TYPE mimeType) {
        return filterResourceTree(this.resourceTree, mimeType);
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

    /**
     * Get resource total size
     * @param sizeUnit
     * @return
     */
    public double getResourceTotalSize(UNIT sizeUnit) {
        return sizeUnit.get(getResourceTotalSize(resourceTree));
    }

    /**
     * Get total resource size
     * @param res
     * @return
     */
    public long getResourceTotalSize(Map res) {
        long size = 0;
        for(Object obj : res.values()) {
            if(obj instanceof ResourceInfo) {
                size += ((ResourceInfo)obj).getResourceSize();
            } else if (obj instanceof Map) {
                size += getResourceTotalSize((Map)obj);
            } else {
                throw new IllegalArgumentException("Resource isn't allowed this type: "+obj.getClass().getName());
            }
        }
        return size;
    }

    @Override
    public void addResource(Path resourcePath) throws IOException { 
        if(resourcePath.toFile().isFile() && this.accessFiltering.include(resourcePath.toFile().getName()) && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {            
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                LoggerFactory.getLogger(this.hostId).debug("[ADD IN-MEMORY RESOURCE] Watch: "+this.watchPath.toString()+"  Resource: "+path.toString());
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new ResourceInfo(path, true));
            } else {
                LoggerFactory.getLogger(this.hostId).debug("[ADD FILE RESOURCE] Watch: "+this.watchPath.toString()+"  Resource: "+path.toString());
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new ResourceInfo(path, false));
            }
        }
    }

    @Override
    public Path resolveRealPath(String contextPath) {
        Path staticPath = this.host.getStatic();
        contextPath = contextPath.charAt(0) == '/' ? contextPath.substring(0) : contextPath;
        contextPath = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.lastIndexOf('/')) : contextPath;
        return staticPath.resolve(contextPath);
    }

    @Override
    public Object getContextResourceInfo(String contextPath) throws IOException { 
        return getResourceInfo(this.watchPath.resolve(contextPath));
    }

    @Override
    public String getContextPath(Path resourcePath) {
        return resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount()).toString().replace("\\", "/");
    }

    @Override
    public Object getResourceInfo(Path resourcePath) {
        resourcePath = resourcePath.normalize();
        if(resourcePath.getNameCount() == watchPath.getNameCount()) {
            return this.resourceTree;
        } else if(resourcePath.getNameCount() < watchPath.getNameCount()) {
            throw new WASException(MSG_TYPE.HTTP, 403, "Requested path is not allowed.");
        }
        Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());        
        String[] paths = path.toString().split(Pattern.quote(File.separator));
        return getResource(this.resourceTree, paths);
    }

    @Override
    public String getStaticPage(String contextPath, Map<String, Object> params) throws IOException {                
        Object resourceInfo = getContextResourceInfo(contextPath);
        if(resourceInfo == null) {            
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), " Static page not found in Resource manager: "+contextPath);
        }
        String page = new String(((ResourceInfo)resourceInfo).getBytes(), Context.getHosts().getHost(this.hostId).charset());
        if(params != null) {
            page = resolvePage(page, params);
        }
        return page;
    }

    @Override
    public String getWelcomePage(Map<String, Object> params) throws IOException {
        return getStaticPage(Context.getHosts().getHost(this.hostId).getWelcomeFile().getName(), params);
    }

    @Override
    public String getTemplatePage(String templatePath, Map<String, Object> params) throws IOException {
        String page = getStaticPage(templatePath, params);
        if(params != null) {
            for(Entry<String, Object> e : params.entrySet()) {
                page = page.replace(e.getKey(), e.getValue()+"");
            }    
        }
        return page;
    }

    @Override
    public String getResponsePage(Map<String, Object> params) throws IOException {
        return getTemplatePage("templates/response.html", params);
    }

    @Override
    public String getErrorPage(Map<String, Object> params) throws IOException {
        return getTemplatePage("templates/error.html", params);
    }

    @Override
    public String getResourcePage(Map<String, Object> params) throws IOException {        
        return getTemplatePage("templates/resource.html", params);
    }

    @Override
    public String resolvePage(String html, Map<String, Object> params) {
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            html = html.replaceAll(Constants.TAG_REGEX_PREFIX+entry.getKey()+Constants.TAG_REGEX_SUFFIX, entry.getValue()+"");
        }
        return html;
    }

    @Override
    public boolean exists(Path resourcePath) {
        return resourcePath.toFile().exists();
    }

    /**
     * ResourceInfo
     * 
     * @author 9ins
     */
    public class ResourceInfo {
        /**
         * Mapping context path
         */
        String contextPath;
        /**
         * Resource path
         */
        Path resourcePath;
        /**
         * In-Memory flag 
         */
        boolean inMemoryFlag;
        /**
         * Resource data List
         */
        List<byte[]> resourceData;
        /**
         * Resource mime-type  
         */        
        MIME_TYPE mimeType;
        /**
         * Resource last modified time
         */
        FileTime lastModified;
        /**
         * Resource File
         */
        File resourceFile;
        /**
         * Resource size
         */
        long resourceSize;
        /**
         * Constructs with resource path & in-memory flag
         * @param resourcePath
         * @param inMemoryFlag
         * @throws IOException
         * @throws ImageProcessingException
         */
        public ResourceInfo(Path resourcePath, boolean inMemoryFlag) throws IOException {
            this.resourcePath = resourcePath;
            this.resourceFile = resourcePath.toFile();
            this.resourceSize = resourcePath.toFile().length();
            this.inMemoryFlag = inMemoryFlag;
            this.mimeType = MIME_TYPE.mimeType(Files.probeContentType(this.resourcePath));
            if(this.inMemoryFlag) {
                this.resourceData = new ArrayList<>();
                int splitSize = Context.getHosts().getHost(hostId).getInMemorySplitUnit();
                try(FileInputStream fis = new FileInputStream(resourcePath.toFile())) {
                    long fileSize = this.resourcePath.toFile().length();
                    int cnt = (int)(fileSize / splitSize);
                    int rest = (int)(fileSize % splitSize);
                    for(int i=0; i<cnt; i++) {
                        byte[] part = new byte[splitSize];
                        fis.read(part);
                        this.resourceData.add(part);
                    }
                    byte[] part = new byte[rest];
                    fis.read(part);
                    this.resourceData.add(part);  
                };
            }
            this.lastModified = Files.getLastModifiedTime(this.resourcePath);
        }        
        /**
         * Get all bytes on resource
         * @return
         * @throws IOException
         */
        public byte[] getBytes() throws IOException {
            if(this.inMemoryFlag) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                for(byte[] part : this.resourceData) {
                    out.write(part);
                }
                return out.toByteArray();
            } else {
                return Files.readAllBytes(this.resourcePath);
            }
        }
        /**
         * Get partitial bytes of File resource by position & length
         * @param position
         * @param length
         * @return
         * @throws IOException
         */
        public byte[] getFileBytes(long position, int length) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(length);
            try(FileInputStream fis = new FileInputStream(this.resourcePath.toFile())) {
                FileChannel channel = fis.getChannel();
                channel.position(position);
                channel.read(buffer);
                channel.close();
                return buffer.array();    
            }
        }
        /**
         * Get bytes of resource(file or memory) with position & length
         * @param position
         * @param length
         * @return
         * @throws IOException
         */
        public byte[] getBytes1(long position, int length) throws IOException {
            byte[] bytes = new byte[length];
            if(this.inMemoryFlag) {
                if(position < 0) {
                    throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
                }
                int start = (int)position;
                int end  = (int)(position + length);
                int posStart = 0, posEnd = 0;
                int idx = 0;
                for(int i=0; i<this.resourceData.size(); i++) {
                    byte[] data = this.resourceData.get(i);
                    posEnd += data.length;
                    posStart = posEnd - data.length;
                    //System.out.println("start: "+start+" end: "+end+"  range: "+posStart+" - "+posEnd+"  i: "+i);
                    if(start >= posStart && start < posEnd) {
                        if(end <= posEnd) {
                            System.arraycopy(data, start - posStart, bytes, 0, length);
                            break;
                        } else {
                            idx = posEnd - start;
                            System.arraycopy(data, start - posStart, bytes, 0, idx);
                            continue;
                        }
                    } else if(start < posStart) {
                        if(end < posEnd) {
                            //System.out.println("begin "+start+"  end: "+end+" data: "+data.length+"  posStart: "+posStart+"  posEnd: "+posEnd);
                            int len = posStart + (posEnd - end);
                            if(idx + len < end) {
                                System.arraycopy(data, 0, bytes, idx, len);
                            } else {
                                System.arraycopy(data, 0, bytes, idx, posEnd - len);
                            }
                            break;    
                        } else {
                            System.arraycopy(data, 0, bytes, idx, data.length);
                            //System.out.println("idx: "+idx);
                            idx += data.length;
                        }
                    }
                }
            } else {
                bytes = getFileBytes(position, length);
            }
            return bytes;
        }
        /**
         * Get bytes of resource(file or memory) with position & length
         * @param position
         * @param length
         * @return
         * @throws IOException
         */
        public byte[] getBytes2(long position, int length) throws IOException {
            byte[] data = new byte[length];
            if(this.inMemoryFlag) {
                if(position < 0) {
                    throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
                }
                long totalLen = this.resourcePath.toFile().length();
                int partCnt = this.resourceData.size();
                int partSize = (int)(totalLen / partCnt);
                int partIdx1 = (int)(position / partSize);
                
                partIdx1 = position % partSize > 0 ? partIdx1++ : partIdx1;
                int partIdx2 = (int)((position + partSize) / partSize);    
                int buffSize = (int)(totalLen / length);
                int buffIdx1 = (int)(position / length);
                System.out.println("partIdx1: "+partIdx1+"  partIdx2: "+partIdx2+"  buffSize: "+buffSize+"  buffIdx1: "+buffIdx1);
                if(partIdx1 == partIdx2) {
                    byte[] part = this.resourceData.get(partIdx1);
                    int idx = (int)position;
                    System.arraycopy(part, idx, data, 0, length);
                } else {
                    if(partIdx2 - partIdx1 == 1) {
                        byte[] part = this.resourceData.get(partIdx1);
                        int idx = (int)(position % buffSize);
                        System.arraycopy(part, idx, data, 0, length - idx);
                        part = this.resourceData.get(partIdx2 - 1);
                        System.arraycopy(part, 0, data, length - idx, length - (length - idx));
                    } else {
                        byte[] part = this.resourceData.get(partIdx1 - 1);
                        int idx = (int)(position % buffSize);
                        System.arraycopy(part, idx, data, 0, length - idx);
                        for(int i=partIdx1+1; i<partIdx2; i++) {
                            part = this.resourceData.get(i);
                            System.arraycopy(part, 0, data, 0, length);
                        }
                        part = this.resourceData.get(partIdx2 - 1);
                        idx = (int)((position + length) % buffSize);
                        System.arraycopy(part, 0, data, 0, length - idx);
                    }
                }
            } else {
                data = getFileBytes(position, length);
            }
            return data;     
        }
        /**
         * Get resource Path object
         * @return
         */
        public Path getResourcePath() {
            return this.resourcePath;
        }
        /**
         * Get context path of resource
         * @return
         */
        public String getContextPath() {
            return this.contextPath;
        }
        /**
         * Get resource name
         * @return
         */
        public String getResourceName() {
            return this.resourcePath.getFileName().toString();
        }
        /**
         * Get resource File
         * @return
         */
        public File getResourceFile() {
            return this.resourceFile;
        }
        /**
         * Get resurce size
         * @return
         */
        public long getResourceSize() {
            return this.resourceSize;
        }
        /**
         * Get mime-type of resource
         * @return
         */
        public MIME_TYPE getMimeType() {
            return this.mimeType;
        }
        /**
         * Get last modified time of specfied unit
         * @param timeUnit
         * @return
         */
        public long getTime(TimeUnit timeUnit) {
            return this.lastModified.to(timeUnit);
        }
    }
}
