package org.chaostocosmos.leap.http.commons;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * To load classes dynamically
 * 
 * @author 9ins
 */
public class DynamicURLClassLoader extends URLClassLoader {

    /**
     * Default constructor
     */
    public DynamicURLClassLoader() {
        this(new URL[0], ClassLoader.getSystemClassLoader());
    }

    /**
     * Construct with URLs
     * @param urls
     */
    public DynamicURLClassLoader(URL[] urls) {
        this(urls, ClassLoader.getSystemClassLoader());
    }

    /**
     * Construct with url paths
     * @param urls
     */
    public DynamicURLClassLoader(List<Path> urls) {
        this(urls.stream().map(p -> {
            try {      
                URL url = p.toFile().toURI().toURL();          
                LoggerFactory.getLogger().info("Add dynamic classpath to ClassLoader: "+url.toString());
                return url;
            } catch (MalformedURLException e) {
                LoggerFactory.getLogger().error(e.getMessage(), e);
                return null;
            }
        }).toArray(URL[]::new));
    }

    /**
     * Construct with URLs, parent class loader
     * @param urls
     * @param parent
     */
    public DynamicURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);        
    }

    /**
     * Add class path 
     * @param path
     * @throws MalformedURLException
     */
    public void addPath(Path path) throws MalformedURLException {                
        addPath(path.toFile());
    }

    /**
     * Add class path
     * @param path
     * @throws MalformedURLException
     */
    public void addPath(File path) throws MalformedURLException {
        addPath(path.toURI().toURL());
    }

    /**
     * Add class path
     * @param url
     */
    public void addPath(URL url) {
        LoggerFactory.getLogger().info("Adding classpath URL: "+url.toString());
        super.addURL(url);
    }
}
