package org.chaostocosmos.leap.context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.LeapApp;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.context.utils.Diff;
import org.chaostocosmos.leap.context.utils.MetaUtils;
import org.chaostocosmos.leap.enums.WAR_PATH;
import org.chaostocosmos.leap.resource.config.ConfigUtils;

/**
 * Context management object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class Context implements Runnable {

    /**
     * Context instance
     */
    private static Context context = null;

    /**
     * Home path
     */
    private static Path HOME_PATH;

    /**
     * Config path
     */
    private static Path CONFIG_PATH;

    /**
     * Context listener list
     */
    private List<MetaListener> contextListeners = new ArrayList<>();

    /**
     * META mod Map
     */
    Map<META, Long> metaFileModMap;

    /**
     * Config watch object
     */
    WatchService watchService;

    /**
     * Context thread
     */
    Thread contextThread;

    /**
     * Constructor 
     * @param HOME_PATH
     * @throws URISyntaxException 
     * @throws IOException 
     */
    private Context(Path HOME_PATH_) {
        HOME_PATH = HOME_PATH_;
        CONFIG_PATH = HOME_PATH.resolve(WAR_PATH.CONFIG.path());
        startWatch();
    }

    /**
     * Get Context object 
     * @return
     */
    public static Context get() {
        if(context == null) {
            context = new Context(LeapApp.HOME_PATH);
        }
        return context;
    }

    /**
     * Start metadata watcher
     */
    public synchronized void startWatch() {
        if(this.isRunning.get()) {
            stopWatch();
        }
        this.contextThread = new Thread(this);
        this.contextThread.setName(getClass().getName());
        this.contextThread.setDaemon(true);
        this.contextThread.start();
        this.isRunning.set(true);
    }

    /**
     * Stop metadata watcher
     * @throws InterruptedException
     */
    public synchronized void stopWatch() {
        this.isRunning.set(false);
        try {
            if(this.contextThread != null) this.contextThread.interrupt();
            if(this.contextThread != null) this.contextThread.join();            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Running flag object
     */
    AtomicBoolean isRunning = new AtomicBoolean(false);

    @Override 
    public void run() {       
        LoggerFactory.getLogger().info("[CONTEXT WATCH SERVICE START] Watch Path: "+CONFIG_PATH.toAbsolutePath().normalize().toString());
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            while(isRunning.get()) {
                try {
                    CONFIG_PATH.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    try {
                        long timestemp = System.currentTimeMillis();
                        WatchKey key = watchService.take();
                        long eventMillis = System.currentTimeMillis(); 
                        for(WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent<Path> kind = (WatchEvent<Path>) event;
                            Path path = kind.context();
                            if(isTemporaryFile(path)) {
                                LoggerFactory.getLogger().debug("Modified file :"+path);
                                continue;
                            }
                            Path metaPath = CONFIG_PATH.resolve(path);
                            System.out.println(metaPath);
                            Map<String, Object> changedMap = ConfigUtils.loadConfig(metaPath);
                            META meta = Arrays.asList(META.values()).stream().filter(m -> m.getMetaPath().toAbsolutePath().normalize().equals(metaPath)).findFirst().orElseThrow(() -> new FileNotFoundException("File not found in event path: "+metaPath.toAbsolutePath()));
                            List<Diff> diffs = MetaUtils.compareDiffMaps(meta.metaMap, changedMap, "");
                            diffs.stream().forEach(d -> System.out.println(d.getPath()+"  "+d.getOriginalValue()+"  "+d.getModifiedValue()));
                            meta.reload();
                            if(diffs.size() > 0) {
                                for(Diff diff : diffs) {
                                    dispatchContextEvent(new MetaEvent<Metadata<?>>(
                                                                                    this, META_EVENT_TYPE.MODIFIED, 
                                                                                    meta.getMeta(), 
                                                                                    diff.getPath(), 
                                                                                    diff.getOriginalValue(),  
                                                                                    diff.getModifiedValue()));
                                }
                            }    
                        }
                        boolean isValid = key.reset();
                        if(!isValid) {
                            break;
                        }                        
                        timestemp = eventMillis;
                    } catch(Exception e) {
                        LoggerFactory.getLogger().error(e.getMessage());
                    }
                } catch (Exception e) {
                    LoggerFactory.getLogger().throwable(e);
                }
            }
            if(this.watchService != null) this.watchService.close();
        } catch (IOException e) {
            LoggerFactory.getLogger().throwable(e);
        }
        LoggerFactory.getLogger().info("CONTEXT WATCH SERVICE IS CLOSED: "+CONFIG_PATH.toAbsolutePath().normalize().toString());
    }

    private boolean isTemporaryFile(Path fileName) {
        String name = fileName.toFile().getName();
        return name.startsWith(".") || name.endsWith(".tmp") || name.endsWith(".swp") || name.endsWith("-swp");
    }    

    /**
     * Refresh all metadata
     * @throws NotSupportedException
     * @throws IOException
     */
    public void refresh() throws IOException, NotSupportedException {
        for(META meta : META.values()) {
            meta.reload(); 
        }
    }

    /**
     * Dispatch context event to listeners
     * @param <V>
     * @throws Exception
     */
    public void dispatchContextEvent(MetaEvent<Metadata<?>> me) { 
        for(MetaListener listener : contextListeners) {
            try {
                //String typeName = ((ParameterizedType)listener.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();            
                ((MetaListener) listener).receiveContextEvent(me);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }    
    }

    /**
     * Add context listener
     * @param meta
     * @param listener
     */
    public <T, V> void addContextListener(MetaListener listener) {        
        contextListeners.add(listener);
    }  

    /**
     * Remove context listener
     * @param listener
     */ 
    public <T, V> void removeContextListener(MetaListener listener) {
        contextListeners.remove(listener);
    }

    /**
     * Get home path
     * @return
     */
    public Path getHome() {
        return HOME_PATH;
    }

    /**
     * Get config path
     * @return
     */
    public Path getConfigPath() {
        return CONFIG_PATH;
    }

    /**
     * Get template path
     * @return
     */
    public Path getTemplatePath(String hostId) {
        return hosts().getTemplates(hostId);
    }

    /**
     * Get Server context
     * @return
     */
    public Server<?> server() {
        return (Server<?>) META.SERVER.getMeta();
    }

    /**
     * Get Hosts context
     * @return
     */
    public Hosts<?> hosts() {
        return (Hosts<?>) META.HOSTS.getMeta();
    }

    /**
     * Get Message context
     * @return
     */
    public Message<?> message() {
        return (Message<?>) META.MESSAGE.getMeta();
    }    

    /**
     * Get Mime context
     * @return
     */
    public Mime<?> mime() {
        return (Mime<?>) META.MIME.getMeta();
    }

    /**
     * Get Chart context
     * @return
     */
    public Monitor<?> monitor() {
        return (Monitor<?>) META.MONITOR.getMeta();
    }

    /**
     * Get Trademark 
     * @return
     */
    public Trademark<?> trademark() {
        return (Trademark<?>) META.TRADEMARK.getMeta();
    }

    /**
     * Get Host object mapping with the specifiedhost ID
     * @param hostId
     * @return
     */
    public Host<?> host(String hostId) {
        Hosts<?> hosts = hosts();
        return hosts.getHost(hostId);
    }

    /**
     * Get Host Map
     * @return
     */
    public List<Host<?>> allHost() {
        Hosts<?> hosts = hosts();
        return hosts.getHosts();
    }

    /**
     * Get meta-data value
     * @param <T>
     * @param metaType
     * @param expr
     * @return
     */
    public <T> T getMetadata(META metaType, String expr) {
        switch(metaType) {
            case SERVER:
                return server().getValue(expr);
            case HOSTS:
                return hosts().getValue(expr);
            case MESSAGE:
                return message().getValue(expr);
            case MIME:
                return mime().getValue(expr);
            case MONITOR:
                return monitor().getValue(expr);
            default:
                throw new IllegalArgumentException("Metadata type is not found: "+metaType.name());
        }
    }

    /**
     * Save config
     * @param map
     * @param targetFile
     * @throws Exception
     */
    public <T, V> void save(META meta) {
        meta.save(); 
        context.dispatchContextEvent(new MetaEvent<Metadata<?>>(context, META_EVENT_TYPE.SAVED, meta.getMeta(), null, null, null));
    } 
}
