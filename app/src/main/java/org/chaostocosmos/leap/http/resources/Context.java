package org.chaostocosmos.leap.http.resources;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.LeapApp;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.commons.Filtering;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.user.GRANT;
import org.chaostocosmos.leap.http.user.User;
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
        if(LeapApp.HOME_PATH == null) {
            return null;
        }
        return initialize(LeapApp.HOME_PATH);
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
            Map<String, Object> configMap = ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
            return configMap;
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
        if(hostsMap == null) {
            hostsMap = getHostIds().stream().map(n -> new Object[]{n, newHosts(n)}).collect(Collectors.toMap(k -> (String)k[0], v -> (Hosts)v[1]));
            if(hostsMap.values().stream().filter(h -> h.isDefaultHost()).count() != 1) {
                throw new IllegalArgumentException("Default host must be defined only one in config.");
            }
        }
        return hostsMap;
    }

    /**
     * New Hosts from config
     * @param hostId
     * @return
     */
    public static Hosts newHosts(String hostId) {        
        return new Hosts((boolean)getConfigValue("hosts."+hostId+".default"), 
                        hostId, 
                        PROTOCOL.getProtocol((String)getConfigValue("hosts."+hostId+".protocol")),
                        Charset.forName((String)getConfigValue("hosts."+hostId+".charset")),
                        (String)getConfigValue("hosts."+hostId+".host"), 
                        (int)getConfigValue("hosts."+hostId+".port"), 
                        (List<User>)((List<Map<?, ?>>)getConfigValue("hosts."+hostId+".users")).stream().map(m -> new User(m.get("username").toString(), m.get("password").toString(), GRANT.valueOf(m.get("grant").toString()))).collect(Collectors.toList()),
                        !getConfigValue("hosts."+hostId+".dynamic-classpath").equals("") ? Paths.get((String)getConfigValue("hosts."+hostId+".dynamic-classpath")) : null,
                        new Filtering(getConfigValue("hosts."+hostId+".dynamic-packages") == null ? new ArrayList<>() : (List<String>)getConfigValue("hosts."+hostId+".dynamic-packages")), 
                        new Filtering(getConfigValue("hosts."+hostId+".spring-jpa-packages") == null ? new ArrayList<>() : (List<String>)getConfigValue("hosts."+hostId+".spring-jpa-packages")), 
                        new Filtering((List<String>)((Map<?, ?>)getConfigValue("hosts."+hostId+".resource")).get("in-memory-filters")),
                        new Filtering((List<String>)((Map<?, ?>)getConfigValue("hosts."+hostId+".resource")).get("access-filters")),
                        new Filtering((List<String>)((Map<?, ?>)getConfigValue("hosts."+hostId+".resource")).get("forbidden-filters")),
                        new Filtering((List<String>)((Map<?, ?>)getConfigValue("hosts."+hostId+".ip-filter")).get("allowed")),
                        new Filtering((List<String>)((Map<?, ?>)getConfigValue("hosts."+hostId+".ip-filter")).get("forbidden")), 
                        ((List<?>)getConfigValue("hosts."+hostId+".error-filters")).stream().map(f -> ClassUtils.getClass(ClassLoader.getSystemClassLoader(), f.toString().trim())).collect(Collectors.toList()), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")).resolve("webapp"), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")).resolve("webapp").resolve("WEB-INF"), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")).resolve("webapp").resolve("WEB-INF").resolve("static"), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")).resolve("webapp").resolve("services"), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")).resolve("webapp").resolve("WEB-INF").resolve("template"), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".doc-root")).resolve("webapp").resolve("WEB-INF").resolve("template").resolve((String)getConfigValue("hosts."+hostId+".welcome")).toFile(), 
                        Paths.get((String)getConfigValue("hosts."+hostId+".logs")), 
                        UtilBox.getLogLevels((String)getConfigValue("hosts."+hostId+".log-level"), ","), 
                        null
                    );
    }

    /**
     * Get hosts object
     * @param hostId
     * @return
     */
    public static Hosts getHosts(String hostId) {
        return hostsMap.get(hostId);
    }

    /**
     * Get all of host name in config
     * @return
     */
    public static List<String> getHostIds() {
        return ((Map<Object, Object>)getConfigValue("hosts"))
                                    .entrySet()
                                    .stream()
                                    .map(e -> (String)e.getKey())
                                    .collect(Collectors.toList());
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
     * Get default Hosts object
     * @return
     */
    public static Hosts getDefaultHosts() {
        return hostsMap.entrySet().stream().filter(e -> e.getValue().isDefaultHost()).findFirst().orElse(null).getValue();
    }

    /**
     * Get default host
     * @return
     */
    public static String getDefaultHost() {
        return hostsMap.entrySet()
                       .stream()
                       .filter(e -> e.getValue().isDefaultHost())
                       .findFirst()
                       .orElseThrow(() -> new RuntimeException("There isn't default host in configuration. Please check Leap config.yml"))
                       .getValue()
                       .getHost();
    }

    /**
     * Get default port
     * @return
     */
    public static int getDefaultPort() {
        return hostsMap.entrySet()
                       .stream()
                       .filter(e -> e.getValue().isDefaultHost())
                       .findFirst()
                       .orElseThrow(() -> new RuntimeException("There isn't default host in configuration. Please check Leap config.yml"))
                       .getValue()
                       .getPort();
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
        return str;
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
					throw new IllegalArgumentException("There isn't exist value of key: "+Arrays.asList(keys).stream().map(o -> o+"").collect(Collectors.joining(".")));
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
		throw new IllegalArgumentException("There isn't exist value of key: "+Arrays.asList(keys).stream().map(o -> o+"").collect(Collectors.joining(".")));
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
