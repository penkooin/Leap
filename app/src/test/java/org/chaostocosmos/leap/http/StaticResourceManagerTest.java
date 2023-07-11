package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.chaostocosmos.leap.ResourceManager;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.junit.Before;
import org.junit.Test;    
    
public class StaticResourceManagerTest {

    @Before
    public void setup() throws IOException{
    }
        
    @Test
    public void test() throws Exception {
        ResourcesModel manager = ResourceManager.get("localhost");
        manager.addResource(Paths.get("D:\\0.github\\leap\\home\\webapp\\WEB-INF\\static"));
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        //ResourcesModel manager = ResourceManager.get("localhost");
        //manager.watch(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static"));        
    }
}
    