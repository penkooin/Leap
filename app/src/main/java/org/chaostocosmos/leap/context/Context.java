package org.chaostocosmos.leap.context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.enums.SERVER_EVENT;
import org.chaostocosmos.leap.enums.WEB_PATH;
import org.chaostocosmos.leap.resource.ResourceHelper;

/**
 * Context management object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class Context extends Thread {
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
     * Wether over thread
     */
    boolean isDone;
    /**
     * Constructor 
     * @param HOME_PATH
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    private Context(Path HOME_PATH_) throws IOException, URISyntaxException, InterruptedException {
        HOME_PATH = HOME_PATH_;
        CONFIG_PATH = HOME_PATH.resolve(WEB_PATH.CONFIG.name().toLowerCase());        
        //init metadata watcher
        //build config environment
        ResourceHelper.extractResource(WEB_PATH.CONFIG.name().toLowerCase(), HOME_PATH); 
        //dispatch context events
        start();
    }
    /**
     * Get Context object 
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Context get() {
        return get(Paths.get(System.getProperty("user.dir")));
    }
    /**
     * Get Context object by specified Path
     * @param HOME_PATH
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Context get(Path HOME_PATH) {
        if(context == null) {
            try {
                context = new Context(HOME_PATH);
            } catch (IOException | URISyntaxException | InterruptedException e) {
                e.printStackTrace();
            }
        }        
        return context;
    }
    /**
     * Stop metadata watcher
     * @throws InterruptedException
     */
    public void stopMetaWatcher() throws InterruptedException {
        this.isDone = true;
        interrupt();
    }

    // @Override
    // public void run() {                
    //     this.metaFileModMap = Arrays.asList(META.values()).stream().map(m -> new Object[]{m, m.getMetaPath().toFile().lastModified()}).collect(Collectors.toMap(k -> (META)k[0], v -> (Long)v[1]));
    //     System.out.println("Start thread..........................");
    //     while(!isDone) {
    //         try {
    //             List<META> modMetas = Arrays.asList(META.values()).stream().filter(m -> m.getMetaPath().toFile().lastModified() > this.metaFileModMap.get(m)).collect(Collectors.toList());
    //             System.out.println("Meta mod count: "+modMetas.size());
    //             if(modMetas.size() > 0) {
    //                 for(int i=0; i<modMetas.size(); i++) {
    //                     META meta = modMetas.get(i);
    //                     System.out.println("META reloading. "+meta.getMetaPath().toString()+"  "+meta.getMetaPath().toFile().lastModified()+"  "+this.metaFileModMap.get(meta));
    //                     meta.reload();
    //                     this.metaFileModMap.put(meta, meta.getMetaPath().toFile().lastModified());
    //                 }
    //             }
    //             Thread.sleep(1000);
    //         } catch(Exception e) {
    //             e.printStackTrace();
    //         }
    //     }   
    // }    

    @Override 
    public void run() {       
        WatchService watchService;    
        try {
            watchService = FileSystems.getDefault().newWatchService();
            CONFIG_PATH.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            long timestemp = System.currentTimeMillis();
            while(!this.isDone) {
                try {
                    WatchKey key = watchService.take();
                    Path context = null;
                    long eventMillis = System.currentTimeMillis(); 
                    for(WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if(kind == StandardWatchEventKinds.ENTRY_MODIFY && eventMillis - timestemp > 30) {
                            context = CONFIG_PATH.resolve(event.context().toString());
                            key.reset();
                            break;
                        }
                    }
                    boolean valid = key.reset(); 
                    if (!valid) {
                        break;
                    }
                    if(context != null) {
                        final Path metaPath = context.toAbsolutePath().normalize();
                        META meta = Arrays.asList(META.values()).stream().filter(m -> m.getMetaPath().toAbsolutePath().normalize().equals(metaPath)).findFirst().orElseThrow(() -> new FileNotFoundException("File not found in event path: "+metaPath.toAbsolutePath()));
                        meta.reload();
                        dispatchContextEvent(new MetaEvent<Metadata<?>>(this, SERVER_EVENT.CHANGED, meta.getMeta(), null, null));
                    }
                    timestemp = eventMillis;
                } catch(Exception e) {
                    //LoggerFactory.getLogger().error(e.getMessage(), e);
                }
            }
            watchService.close();
        } catch (IOException e) {
            LoggerFactory.getLogger().error(e.getMessage(), e);
        }
        LoggerFactory.getLogger().info("Config data watcher is closed...");
    }

    /**
     * Refresh all metadata
     * @throws NotSupportedException
     * @throws IOException
     */
    public void refresh() throws NotSupportedException, IOException {
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
    public Path getConfig() {
        return CONFIG_PATH;
    }

    /**
     * Get template path
     * @return
     */
    public Path getTemplates(String hostId) {
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
        return hosts.getAllHost();
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
     * 
     * @param map
     * @param targetFile
     * @throws Exception
     */
    public <T, V> void save(META meta) {
        meta.save(); 
        context.dispatchContextEvent(new MetaEvent<Metadata<?>>(context, SERVER_EVENT.STORED, meta.getMeta(), null, null));
    } 
}
