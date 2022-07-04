package org.chaostocosmos.leap.http.context;

import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.resources.ResourceHelper;

/**
 * Context management object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class Context {
    /**
     * Home path
     */
    private static Path HOME_PATH;
    /**
     * Config path
     */
    private static Path CONFIG_PATH;
    /**
     * Metadata Map
     */
    private static Map<META, Metadata<?>> metaMap = new HashMap<>();;
    /**
     * Context listener map
     */
    private static List<ContextListener<?>> contextListeners = new ArrayList<>();;

    public Context() {}
    /**
     * Constructor with home path
     * @param homePath
     */
    public static void init(Path homePath) {
        try {
            HOME_PATH = homePath;
            CONFIG_PATH = HOME_PATH.resolve("config");

            //build config environment
            ResourceHelper.extractResource("config", homePath); 

            //refresh context attributes
            refresh();

            //dispatch context events
            dispatchContextEvent(EVENT_TYPE.INITIALIZED);
        } catch(Exception e) {
            throw new WASException(e);
        }
    }
    /**
     * Refresh context attributes
     */
    public static void refresh() {
        //load configuration files
        metaMap.put(META.SERVER, new Server<Map<String, Object>>(META.SERVER.getMetaMap()));            
        metaMap.put(META.HOSTS, new Hosts<Map<String, Object>>(META.HOSTS.getMetaMap()));
        metaMap.put(META.MESSAGES, new Messages<Map<String, Object>>(META.MESSAGES.getMetaMap()));
        metaMap.put(META.MIME, new Mime<Map<String, Object>>(META.MIME.getMetaMap()));
        metaMap.put(META.CHART, new Chart<Map<String, Object>>(META.CHART.getMetaMap()));
    }
    /**
     * Dispatch context event to listeners
     * @param <T>
     * @throws Exception
     */
    public static void dispatchContextEvent(EVENT_TYPE eventType) {
        try{
            for(ContextListener<?> listener : contextListeners) {
                String typeName = ((ParameterizedType)listener.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
                if(typeName.startsWith(Server.class.getTypeName())) {
                    listener.contextServer(new ContextEvent<Server<?>>(new Context(), eventType, (Server<?>)metaMap.get(META.SERVER)));
                } else if(typeName.startsWith(Hosts.class.getTypeName())) {
                    listener.contextHosts(new ContextEvent<Hosts<?>>(new Context(), eventType, (Hosts<?>)metaMap.get(META.HOSTS)));
                } else if(typeName.startsWith(Messages.class.getTypeName())) {
                    listener.contextMessages(new ContextEvent<Messages<?>>(new Context(), eventType, (Messages<?>)metaMap.get(META.MESSAGES)));
                } else if(typeName.startsWith(Mime.class.getTypeName())) {
                    listener.contextMime(new ContextEvent<Mime<?>>(new Context(), eventType, (Mime<?>)metaMap.get(META.MIME)));
                } else if(typeName.startsWith(Chart.class.getTypeName())) {
                    listener.contextChart(new ContextEvent<Chart<?>>(new Context(), eventType, (Chart<?>)metaMap.get(META.CHART)));                
                } else {
                    throw new GeneralSecurityException("ContextListener generic type not support type of Metadata");
                }
            }    
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Add context listener
     * @param meta
     * @param listener
     */
    public static <T> void addContextListener(ContextListener<T> listener) {        
        contextListeners.add(listener);
    }   
    /**
     * Remove context listener
     * @param listener
     */ 
    public static <T> void removeContextListener(ContextListener<T> listener) {
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
    public static Server<?> getServer() {
        return (Server<?>)metaMap.get(META.SERVER);
    }
    /**
     * Get Hosts context
     * @return
     */
    public static Hosts<?> getHosts() {
        return (Hosts<?>)metaMap.get(META.HOSTS);
    }
    /**
     * Get Messages context
     * @return
     */
    public static Messages<?> getMessages() {
        return (Messages<?>)metaMap.get(META.MESSAGES);
    }    
    /**
     * Get Mime context
     * @return
     */
    public static Mime<?> getMime() {
        return (Mime<?>)metaMap.get(META.MIME);
    }
    /**
     * Get Chart context
     * @return
     */
    public static Chart<?> getChart() {
        return (Chart<?>)metaMap.get(META.CHART);
    }
    /**
     * Get Host object mapping with the specifiedhost ID
     * @param hostId
     * @return
     */
    public static Host<?> getHost(String hostId) {
        return getHosts().getHost(hostId);
    }
    /**
     * Get Host Map
     * @return
     */
    public static Map<String, Host<?>> getHostMap() {
        return getHosts().getHostMap();
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
     * @throws WASException
     */
    public static void save(META meta) throws WASException {
        meta.save(); 
        dispatchContextEvent(EVENT_TYPE.STORED);       
    }   
}
