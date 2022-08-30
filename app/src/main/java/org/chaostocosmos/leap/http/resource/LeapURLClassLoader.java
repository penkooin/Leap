package org.chaostocosmos.leap.http.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.common.LoggerFactory;

/**
 * To load classes dynamically
 * 
 * @author 9ins
 */
public class LeapURLClassLoader extends URLClassLoader {

    /**
     * Default constructor
     */
    public LeapURLClassLoader() { 
        super(new URL[0], ClassLoader.getSystemClassLoader());
    }

    /**
     * Construct with URLs
     * @param urls
     */
    public LeapURLClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    /**
     * Construct with URLs, parent class loader
     * @param urls
     * @param parent
     */
    public LeapURLClassLoader(URL[] urls, ClassLoader parent) {
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
