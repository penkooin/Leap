package org.chaostocosmos.leap.resource;

import java.net.URISyntaxException;
import java.net.URL;

import org.chaostocosmos.leap.common.LeapURLClassLoader;
import org.junit.Before;
import org.junit.Test;

public class LeapURLClassLoaderTest {

    LeapURLClassLoader classLoader;

    URL[] urls = new URL[] {
       ClassLoader.getSystemClassLoader().getResource("")
    };
    
    @Before
    public void init() {        
        classLoader = new LeapURLClassLoader();
    }

    @Test
    public void testPrintUrls() throws URISyntaxException {        
        System.out.println(classLoader.getResource("").toURI().toString()+"%%%%%%%%%%%%%%%%%%%%%%%");
        URL[] urls = classLoader.getURLs();
        System.out.println("Print URLs----------------------");        
        for(URL url : urls) {
            System.out.println(url);
        }
    }

    @Test
    public void testAddPath() {
        URL url = ClassLoader.getSystemClassLoader().getResource("");
        classLoader.addPath(url);
        System.out.println(classLoader.getURLs()[0]);
    }

    @Test
    public void testAddPath2() {

    }

    @Test
    public void testAddPath3() {

    }
}
