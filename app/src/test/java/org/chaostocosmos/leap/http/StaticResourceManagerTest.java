package org.chaostocosmos.leap.http;

import java.io.IOException;

import com.drew.imaging.ImageProcessingException;

import org.chaostocosmos.leap.http.resources.Resources;
import org.chaostocosmos.leap.http.resources.StaticResourceManager;
import org.junit.Before;
import org.junit.Test;    
    
public class StaticResourceManagerTest {

    @Before
    public void setup() throws IOException{
    }
        
    @Test
    public void test() throws IOException, InterruptedException, ImageProcessingException {
        Resources manager = StaticResourceManager.get("localhost");
        //manager.watch(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static"));
    }

    public static void main(String[] args) throws IOException, InterruptedException, ImageProcessingException {
        Resources manager = StaticResourceManager.get("localhost");
        //manager.watch(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static"));        
    }
}
    