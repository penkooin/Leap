package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.resource.ResourceProvider;
import org.chaostocosmos.leap.resource.model.ResourcesWatcherModel;
import org.junit.Before;
import org.junit.Test;    
    
public class StaticResourceManagerTest {

    @Before
    public void setup() throws IOException{
    }
        
    @Test
    public void test() throws Exception {
        System.out.println(ClassLoader.getSystemClassLoader().getResource(""));
        
    }


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        //ResourcesModel manager = ResourceManager.get("localhost");
        //manager.watch(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static"));        
    }
}
    