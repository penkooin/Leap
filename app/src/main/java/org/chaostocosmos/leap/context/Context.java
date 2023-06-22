package org.chaostocosmos.leap.context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

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
    private Path HOME_PATH;
    /**
     * Config path
     */
    private Path CONFIG_PATH;
    /**
     * Context listener list
     */
    private List<MetaListener<?>> contextListeners = new ArrayList<>();

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
    private Context(Path HOME_PATH) throws IOException, URISyntaxException, InterruptedException {
        System.out.println("Start context service.....................");
        this.HOME_PATH = HOME_PATH;
        this.CONFIG_PATH = this.HOME_PATH.resolve("config");        
        //init metadata watcher
        //build config environment
        ResourceHelper.extractResource("config", this.HOME_PATH); 
        //dispatch context events
        dispatchContextEvent(new MetaEvent<Map<String, Object>, Object>(this, EVENT_TYPE.INITIALIZED, null, null, null));
        System.out.println("////////////////////////////");
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

    @Override
    public void run() {                
        this.metaFileModMap = Arrays.asList(META.values()).stream().map(m -> new Object[]{m, m.getMetaPath().toFile().lastModified()}).collect(Collectors.toMap(k -> (META)k[0], v -> (Long)v[1]));
        System.out.println("Start thread..........................");
        while(!isDone) {
            try {
                // List<META> modMetas = Arrays.asList(META.values()).stream().filter(m -> m.getMetaPath().toFile().lastModified() > this.metaFileModMap.get(m)).collect(Collectors.toList());
                // System.out.println("Meta mod count: "+modMetas.size());
                // if(modMetas.size() > 0) {
                //     for(int i=0; i<modMetas.size(); i++) {
                //         META meta = modMetas.get(i);
                //         System.out.println("META reloading. "+meta.getMetaPath().toString()+"  "+meta.getMetaPath().toFile().lastModified()+"  "+this.metaFileModMap.get(meta));
                //         meta.reload();
                //         this.metaFileModMap.put(meta, meta.getMetaPath().toFile().lastModified());
                //     }
                // }
                Thread.sleep(1000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }   
    }    

    /**
     * MetaWatcher class
     * 
     * @author 9ins
     */
    // public class MetaWatcher extends Thread {
    //     boolean isDone = false;
    //     Path watchPath;        
    //     WatchService watchService;
    //     /**
    //      * Metadata file modified time Map
    //     */
    //     private Map<META, Long> metaFileModMap;

    //     /**
    //      * Constructor
    //      * @param watchPath
    //      * @throws IOException
    //      */
    //     public MetaWatcher(Path watchPath) throws IOException {
    //         this.watchPath = watchPath.toAbsolutePath();
    //     }
    //     /**
    //      * End watch
    //      * @throws InterruptedException
    //      */
    //     public void end() throws InterruptedException {
    //         this.isDone = true;
    //         this.interrupt();
    //     }

    //     @Override 
    //     public void run() {            
    //         long timestemp = System.currentTimeMillis();
    //         int cnt = 0;
    //         while(!this.isDone) {
    //             try {
    //                 WatchKey key = this.watchService.take();
    //                 Path context = null;
    //                 long eventMillis = System.currentTimeMillis();
    //                 for(WatchEvent<?> event : key.pollEvents()) {
    //                     WatchEvent.Kind<?> kind = event.kind();
    //                     if(kind == StandardWatchEventKinds.ENTRY_MODIFY && eventMillis - timestemp > 50) {
    //                         context = CONFIG_PATH.resolve(event.context().toString());
    //                         break;
    //                     }
    //                 }
    //                 boolean valid = key.reset(); 
    //                 if (!valid) {
    //                     break;
    //                 }
    //                 if(context != null) {
    //                     final Path metaPath = context.toAbsolutePath().normalize();
    //                     System.out.println(metaPath.toString()+"   "+(eventMillis - timestemp));
    //                     META meta = Arrays.asList(META.values()).stream().filter(m -> m.getMetaPath().toAbsolutePath().normalize().equals(metaPath)).findFirst().orElseThrow(() -> new FileNotFoundException("File not found in event path: "+metaPath.toAbsolutePath()));
    //                     meta.reload();
    //                 }
    //                 timestemp = eventMillis;
    //             } catch(IOException | ClosedWatchServiceException | InterruptedException | NotSupportedException e) {
    //                 e.printStackTrace();     
    //             }
    //         }
    //         try {
    //             this.watchService.close();
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //         LoggerFactory.getLogger().info("Config data watcher is closed...");
    //     }
    // }

    /**
     * Refresh all metadata
     * @throws NotSupportedException
     * @throws IOException
     */
    public static void refresh() throws NotSupportedException, IOException {
        for(META meta : META.values()) {
            meta.reload(); 
        }
    }

    /**
     * Dispatch context event to listeners
     * @param <V>
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T, V> void dispatchContextEvent(MetaEvent<T, V> me) { 
        for(MetaListener<?> listener : contextListeners) {
            try {
                //String typeName = ((ParameterizedType)listener.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();            
                ((MetaListener<T>)listener).receiveContextEvent(me);
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
    public <T, V> void addContextListener(MetaListener<T> listener) {        
        contextListeners.add(listener);
    }  

    /**
     * Remove context listener
     * @param listener
     */ 
    public <T, V> void removeContextListener(MetaListener<T> listener) {
        contextListeners.remove(listener);
    }

    /**
     * Get home path
     * @return
     */
    public Path getHomePath() {
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
    public Path getTemplates(String hostId) {
        return hosts().getTemplates(hostId);
    }

    /**
     * Get Server context
     * @return
     */
    public <T> Server<T> server() {
        return new Server<T>(META.SERVER.getMeta());
    }

    /**
     * Get Hosts context
     * @return
     */
    public <T> Hosts<T> hosts() {
        return new Hosts<T>(META.HOSTS.getMeta());
    }

    /**
     * Get Messages context
     * @return
     */
    public <T> Messages<T> messages() {
        return new Messages<T>(META.MESSAGES.getMeta());
    }    

    /**
     * Get Mime context
     * @return
     */
    public <T> Mime<T> mime() {
        return new Mime<T>(META.MIME.getMeta());
    }

    /**
     * Get Chart context
     * @return
     */
    public <T> Chart<T> chart() {
        return new Chart<T>(META.CHART.getMeta());
    }

    /**
     * Get Host object mapping with the specifiedhost ID
     * @param hostId
     * @return
     */
    public <T> Host<T> host(String hostId) {
        Hosts<T> hosts = hosts();
        return hosts.getHost(hostId);
    }

    /**
     * Get Host Map
     * @return
     */
    public <T> List<Host<T>> allHost() {
        Hosts<T> hosts = hosts();
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
            case MESSAGES:
                return messages().getValue(expr);
            case MIME:
                return mime().getValue(expr);
            case CHART:
                return chart().getValue(expr);
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
        context.dispatchContextEvent(new MetaEvent<T,V>(context, EVENT_TYPE.STORED, meta.getMeta(), null, null));
    }
 
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Context context = new Context(Paths.get("."));        
        context.start();
    }
}
