package org.chaostocosmos.leap.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.chaostocosmos.leap.common.log.LoggerFactory;

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
        this(new URL[] {LeapURLClassLoader.class.getProtectionDomain().getCodeSource().getLocation()});
    }

    /**
     * Construct with URLs
     * @param urls
     */
    public LeapURLClassLoader(URL[] urls) {
        super(urls);
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
        if(Stream.of(super.getURLs()).anyMatch(u -> u.equals(url))) {
            LoggerFactory.getLogger().debug("Parameted URL is already exists in LeapURLClassLoader: "+url.toString());
        }
        super.addURL(url);
    }
}
