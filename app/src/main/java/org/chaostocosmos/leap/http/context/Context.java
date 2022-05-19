package org.chaostocosmos.leap.http.context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.TEMPLATE;
import org.chaostocosmos.leap.http.resources.ResourceHelper;
import org.yaml.snakeyaml.Yaml;

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
     * Yaml configuration Path
     */
    private static Path serverPath, hostsPath, messagesPath, mimePath;
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
     * Default & Virtual host Map
     */
    private static Map<String, Host<?>> hostMap;
    /**
     * Template Map
     */
    private static Map<TEMPLATE, byte[]> templateMap;
    /**
     * Constructor with home path
     * @param homePath
     */
    private Context(Path homePath) {        
        HOME_PATH = homePath;
        templatePath = HOME_PATH.resolve("config").resolve("templates");
        serverPath = HOME_PATH.resolve("config").resolve("server.yml");
        hostsPath = HOME_PATH.resolve("config").resolve("hosts.yml");
        messagesPath = HOME_PATH.resolve("config").resolve("messages.yml");
        mimePath = HOME_PATH.resolve("config").resolve("mime.yml");
        try {            
            if(!HOME_PATH.toFile().isDirectory() || !HOME_PATH.toFile().exists()) {
                throw new FileNotFoundException("Resource path must be directory and exist : "+HOME_PATH.toAbsolutePath().toString());
            }
            //build config environment
            ResourceHelper.extractResource("config", homePath); 
            //load configuration files
            server = new Server<Map<String, Object>>(load(serverPath));
            System.out.println(hostsPath);
            hosts = new Hosts<Map<String, Object>>(load(hostsPath));
            messages = new Messages<Map<String, Object>>(load(messagesPath));
            mime = new Mime<Map<String, Object>>(load(mimePath));
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
     * Load config.yml
     * @param metaPath
     * @return
     */
    private Map<String, Object> load(Path metaPath) {
        try {            
            if(metaPath == null) {
                throw new UnsupportedOperationException("Path is not set.");
            } else if(!metaPath.toFile().exists()) {
                throw new FileNotFoundException(metaPath.toFile().getName()+" not found. Please check your configuration : "+metaPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(metaPath).stream().collect(Collectors.joining(System.lineSeparator()));
            return ((Map<?, ?>)new Yaml().load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
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
    public static Path getTemplatePath() {
        return templatePath;
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
     * Save server meta
     */
    public static void saveServer() {
        save(server.getMeta(), serverPath.toFile());
    }
    /**
     * Save hosts meta
     */
    public static void saveHosts() {
        save(server.getMeta(), hostsPath.toFile());
    }
    /**
     * Save messages meta
     */
    public static void saveMessages() {
        save(server.getMeta(), messagesPath.toFile());
    }
    /**
     * Save mime meta
     */
    public static void saveMime() {
        save(server.getMeta(), mimePath.toFile());
    }
    /**
     * Save config
     * 
     * @param map
     * @param targetFile
     * @throws WASException
     */
    public static void save(Map<String, Object> map, File targetFile) throws WASException {
        Yaml yaml = new Yaml(); 
            try (FileWriter writer = new FileWriter(targetFile)) {
        } catch (IOException e) {
            throw new WASException(MSG_TYPE.ERROR, 13, e.getMessage());
        }
        yaml.dump(map);
    }   
}
