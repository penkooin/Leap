package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.chaostocosmos.leap.http.servlet.ServletBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Context management object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class Context {
    /**
     * Logger
     */
    public static Logger logger = LoggerFactory.getLogger(Context.class);

    /**
     * Context
     */
    private static Context context;

    /**
     * Document root path
     */
    private final Path doc_root;

    /**
     * Config path
     */
    private final Path configPath;

    /**
     * Config map
     */
    private final Map<String, Object> configMap;

    /**
     * Gson object
     */
    private Gson gson = new Gson();

    /**
     * Servlet List
     */
    private List<ServletBean> servletBeanList;

    /**
     * Constructor
     * 
     * @param doc_root
     * @throws IOException
     * @throws URISyntaxException
     */
    private Context(Path doc_root) throws IOException, URISyntaxException {
        logger.debug(doc_root.toAbsolutePath().toString());
        if(!doc_root.toFile().isDirectory() || !doc_root.toFile().exists()) {
            throw new FileNotFoundException("Resource path must be directory and exist : "+doc_root.toAbsolutePath().toString());
        }
        this.doc_root = doc_root;
        //build environment each virtual host 
        ResourceHelper.buildEnv(this.doc_root);

        this.configPath = ResourceHelper.getWebInfPath().resolve("config.yml");
        if(!this.configPath.toFile().exists()) {
            throw new FileNotFoundException("config.yml not found. Please check your configuration : "+this.configPath.toAbsolutePath().toString());
        }
        //from JSON
        //this.configMap = gson.fromJson(Files.readString(this.configPath), Map.class);
        //from YAML
        String allStr = Files.readAllLines(this.configPath).stream().collect(Collectors.joining(System.lineSeparator()));
        Yaml yaml = new Yaml();
        this.configMap = (Map<String, Object>)yaml.load(allStr);
        this.servletBeanList = getServletBeanList();
    }

    /**
     * Get context instance without resource path
     * 
     * @return
     */
    public static Context getInstance() {
        return getInstance(LeapHttpServer.WAS_HOME);
    }

    /**
     * Get context instance with resource path
     * 
     * @param WAS_HOME
     * @return
     */   
    public static Context getInstance(Path WAS_HOME) {
        if(context == null) {
            try {
                context = new Context(WAS_HOME);
            } catch(Exception e) {
                logger.error("Something wrong in Context initialize process: ", e);
            }
        }
        return context;
    }

    /**
     * Get servlet bean object
     * @param servletClassName
     * @return
     */
    public ServletBean getServletBean(String servletClassName) {
        return this.servletBeanList.stream().filter(s -> s.getServletClass().equals(servletClassName)).findAny().orElse(null);
    }

    /**
     * Get servlet bean list object
     * @return
     */
    public List<ServletBean> getServletBeanList() {
        List<ServletBean> beanList = new ArrayList<>();
        List<Object> servletList = (List<Object>)getConfigValue("server.servlet");
        for(Object o : servletList) {
            Map<String, Object> m = (Map<String,Object>)o;
            String servletName = m.get("servletName").toString();
            String servletClass = m.get("servletClass").toString();
            List<String> servletFilters = (List<String>) m.get("servletFilters");
            ServletBean bean = new ServletBean(servletName, servletClass, servletFilters);
            beanList.add(bean);
        }
        return beanList;
    }

    /**
     * Get threadpool core size
     * @return
     */
    public int getThreadPoolCoreSize() {
        return (int)getConfigValue("server.threadpool.core");
    }

    /**
     * Get thread pool maximum size
     * @return
     */
    public int getThreadPoolMaxSize() {
        return (int)getConfigValue("server.threadpool.max");
    }

    /**
     * Get thread pool keep-alive seconds
     * @return
     */
    public int getThreadPoolKeepAlive() {
        return (int)getConfigValue("server.threadpool.keep-alive");
    }

    /**
     * Get backlog
     * @return
     */
    public int getBackLog() {
        return (int)getConfigValue("server.backlog");
    }

    /**
     * Get welcome filename
     * @return
     */
    public String getWelcomFilename() {
        return getConfigValue("server.welcome").toString();
    }

    /**
     * Get server name
     * @return
     */
    public String getDefaultHost() {
        return getConfigValue("server.host").toString();
    }

    /**
     * Get server version
     * @return
     */
    public String getVersion() {
        return getConfigValue("server.version").toString();
    }

    /**
     * Get server port
     * @return
     */
    public int getDefaultPort() {
        return (int)getConfigValue("server.port");
    }
    /**
     * Get http version(HTTP/1.0)
     * @return
     */
    public String getHttpVersion() {
        return getConfigValue("server.http-version").toString();
    }

    /**
     * Get charset of server
     * @return
     */
    public Charset getServerCharset() {
        return
         Charset.forName(getConfigValue("server.charset")+"");
    }
    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public String getHttpMsg(int code) {
        return getMsg(MSG_TYPE.HTTP, code+"");
    }

    /**
     * Get debug message from config
     * @param code
     * @param args
     * @return
     */
    public String getDebugMsg(String code, Object... args) {
        return getMsg(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get info message from config
     * @param code
     * @param args
     * @return
     */
    public String getInfoMsg(String code, Object ... args) {
        return getMsg(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from config
     * @param code
     * @param args
     * @return
     */
    public String getWarnMsg(String code, Object ... args) {
        return getMsg(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from config
     * @param code
     * @param args
     * @return
     */
    public String getErrorMsg(String code, Object ... args) {
        return getMsg(MSG_TYPE.ERROR, code, args);
    }

    /**
     * Get message from config
     * @param type
     * @param code
     * @param args
     * @return
     */
    public String getMsg(MSG_TYPE type, String code, Object ... args) {
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
    public Path getResponseResource(int code) throws WASException, URISyntaxException {
        Object msg = getConfigValue("message.http."+code);
        if(msg == null) {
            throw new WASException(MSG_TYPE.HTTP, "500", new RuntimeException(getErrorMsg("error018", new Object[]{code})));
        }
        return new File(this.getClass().getResource(getConfigValue("static-resource.response").toString()).toURI()).toPath();
    }

    /**
     * Get configuration value by key path separated with dot in json. 
     * e.g. server.name or server.port
     * @param path
     * @return
     */
    public Object getConfigValue(String path) {
        return findValue(this.configMap, path.split("\\."));
    }
    
	/**
	 * Find value of key on structural data
	 * @param obj
	 * @param keys
	 * @return
	 */
	public Object findValue(Object obj, Object[] keys) {
		if (obj instanceof List) {
			List list = (List) obj;
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
			Map map = (Map) obj;
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
