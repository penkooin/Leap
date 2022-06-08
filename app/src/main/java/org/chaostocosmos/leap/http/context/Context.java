package org.chaostocosmos.leap.http.context;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.resources.ResourceHelper;
import org.chaostocosmos.leap.http.resources.TemplateBuilder;

/**
 * Context management object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class Context {
    /**
     * Context
     */
    private static Context context;
    /**
     * Document root path
     */
    private static Path HOME_PATH;
    /**
     * Template Path
     */
    private static Path templatePath;
    /**
     * Config object
     */
    private static Server<Map<String, Object>> server;
    /**
     * Messages object
     */
    private static Messages<Map<String, Object>> messages;
    /**
     * Mime object
     */
    private static Mime<Map<String, Object>> mime;
    /**
     * Hosts object
     */
    private static Hosts<Map<String, Object>> hosts;
    /**
     * Chart schema Map
     */
    private static Map<String, Object> chart;
    /**
     * Default & Virtual host Map
     */
    private static Map<String, Host<?>> hostMap;
    /**
     * Template Map
     */
    private static Map<TemplateBuilder, byte[]> templateMap;
    /**
     * Constructor with home path
     * @param homePath
     */
    private Context(Path homePath) {        
        HOME_PATH = homePath;
        try {            
            if(!HOME_PATH.toFile().isDirectory() || !HOME_PATH.toFile().exists()) {
                throw new FileNotFoundException("Resource path must be directory and exist : "+HOME_PATH.toAbsolutePath().toString());
            }
            //build config environment
            ResourceHelper.extractResource("config", homePath); 
            //load configuration files
            server = new Server<Map<String, Object>>(META.SERVER.getMetaMap());
            hosts = new Hosts<Map<String, Object>>(META.HOSTS.getMetaMap());
            messages = new Messages<Map<String, Object>>(META.MESSAGES.getMetaMap());
            mime = new Mime<Map<String, Object>>(META.MIME.getMetaMap());
            chart = META.CHART.getMetaMap();
            hostMap = hosts.getHostMap();
        } catch(Exception e) {
            throw new WASException(e);
        }
    }
    /**
     * Get context instance without resource path
     * @param homePath
     * @return
     */
    public static Context initialize(Path homePath) {
        if(homePath != null) {
            context = new Context(homePath);
        }
        return context;
    }
    /**
     * Get home path
     * @return
     */
    public static Path getHomePath() {
        return HOME_PATH;
    }
    /**
     * Get template path
     * @return
     */
    public static Path getTemplates(String hostId) {
        return hosts.getTemplates(hostId);
    }
    /**
     * Get Server context
     * @return
     */
    public static Server<?> getServer() {
        return server;
    }
    /**
     * Get Hosts context
     * @return
     */
    public static Hosts<?> getHosts() {
        return hosts;
    }
    /**
     * Get Messages context
     * @return
     */
    public static Messages<?> getMessages() {
        return messages;
    }    
    /**
     * Get Mime context
     * @return
     */
    public static Mime<?> getMime() {
        return mime;
    }
    /**
     * Get Host Map
     * @return
     */
    public static Map<String, Host<?>> getHostMap() {
        return hostMap;
    }
    /**
     * Get Host object mapping with the specifiedhost ID
     * @param hostId
     * @return
     */
    public static Host<?> getHost(String hostId) {
        return hostMap.get(hostId);
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
                return server.getValue(expr);
            case HOSTS:
                return hosts.getValue(expr);
            case MESSAGES:
                return messages.getValue(expr);
            case MIME:
                return mime.getValue(expr);
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
    }   
}
