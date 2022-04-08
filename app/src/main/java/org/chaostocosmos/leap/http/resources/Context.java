package org.chaostocosmos.leap.http.resources;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.LeapApplication;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
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
    private static Path configPath, messagesPath, mimeTypesPath;

    /**
     * Yaml configuration Map
     */
    private static Map<String, Object> configMap, messagesMap, mimeTypesMap;

    /**
     * Hosts Map
     */
    private static Map<String, Hosts> hostsMap;

    /**
     * Constructor with home path
     * 
     * @param homePath
     * @throws WASException
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
            configMap = loadConfig();
            messagesMap = loadMessages();
            mimeTypesMap = loadMimesTypes();
            hostsMap = getAllHostsMap();
        } catch(Exception e) {
            throw new WASException(e);
        }
        System.out.println("Context Initialized... "+new Date().toString());
    }

    /**
     * Get Context object
     * @return
     */
    public static Context get() {
        if(LeapApplication.HOME_PATH == null) {
            return null;
        }
        return initialize(LeapApplication.HOME_PATH);
    }

    /**
     * Get context instance without resource path
     * @return
     */
    public static Context initialize(Path homePath) {
        if(context == null) {
            context = new Context(homePath);
        }
        return context;
    }

    /**
     * Load config.yml
     * @return
     */
    public Map<String, Object> loadConfig() {
        try {
            configPath = HOME_PATH.resolve("config").resolve("config.yml");
            if(!configPath.toFile().exists()) {
                throw new FileNotFoundException("config.yml not found. Please check your configuration : "+configPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(configPath).stream().collect(Collectors.joining(System.lineSeparator()));
            Yaml yaml = new Yaml(); 
            return ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Load messages.yml
     * @return
     */
    public Map<String, Object> loadMessages() {
        try {
            messagesPath = HOME_PATH.resolve("config").resolve("messages.yml");
            if(!messagesPath.toFile().exists()) {
                throw new FileNotFoundException("messages.yml not found. Please check your configuration : "+messagesPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(messagesPath).stream().collect(Collectors.joining(System.lineSeparator()));
            Yaml yaml = new Yaml(); 
            return ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Load mime.yml
     * @return
     */
    public Map<String, Object> loadMimesTypes() {
        try {
            mimeTypesPath = HOME_PATH.resolve("config").resolve("mime.yml");
            if(!mimeTypesPath.toFile().exists()) {
                throw new FileNotFoundException("mime.yml not found. Please check your configuration : "+mimeTypesPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(mimeTypesPath).stream().collect(Collectors.joining(System.lineSeparator()));
            Yaml yaml = new Yaml(); 
            return ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Get all host Map
     * @return
     * @throws IOException
     */
    private static Map<String, Hosts> getAllHostsMap() throws IOException {
        Map<String, Hosts> hostsMap = new HashMap<>();
        Map<Object, Object> map = (Map<Object, Object>)getConfigValue("server.default-host");
        map.put("default", true);
        hostsMap.put(map.get("host").toString(), new Hosts(map)); 
        for(Map<Object, Object> vmap : ((List<Map<Object, Object>>)getConfigValue("server.virtual-host"))) {
            hostsMap.put(vmap.get("host").toString(), new Hosts(vmap));
        }
        return hostsMap;
    }

    /**
     * Get home path
     * @return
     */
    public static Path getLeapHomePath() {
        return HOME_PATH;
    }

    /**
     * Get hosts Map
     * @return
     */
    public static Map<String, Hosts> getHostsMap() { 
        return hostsMap;
    }

    /**
     * Get hosts object
     * @param host
     * @return
     */
    public static Hosts getHosts(String host) {
        return hostsMap.get(host);
    }

    /**
     * Get default host
     * @return
     */
    public static String getDefaultHost() {
        return (String)getConfigValue("server.default-host.host");
    }

    /**
     * Get default port
     * @return
     */
    public static int getDefaultPort() {
        return (int)getConfigValue("server.default-host.port");
    }

    /**
     * Get leap version
     * @return
     */
    public static String getLeapVersion() {
        return (String)getConfigValue("server.version");
    }

    /**
     * Get connection timeout
     * @return
     */
    public static int getConnectionTimeout() {
        return (int)getConfigValue("server.connection.connection-timeout");
    }

    /**
     * Get server backlog
     * @return
     */
    public static int getBackLog() {
        return (int)getConfigValue("server.connection.backlog");
    }    

    /**
     * Get upload file buffer flush size
     * @return
     */
    public static int getFileBufferSize() {
        return (int)getConfigValue("server.performance.file-buffer-size");
    }

    /**
     * Get threadpool core size
     * @return
     */
    public static int getThreadPoolCoreSize() {
        return (int)getConfigValue("server.performance.threadpool.core");
    }

    /**
     * Get threadpool max size
     * @return
     */
    public static int getThreadPoolMaxSize() {
        return (int)getConfigValue("server.performance.threadpool.max");
    }

    /**
     * Get threadpool keep-alive 
     * @return
     */
    public static int getThreadPoolKeepAlive() {
        return (int)getConfigValue("server.performance.threadpool.keep-alive");
    }

    /**
     * Get threadpool queue size
     * @return
     */
    public static int getThreadQueueSize() {
        return (int)getConfigValue("server.performance.threadpool.queue-size");
    }

    /**
     * Get Load-Balance redirect Map
     * @return
     */
    public static Map<String, Integer> getLoadBalanceRedirects() {
        return (Map<String, Integer>)getConfigValue("server.performance.redirect");
    }

    /**
     * Get SSL protocol
     * @return
     */
    public static String getEncryptionMethod() {
        return (String)getConfigValue("server.security.ssl.encryption");
    }

    /**
     * Get SSL key store Path
     * @return
     */
    public static Path getKeyStore() {
        return Paths.get((String)getConfigValue("server.security.ssl.keystore"));
    }

    /**
     * Get SSL key store password
     * @return
     */
    public static String getPassphrase() {
        return (String)getConfigValue("server.security.ssl.passphrase");
    }

    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public static String getHttpMsg(int code) {
        String str = getMsg(MSG_TYPE.HTTP, code);
        return str.substring(0, str.lastIndexOf(" "));
    }

    /**
     * Get HTTP message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public static String getHttpMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.HTTP, code, args);
    }

    /**
     * Get debug message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public static String getDebugMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get info message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public static String getInfoMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public static String getWarnMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public static String getErrorMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.ERROR, code, args);
    }

    /**
     * Get message from messages.yml
     * @param type
     * @param code
     * @param args
     * @return
     */
    public static String getMsg(MSG_TYPE type, int code, Object ... args) {
        Object value = getMessagesValue("messages."+type.name().toLowerCase()+"."+type.name().toLowerCase()+code);        
        String msg = value == null ? "" : value.toString();
        return Arrays.stream(args).filter(a -> a != null).reduce(msg, (ap, a) -> ap.toString().replaceFirst("\\{\\}", a.toString())).toString();
    }    

    /**
     * Get mime type value
     * @param path
     * @return
     */
    public static Object getMimeTypes(String path) {
        return findValue(mimeTypesMap, path.split("\\."));
    }

    /**
     * Get value of messages
     * @param path
     * @return
     */
    public static Object getMessagesValue(String path) {
        return findValue(messagesMap, path.split("\\."));
    }

    /**
     * Set value to messages
     * @param path
     * @param value
     */
    public static void setMessagesValue(String path, Object value) {
        setValue(messagesMap, path.split("\\."), value);
    }

    /**
     * Get value of messages
     * @param path
     * @return
     */
    public static Object getConfigValue(String path) {
        return findValue(configMap, path.split("\\."));
    }

    /**
     * Set value of config
     * @param path
     * @param value
     */
    public static void getConfigValue(String path, Object value) {
        setValue(configMap, path.split("\\."), value);
    }

    /**
     * Set value to config
     * @param obj
     * @param keys
     * @param value
     */
    public static void setValue(Object obj, Object[] keys, Object value) {
        ((Map)findValue(obj, Arrays.copyOfRange(keys, 0, keys.length-1))).put(keys[keys.length-1], value); 
    }

	/**
	 * Find value of key on structural data
	 * @param obj
	 * @param keys
	 * @return
	 */
	public static Object findValue(Object obj, Object[] keys) {
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			if (keys.length == 1) {
				if (keys[0] instanceof Integer && list.size() > (int) keys[0]) {
					return list.get((int) keys[0]);
				} else {
					return null;
				}
			} else if (keys.length > 1 && list.size() > 0) {
				Object[] subKeys = new Object[keys.length - 1];
				System.arraycopy(keys, 1, subKeys, 0, subKeys.length);
				return findValue(list.get((int) keys[0]), subKeys);
			}
		} else if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			if (keys.length == 1 && map.containsKey(keys[0])) {
				return map.get(keys[0]);
			} else if (keys.length > 1 && map.containsKey(keys[0])) {
				Object[] subKeys = new Object[keys.length - 1];
				System.arraycopy(keys, 1, subKeys, 0, subKeys.length);
				return findValue(map.get(keys[0]), subKeys);
			}
		}
		return null;
	}    

    /**
     * Save config
     * @throws WASException
     */
    public static void save() throws WASException {
        Yaml yaml = new Yaml(); 
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
        } catch (IOException e) {
            throw new WASException(MSG_TYPE.ERROR, 13, e.getMessage());
        }
        yaml.dump(configMap);
    }   
}
