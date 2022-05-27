package org.chaostocosmos.leap.http;

import java.io.IOException;

import org.chaostocosmos.leap.http.resources.Resources;
import org.chaostocosmos.leap.http.resources.ResourceManager;
import org.junit.Before;
import org.junit.Test;    
    
public class StaticResourceManagerTest {

    @Before
    public void setup() throws IOException{
    }
        
    @Test
    public void test() throws IOException, InterruptedException {
        Resources manager = ResourceManager.get("localhost");
        //manager.watch(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static"));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Resources manager = ResourceManager.get("localhost");
        //manager.watch(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static"));        
    }
}
    