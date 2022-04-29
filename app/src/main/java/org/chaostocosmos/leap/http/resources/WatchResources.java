package org.chaostocosmos.leap.http.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.google.gson.Gson;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UNIT;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * WatchResource object
 * 
 * @author 9ins
 */
public class WatchResources extends Thread implements Resources {
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
     * @throws ImageProcessingException
     */
    public WatchResources(Hosts hosts, Kind<?>[] watchKinds) throws IOException, ImageProcessingException {
        this(hosts.getHost(), hosts.getStatic(), watchKinds, hosts.getAccessFiltering(), hosts.getForbiddenFiltering(), hosts.getInMemoryFiltering(), 1024 * 1000 * 1000);
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
    public WatchResources(String host, Path watchPath, Kind<?>[] watchKinds, Filtering accessFiltering, Filtering forbiddenFiltering, Filtering inMemoryFiltering, int inMemoryLimitSize) throws IOException, ImageProcessingException {
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
                                try {
                                    if(this.inMemoryFiltering.include(path.toFile().getName())) {
                                        //When In-Memory resource
                                        Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);                                        
                                        if(path.toFile().length() <= this.inMemoryLimitSize) {
                                            data = new ResourceInfo(path, true);
                                            LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] Loaded to memory: "+((byte[])data).length);
                                        } else {
                                            //When In-Memory limit and reject
                                            data = new ResourceInfo(path, false);
                                            LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] In-Memory file size overflow. Limit: "+UNIT.MB.get(this.inMemoryLimitSize, 2)+"  File size: "+UNIT.MB.get(path.toFile().length(), 2));
                                        }
                                    } else {                        
                                        // When File resource            
                                        data = new ResourceInfo(path, false);                                
                                    }
                                } catch(Exception e) {
                                    continue;
                                }
                            }
                        }
                        String[] paths = path.subpath(this.watchPath.getNameCount(), path.getNameCount()).toString().split(Pattern.quote(File.separator));
                        addResource(this.resourceTree, paths, data);
                    } else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        path = path.resolve(event.context().toString());
                        if(path.toFile().isFile() && this.accessFiltering.include(path.toFile().getName())) {
                            Object data = null;
                            try {
                                //When In-Memory resource
                                if(this.accessFiltering.include(path.toFile().getName()) && this.forbiddenFiltering.exclude(path.toFile().getName())) {
                                    Files.move(path, path, StandardCopyOption.ATOMIC_MOVE);                                          
                                    if(path.toFile().length() <= this.inMemoryLimitSize) {
                                        LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] Loaded to memory: "+((byte[])data).length);
                                        data = new ResourceInfo(path, true);
                                    } else {
                                        //When In-Memory limit and reject
                                        LoggerFactory.getLogger(this.host).debug("[IN-MEMORY CREATED] In-Memory file size overflow. Limit: "+UNIT.MB.get(this.inMemoryLimitSize, 2)+"  File size: "+UNIT.MB.get(path.toFile().length(), 2));
                                        data = new ResourceInfo(path, false);
                                    }                                    
                                } else {
                                    // When File resource   
                                    data = new ResourceInfo(path, false);
                                }
                            } catch(Exception e) {
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
    protected Map<String, Object> loadResoureTree(Path path, Map<String, Object> tree) throws IOException, ImageProcessingException {
        File[] files = path.toFile().listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                tree.put(file.getName(), loadResoureTree(file.toPath(), new LinkedHashMap<String, Object>()));
            } else {
                if(file.isFile()) {
                    String fileSize = (float)UNIT.KB.applyUnit(file.length(), 2)+" "+UNIT.KB.name();
                    if(file.getParentFile().getName().equals("template") || this.accessFiltering.include(file.getName())) {
                        if(this.forbiddenFiltering.exclude(file.getName())) {
                            if(file.length() <= this.inMemoryLimitSize && this.inMemoryFiltering.include(file.getName())) {
                                System.out.println(file.getAbsolutePath());
                                tree.put(file.getName(), new ResourceInfo(file.toPath(), true));
                                LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][LOAD] In-Memory resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                            } else {
                                tree.put(file.getName(), new ResourceInfo(file.toPath(), false));
                                LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][LOAD] File resource loaded: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                            }    
                        } else {
                            LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][EXCLUDING] Forbidden resource: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
                        }
                    } else {
                        LoggerFactory.getLogger(this.host).debug("[HOST:"+this.host+"][NOT-INCLUDED] Not included in access resources: "+file.getName()+"  Size: "+fileSize+"  Bytes: "+file.length());
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
    private ResourceInfo getResource(Map<String, Object> tree, String[] res) {
        if(res.length == 1) {
            return (ResourceInfo)tree.get(res[0]);
        } else {
            return getResource((Map<String, Object>)tree.get(res[0]), Arrays.copyOfRange(res, 1, res.length));
        }
    }

    @Override
    public void addResource(Path resourcePath) throws IOException, ImageProcessingException { 
        if(resourcePath.toFile().isFile() && this.accessFiltering.include(resourcePath.toFile().getName()) && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {            
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            if(this.inMemoryFiltering.include(resourcePath.toFile().getName())) {
                LoggerFactory.getLogger(this.host).debug("[ADD IN-MEMORY RESOURCE] Watch: "+this.watchPath.toString()+"  Resource: "+path.toString());
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new ResourceInfo(path, true));
            } else {
                LoggerFactory.getLogger(this.host).debug("[ADD FILE RESOURCE] Watch: "+this.watchPath.toString()+"  Resource: "+path.toString());
                addResource(this.resourceTree, path.toString().split(Pattern.quote(File.separator)), new ResourceInfo(path, false));
            }
        }
    }

    @Override
    public ResourceInfo getContextResourceInfo(String contextPath) throws IOException { 
        return getResourceInfo(this.watchPath.resolve(contextPath));
    }

    @Override
    public ResourceInfo getResourceInfo(Path resourcePath) {
        if(this.accessFiltering.include(resourcePath.toFile().getName()) && this.forbiddenFiltering.exclude(resourcePath.toFile().getName())) {    
            Path path = resourcePath.subpath(this.watchPath.getNameCount(), resourcePath.getNameCount());
            String[] paths = path.toString().split(Pattern.quote(File.separator));
            return getResource(this.resourceTree, paths);
        }
        throw new WASException(MSG_TYPE.ERROR, 20, resourcePath);
    }

    @Override
    public String getStaticPage(String contextPath, Map<String, Object> params) throws IOException {        
        Object data = getContextResourceInfo(contextPath);
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

    /**
     * Extract resource metadata
     * @param resourcePath
     * @return
     * @throws ImageProcessingException
     * @throws IOException
     */
    public Map<String, Map<String, String>> extractMetadata(Path resourcePath) throws ImageProcessingException, IOException {
        Map<String, Map<String, String>> metadataMap = new HashMap<>();
        Metadata metadata = Mp4MetadataReader.readMetadata(resourcePath.toFile());
        for (Directory directory : metadata.getDirectories()) {
            if(directory.getTagCount() > 0) {
                Map<String, String> tagMap = new HashMap<>();
                for (Tag tag : directory.getTags()) {
                    tagMap.put(tag.getTagName(), tag.getDescription());
                }    
                metadataMap.put(directory.getName(), tagMap);
            }
        }      
        return metadataMap;
    }

    /**
     * ResourceInfo
     * 
     * @author 9ins
     */
    public class ResourceInfo {

        final int BYTE_LIMIT = 1024 * 1000;

        Path resourcePath;

        boolean inMemoryFlag;

        List<byte[]> resourceData;

        Map<String, Map<String, String>> metadata;
        
        /**
         * Constructor
         * @param resourcePath
         * @param inMemoryFlag
         * @throws IOException
         * @throws ImageProcessingException
         */
        public ResourceInfo(Path resourcePath, boolean inMemoryFlag) throws IOException, ImageProcessingException {
            this.resourcePath = resourcePath;
            this.inMemoryFlag = inMemoryFlag;
            System.out.println(resourcePath);
            this.metadata = extractMetadata(this.resourcePath);
            if(this.inMemoryFlag) {
                this.resourceData = new ArrayList<>();
                FileInputStream fis = new FileInputStream(resourcePath.toFile());
                long fileSize = this.resourcePath.toFile().length();
                int cnt = (int)(fileSize / BYTE_LIMIT);
                int rest = (int)(fileSize % BYTE_LIMIT);                
                for(int i=0; i<cnt; i++) {
                    byte[] part = new byte[BYTE_LIMIT];
                    fis.read(part);
                    this.resourceData.add(part);
                }
                byte[] part = new byte[rest];
                fis.read(part);
                this.resourceData.add(part);  
            }
        }

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

        public byte[] getBytes(long offset, int length) throws IOException {
            byte[] bytes = new byte[length];
            if(this.inMemoryFlag) {
                if(offset < 0) {
                    throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
                }
                int len = 0;                
                int start = (int)offset;
                int end  = (int)(offset + length);
                for(int i=0; i<this.resourceData.size(); i++) {
                    byte[] data = this.resourceData.get(i);
                    System.out.println("start: "+start+" end: "+end+"  range: "+(i*data.length)+" - "+(i + 1)*data.length);
                    if(start >= i * data.length && start < (i + 1)*data.length) {
                        if(end < (i + 1)*data.length) {
                            System.out.println("begin exit "+start+"  "+length);
                            System.arraycopy(data, start - i*data.length, bytes, 0, length);
                            break;
                        } else {
                            len += (i + 1)*data.length - start;
                            System.out.println("begin "+start+"  "+len);
                            System.arraycopy(data, start - i*data.length, bytes, 0, len);
                        }   
                    } else if(end >= i * data.length  && end < (i + 1)*data.length) {
                        System.out.println("end  len: "+len);
                        System.arraycopy(data, 0, bytes, len, bytes.length - len);
                        break;
                    } else if(start < i * data.length  && end > (i + 1)*data.length) {
                        System.out.println("middle ");
                        System.arraycopy(data, 0, bytes, i * data.length , data.length);
                        len += data.length;
                    }
                }
            } else {
                FileInputStream fis = new FileInputStream(this.resourcePath.toFile());
                fis.skip(offset);
                fis.read(bytes);
            }
            return bytes;
        }
        
        public String getMetadataValue(String type, String key) {
            return this.metadata.get(type).get(key);
        }

        public Path getResourcePath() {
            return this.resourcePath;
        }

        public String getResourceName() {
            return this.resourcePath.getFileName().toString();
        }

        public long getResourceSize() {
            return this.resourcePath.toFile().length();
        }
    }
}
