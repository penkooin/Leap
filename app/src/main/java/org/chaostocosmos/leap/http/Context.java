package org.chaostocosmos.leap.http;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.Hosts;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.ResourceHelper;
import org.chaostocosmos.leap.http.commons.UtilBox;
import org.yaml.snakeyaml.Yaml;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

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
     * Constructor
     * @param homePath
     * @throws WASException
     */
    private Context(Path homePath) throws WASException {        
        HOME_PATH = homePath;
        try {            
            if(!HOME_PATH.toFile().isDirectory() || !HOME_PATH.toFile().exists()) {
                throw new FileNotFoundException("Resource path must be directory and exist : "+HOME_PATH.toAbsolutePath().toString());
            }
            //build config environment
            ResourceHelper.extractResource("config", homePath); 
            //load configuration files
            loadConfig();
            loadMessages();
            loadMimesTypes();
            //build webapp environment
            ResourceHelper.extractResource("webapp", getDefaultDocroot());
            for(Map.Entry<String, Hosts> entry : getVirtualHosts().entrySet()) {
                ResourceHelper.extractResource("webapp", entry.getValue().getDocroot());
            }    
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Load config.yml
     * @throws WASException
     */
    public void loadConfig() throws WASException {
        try {
            configPath = HOME_PATH.resolve("config").resolve("config.yml");
            if(!configPath.toFile().exists()) {
                throw new FileNotFoundException("config.yml not found. Please check your configuration : "+configPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(configPath).stream().collect(Collectors.joining(System.lineSeparator()));
            Yaml yaml = new Yaml(); 
            configMap = ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Load messages.yml
     * @throws WASException
     */
    public void loadMessages() throws WASException {
        try {
            messagesPath = HOME_PATH.resolve("config").resolve("messages.yml");
            if(!messagesPath.toFile().exists()) {
                throw new FileNotFoundException("messages.yml not found. Please check your configuration : "+messagesPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(messagesPath).stream().collect(Collectors.joining(System.lineSeparator()));
            Yaml yaml = new Yaml(); 
            messagesMap = ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Load mime.yml
     * @throws WASException
     */
    public void loadMimesTypes() throws WASException {
        try {
            mimeTypesPath = HOME_PATH.resolve("config").resolve("mime.yml");
            if(!mimeTypesPath.toFile().exists()) {
                throw new FileNotFoundException("mime.yml not found. Please check your configuration : "+mimeTypesPath.toAbsolutePath().toString());
            }
            String lines = Files.readAllLines(mimeTypesPath).stream().collect(Collectors.joining(System.lineSeparator()));
            Yaml yaml = new Yaml(); 
            mimeTypesMap = ((Map<?, ?>)yaml.load(lines)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        } catch(Exception e) {
            throw new WASException(e);
        }
    }

    /**
     * Get context instance without resource path
     * @return
     * @throws WASException
     */
    public static Context initialize(Path homePath) {
        if(context == null) {
            try {
                context = new Context(homePath);
            } catch (WASException e) {
                e.printStackTrace();
            }
        }
        return context;
    }

    /**
     * Get Context object
     * @return
     * @throws WASException
     */
    public static Context getInstance() {
        return initialize(LeapWAS.HOME_PATH);
    }

    /**
     * Get service bean object
     * @param serviceClassName
     * @return
     */
    // public static ServiceMethodBean getServiceBean(String serviceClassName) {
    //     return servletBeanList.stream().filter(s -> s.getServiceClass().equals(serviceClassName)).findAny().orElse(null);
    // }

    /**
     * Get service bean list object
     * @return
     * @throws MalformedURLException
     */
    // public static List<ServiceMethodBean> getServiceBeanList() {
    //     List<ServiceMethodBean> beanList = new ArrayList<>();
    //     List<?> servletList = (List<?>)getConfigValue("server.servlet");
    //     for(Object o : servletList) {
    //         Map<?, ?> m = (Map<?,?>)o;
    //         String servletName = m.get("servlet-name").toString();
    //         String servletClass = m.get("servlet-class").toString();
    //         List<String> servletFilters = ((List<?>) m.get("servlet-filters")).stream().map(Object::toString).collect(Collectors.toList());
    //         ServiceMethodBean bean = new ServiceMethodBean(servletName, servletClass, servletFilters, servletFilters);
    //         beanList.add(bean);
    //     }
    //     return beanList;
    // }

    /**
     * Get all dynamic classpath URL array
     * @return
     * @throws MalformedURLException
     */
    public static URL[] getAllDynamicClasspathURLs() throws MalformedURLException {
        List<Path> paths = getAllDynamicClasspaths();
        List<URL> urls = new ArrayList<>();
        for(Path path : paths) {
            urls.add(path.toFile().toURI().toURL());
        }
        return urls.stream().toArray(URL[]::new);
    }

    /**
     * Get default dynamic classpath list
     * @return
     */
    public static List<Path> getDefaultDynamicClaspaths() {
        return ((List<?>)getConfigValue("server.dynamic-classpaths")).stream().map(p -> Paths.get(p.toString())).collect(Collectors.toList());
    }

    /**
     * Get all of dynamic classpath list
     * @return
     */
    public static List<Path> getAllDynamicClasspaths() {
        List<Path> paths = getDefaultDynamicClaspaths();
        paths.addAll(getVirtualHosts().values().stream().flatMap(h -> h.getDynamicClasspaths().stream()).collect(Collectors.toList()));
        return paths;
    }

    /**
     * Get all host Map
     * @return
     */
    public static Map<String, Hosts> getAllHosts() {
        Map<String, Hosts> hosts = getVirtualHosts();
        Hosts defaultHosts = getDefaultHosts();
        hosts.put(defaultHosts.getHost(), defaultHosts);
        return hosts;
    }

    /**
     * Get virtual host Map by port key ordered 
     * @return
     */
    public static Map<String, Hosts> getVirtualHosts() {
        return ((List<?>)getConfigValue("server.virtual-host")) 
                         .stream()
                         .map(m -> ((Map<?, ?>)m))                                                  
                         .map(e -> new Object[]{e.get("host"), 
                                                new Hosts(false,
                                                         (String)e.get("server-name"), 
                                                         (String)e.get("host"), 
                                                         Integer.parseInt(e.get("port")+""), 
                                                         ((List<?>)e.get("dynamic-classpaths")).stream().map(c -> Paths.get(c.toString())).collect(Collectors.toList()),
                                                         Paths.get((String)e.get("doc-root")), 
                                                         (String)e.get("logs"), 
                                                         UtilBox.getLogLevels((String)e.get("log-level"), ","))
                                                         })
                        .collect(Collectors.toMap(k -> (String)k[0], v -> (Hosts)v[1])); 
    }

    /**
     * Get virtual host for specified virtual host name
     * @param vhost
     * @return
     * @throws WASException
     */
    public static Hosts getVirtualHosts(final String vhost) throws WASException {
        return ((List<?>)getConfigValue("server.virtual-host"))
                        .stream()
                        .map(e -> (Map<?, ?>)e)
                        .collect(Collectors.toList())
                            .stream()
                            .map(o -> (Map<?, ?>)o)
                            .filter(m -> m.get("host").equals(vhost))
                            .map(e -> new Hosts(false,
                                                (String)e.get("server-name"), 
                                                (String)e.get("host"), 
                                                Integer.parseInt(e.get("port")+""), 
                                                ((List<?>)e.get("dynamic-classpaths")).stream().map(c -> Paths.get(c.toString())).collect(Collectors.toList()),
                                                Paths.get((String)e.get("doc-root")), 
                                                (String)e.get("logs"),
                                                UtilBox.getLogLevels((String)e.get("log-level"))))
                            .findFirst()
                            .orElseThrow(() -> new WASException(MSG_TYPE.ERROR, 19));
    }

    /**
     * Get default Hosts
     * @return
     */
    public static Hosts getDefaultHosts() {
        return new Hosts( true,
                          getDefaultServerName(),
                          getDefaultHost(),
                          getDefaultPort(),
                          getDefaultDynamicClaspaths(),
                          getDefaultDocroot(),
                          getDefaultLogPath(),
                          getDefaultLogLevel());
    }

    /**
     * Get host list matching with a port 
     * @param port
     * @return
     */
    public static List<String> getHostsByPort(int port) {
        List<String> hosts = new ArrayList<>();
        getVirtualHosts()
                    .entrySet()
                    .stream()
                    .filter(h -> h.getValue().getPort() == port)
                    .map(h -> h.getValue().getHost())
                    .forEach(h -> hosts.add(h));
        if(getDefaultPort() == port) {
            hosts.add(getDefaultHost());
        }
        return hosts;
    }

    /**
     * Get using ports
     * @return
     */
    public static int[] getUsingPorts() {
        return getAllHosts().entrySet().stream().mapToInt(e -> e.getValue().getPort()).distinct().toArray();
    }

    /**
     * Get docroot path by host name
     * @param host
     * @return
     */
    public static Path getDocroot(String host) {
        return getAllHosts().values().stream().filter(h -> h.getHost().equals(host)).map(h -> h.getDocroot()).findFirst().orElseThrow();
    }

    /**
     * Get home path
     * @return
     */
    public static Path getHomePath() {
        return HOME_PATH;
    }

    /**
     * Get upload file buffer flush size
     * @return
     */
    public static long getFlushBufferSize() {
        return (long)getConfigValue("server.flush-buffer-size");
    }

    /**
     * Get threadpool core size
     * @return
     */
    public static int getThreadPoolCoreSize() {
        return (int)getConfigValue("server.threadpool.core");
    }

    /**
     * Get thread pool maximum size
     * @return
     */
    public static int getThreadPoolMaxSize() {
        return (int)getConfigValue("server.threadpool.max");
    }

    /**
     * Get thread pool keep-alive seconds
     * @return
     */
    public static int getThreadPoolKeepAlive() {
        return (int)getConfigValue("server.threadpool.keep-alive");
    }

    /**
     * Get backlog
     * @return
     */
    public static int getBackLog() {
        return (int)getConfigValue("server.backlog");
    }

    /**
     * Get welcome filename
     * @return
     */
    public static String getWelcome() {
        return (String)getConfigValue("server.welcome");
    }

    /**
     * Get default server version
     * @return
     */
    public static String getVersion() {
        return (String)getConfigValue("server.version");
    }

    /**
     * Get default server name
     * @return
     */
    public static String getDefaultServerName() {
        return (String)getConfigValue("server.server-name");
    }

    /**
     * Get default server name
     * @return
     */
    public static String getDefaultHost() {
        return (String)getConfigValue("server.host");
    }

    /**
     * Get default server port
     * @return
     */
    public static int getDefaultPort() {
        return (int)getConfigValue("server.port");
    }

    /**
     * Get default docroot
     * @return
     */
    public static Path getDefaultDocroot() {
        return Paths.get((String)getConfigValue("server.doc-root"));
    }

    /**
     * Get default logger
     * @return
     */
    public static Logger getDefaultLogger() {
        return LoggerFactory.getLogger(getDefaultHost());
    }

    /**
     * Get default log path
     * @return
     */
    public static String getDefaultLogPath() {
        return (String)getConfigValue("server.logs");
    }

    /**
     * Get default log level
     * @return
     */
    public static List<Level> getDefaultLogLevel() {
        return UtilBox.getLogLevels((String)getConfigValue("server.log-level"));
    }

    /**
     * Set document path
     * @param docroot
     */
    public static void setDefaultDocroot(String docroot) {
        getConfigValue("server.doc-root", docroot);
    }

    /**
     * Get http version(HTTP/1.0)
     * @return
     */
    public static String getHttpVersion() {
        return (String)getConfigValue("server.http-version");
    }

    /**
     * Get charset of server
     * @return
     */
    public static Charset charset() {
        return Charset.forName(getConfigValue("server.charset")+"");
    }
    
    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public static String getHttpMsg(int code) {
        return getMsg(MSG_TYPE.HTTP, code);
    }

    /**
     * Get debug message from config
     * @param code
     * @param args
     * @return
     */
    public static String getDebugMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get info message from config
     * @param code
     * @param args
     * @return
     */
    public static String getInfoMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from config
     * @param code
     * @param args
     * @return
     */
    public static String getWarnMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from config
     * @param code
     * @param args
     * @return
     */
    public static String getErrorMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.ERROR, code, args);
    }

    /**
     * Get message from config
     * @param type
     * @param code
     * @param args
     * @return
     */
    public static String getMsg(MSG_TYPE type, int code, Object ... args) {
        Object value = getMessagesValue("messages."+type.name().toLowerCase()+"."+type.name().toLowerCase()+code);
        String msg = value == null ? "" : value.toString();
        return Arrays.stream(args).reduce(msg, (ap, a) -> ap.toString().replaceFirst("\\{\\}", a.toString())).toString();
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
            throw new WASException(MSG_TYPE.ERROR, 30, e.getMessage());
        }
        yaml.dump(configMap);
    }   
}
