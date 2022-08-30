package org.chaostocosmos.leap.http.context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.resource.ResourceHelper;

/**
 * Context management object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class Context {
    /**
     * Context instance
     */
    public static Context context;

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
    private static List<MetaListener<?>> contextListeners = new ArrayList<>();


    /**
     * Constructor with home path
     * @param homePath
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void init(Path homePath) throws IOException, URISyntaxException {
        if(context == null) {
            context = new Context();
        }
        HOME_PATH = homePath.normalize().toAbsolutePath();
        CONFIG_PATH = HOME_PATH.resolve("config");

        //build config environment
        ResourceHelper.extractResource("config", homePath); 

        //dispatch context events
        dispatchContextEvent(new MetaEvent<Map<String, Object>, Object>(context, EVENT_TYPE.INITIALIZED, null, null, null));
    }

    /**
     * Refresh all metadata
     * @throws NotSupportedException
     * @throws IOException
     */
    public static void refresh() throws NotSupportedException, IOException {
        for(META meta : META.values()) {
            meta.load();
        }
    }

    /**
     * Dispatch context event to listeners
     * @param <V>
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T, V> void dispatchContextEvent(MetaEvent<T, V> me) {
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
    public static <T, V> void addContextListener(MetaListener<T> listener) {        
        contextListeners.add(listener);
    }  

    /**
     * Remove context listener
     * @param listener
     */ 
    public static <T, V> void removeContextListener(MetaListener<T> listener) {
        contextListeners.remove(listener);
    }

    /**
     * Get home path
     * @return
     */
    public static Path getHomePath() {
        return HOME_PATH;
    }

    /**
     * Get config path
     * @return
     */
    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    /**
     * Get template path
     * @return
     */
    public static Path getTemplates(String hostId) {
        return getHosts().getTemplates(hostId);
    }

    /**
     * Get Server context
     * @return
     */
    public static <T> Server<T> getServer() {
        return META.SERVER.getMeta();
    }

    /**
     * Get Hosts context
     * @return
     */
    public static <T> Hosts<T> getHosts() {
        return META.HOSTS.<Hosts<T>>getMeta();
    }

    /**
     * Get Messages context
     * @return
     */
    public static <T> Messages<T> getMessages() {
        return META.MESSAGES.getMeta();
    }    

    /**
     * Get Mime context
     * @return
     */
    public static <T> Mime<T> getMime() {
        return META.MIME.getMeta();
    }

    /**
     * Get Chart context
     * @return
     */
    public static <T> Chart<T> getChart() {
        return META.CHART.getMeta();
    }

    /**
     * Get Host object mapping with the specifiedhost ID
     * @param hostId
     * @return
     */
    public static <T> Host<T> getHost(String hostId) {
        Hosts<T> hosts = getHosts();
        return hosts.getHost(hostId);
    }

    /**
     * Get Host Map
     * @return
     */
    public static <T> List<Host<T>> getAllHost() {
        Hosts<T> hosts = getHosts();
        return hosts.getAllHost();
    }

    /**
     * Get meta-data value
     * @param <T>
     * @param metaType
     * @param expr
     * @return
     */
    public static <T> T getMetadata(META metaType, String expr) {
        switch(metaType) {
            case SERVER:
                return getServer().getValue(expr);
            case HOSTS:
                return getHosts().getValue(expr);
            case MESSAGES:
                return getMessages().getValue(expr);
            case MIME:
                return getMime().getValue(expr);
            case CHART:
                return getChart().getValue(expr);
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
    public static <T, V> void save(META meta) {
        meta.save(); 
        dispatchContextEvent(new MetaEvent<T,V>(context, EVENT_TYPE.STORED, meta.getMeta(), null, null));       
    }   
}
