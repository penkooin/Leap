package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.servlet.ServletBean;
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
     * Config path
     */
    private static Path configPath;

    /**
     * Config map
     */
    private static Map<String, Object> configMap; 

    /**
     * Servlet List
     */
    private static List<ServletBean> servletBeanList;

    /**
     * Constructor
     * @param homePath
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    private Context(Path homePath) throws IOException, URISyntaxException {
        this.HOME_PATH = homePath;
        if(!this.HOME_PATH.toFile().isDirectory() || !this.HOME_PATH.toFile().exists()) {
            throw new FileNotFoundException("Resource path must be directory and exist : "+HOME_PATH.toAbsolutePath().toString());
        }
        this.configPath = this.HOME_PATH.resolve("config").resolve("config.yml"); 
        if(!this.configPath.toFile().exists()) {
            throw new FileNotFoundException("config.yml not found. Please check your configuration : "+this.configPath.toAbsolutePath().toString());
        }
        String allStr = Files.readAllLines(this.configPath).stream().collect(Collectors.joining(System.lineSeparator()));
        Yaml yaml = new Yaml(); 
        configMap = ((Map<?, ?>)yaml.load(allStr)).entrySet().stream().collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue()));
        ResourceHelper.extractResource("webapp", getDefaultDocroot());
        for(Map.Entry<String, Hosts> entry : getVirtualHosts().entrySet()) {
            ResourceHelper.extractResource("webapp", entry.getValue().getDocroot());
        }
        servletBeanList = getServletBeanList();
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
            } catch (IOException | URISyntaxException e) {
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
     * Get servlet bean object
     * @param servletClassName
     * @return
     */
    public static ServletBean getServletBean(String servletClassName) {
        return servletBeanList.stream().filter(s -> s.getServletClass().equals(servletClassName)).findAny().orElse(null);
    }

    /**
     * Get servlet bean list object
     * @return
     */
    public static List<ServletBean> getServletBeanList() {
        List<ServletBean> beanList = new ArrayList<>();
        List<?> servletList = (List<?>)getConfigValue("server.servlet");
        for(Object o : servletList) {
            Map<?, ?> m = (Map<?,?>)o;
            String servletName = m.get("servlet-name").toString();
            String servletClass = m.get("servlet-class").toString();
            List<String> servletFilters = ((List<?>) m.get("servlet-filters")).stream().map(Object::toString).collect(Collectors.toList());
            ServletBean bean = new ServletBean(servletName, servletClass, servletFilters);
            beanList.add(bean);
        }
        return beanList;
    }

    /**
     * Get all host Map
     * @return
     */
    public static Map<String, Hosts> getAllHosts() {
        Map<String, Hosts> vHosts = getVirtualHosts();
        Hosts defaultHosts = getDefaultHosts();
        vHosts.put(defaultHosts.getHost(), defaultHosts);
        return vHosts;
    }

    /**
     * Get virtual host Map by port key ordered 
     * @return
     */
    public static Map<String, Hosts> getVirtualHosts() {
        return ((List<?>)getConfigValue("server.virtual-host"))
                         .stream()
                         .map(m -> ((Map<?, ?>)m).get("host"))                                                  
                         .map(h -> {
                            try {
                                return new Object[]{h, getVirtualHosts((String)h)};
                            } catch (WASException e) {
                                LoggerFactory.getLogger(getDefaultHost()).error(e.getMessage(), e);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
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
                        .map(e -> (Map<String, Object>)e)
                        .collect(Collectors.toList())
                            .stream()
                            .map(o -> (Map<?, ?>)o)
                            .filter(m -> m.get("host").equals(vhost))
                            .map(m -> new Hosts((String)m.get("serverName"), 
                                                (String)m.get("host"), 
                                                Integer.parseInt(m.get("port")+""), 
                                                Paths.get((String)m.get("doc-root")), 
                                                (String)m.get("host"),
                                                Level.toLevel((String)m.get("log-level"))))
                            .findFirst().orElseThrow(() -> new WASException(MSG_TYPE.ERROR, "error019"));
    }

    /**
     * Get default Hosts
     * @return
     */
    public static Hosts getDefaultHosts() {
        return new Hosts(getDefaultServerName(),
                          getDefaultHost(),
                          getDefaultPort(),
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
     * Get home path
     * @return
     */
    public static Path getHomePath() {
        return HOME_PATH;
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
        return (String)getConfigValue("server.serverName");
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
    public static Level getDefaultLogLevel() {
        return Level.toLevel((String)getConfigValue("server.log-level"));
    }

    /**
     * Set document path
     * @param docroot
     */
    public static void setDefaultDocroot(String docroot) {
        setConfigValue("server.doc-root", docroot);
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
    public static Charset getServerCharset() {
        return Charset.forName(getConfigValue("server.charset")+"");
    }
    
    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public static String getHttpMsg(int code) {
        return getMsg(MSG_TYPE.HTTP, code+"");
    }

    /**
     * Get debug message from config
     * @param code
     * @param args
     * @return
     */
    public static String getDebugMsg(String code, Object... args) {
        return getMsg(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get info message from config
     * @param code
     * @param args
     * @return
     */
    public static String getInfoMsg(String code, Object ... args) {
        return getMsg(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from config
     * @param code
     * @param args
     * @return
     */
    public static String getWarnMsg(String code, Object ... args) {
        return getMsg(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from config
     * @param code
     * @param args
     * @return
     */
    public static String getErrorMsg(String code, Object ... args) {
        return getMsg(MSG_TYPE.ERROR, code, args);
    }

    /**
     * Get message from config
     * @param type
     * @param code
     * @param args
     * @return
     */
    public static String getMsg(MSG_TYPE type, String code, Object ... args) {
        Object value = getConfigValue("messages."+type.name()+"."+code);
        String msg = value == null ? "" : value.toString();
        return Arrays.stream(args).reduce(msg, (ap, a) -> ap.toString().replaceFirst("\\{\\}", a.toString())).toString();
    }    

    /**
     * Get response html file contents
     * @param code
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws WASException
     */
    public static Path getResponseResource(int code) throws WASException, URISyntaxException {
        Object msg = getConfigValue("message.http."+code);
        if(msg == null) {
            throw new WASException(MSG_TYPE.HTTP, "500", new RuntimeException(getErrorMsg("error018", new Object[]{code})));
        }
        return new File(Thread.currentThread().getContextClassLoader().getResource(getConfigValue("static-resource.response").toString()).toURI()).toPath();
    }

    /**
     * Get configuration value by key path separated with dot in json. 
     * e.g. server.name or server.port
     * @param path
     * @return
     */
    public static Object getConfigValue(String path) {
        return findValue(configMap, path.split("\\."));
    }

    /**
     * Set value to configuration
     * @param path
     * @param value
     */
    public static void setConfigValue(String path, Object value) {
        setValue(configMap, path.split("\\."), value);
    }

    /**
     * Set value to configuration Map
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

}
