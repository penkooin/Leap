package org.chaostocosmos.leap.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.exception.LeapException;
import org.junit.jupiter.api.Test; 

/**
 * Context test
 */
public class ContextTest {

    public ContextTest() throws URISyntaxException, IOException, LeapException {    
        Context context = Context.get(Paths.get("."));
        String msg = context.message().leap(901, "aaa", "bbb", "ccc", "DDD");
        msg = context.server().getMonitoringLogLevel();
        System.out.println(msg);
        msg = context.server().getMonitoringInterval();
        System.out.println(msg);
    }

    @Test
    public void testGetServerPort() {
        int port = Context.get().hosts().getDefaultPort();
        System.out.println("port: "+port);
        assertEquals(8080, port);
    }

    @Test
    public void testGetThreadPoolCoreSize() {
        int cnt = Context.get().server().getThreadPoolCoreSize();
        System.out.println("cnt: "+cnt);
        assertEquals(50, cnt);
    }

    @Test
    public void testGetResouce() throws IOException, URISyntaxException, LeapException {
        //int code = 404;
        //Path path = ResourceHelper.getResponseResourcePath("locahost");
        //byte[] bytes = ResourceHelper.getResourceContent("response.html");
        //System.out.println(new String(bytes));
        //String res = Files.readString(new File(Context.class.getClass().getResource("/WEB-INF/static/"+code+".html").toURI()).toPath());
        //assertEquals(res, html);
    }
    
    @Test
    public void testGetMsg() {
        String str = Context.get().message().http(500);
        System.out.println(str);
    }

    @Test
    public void testGetServletBeanList() {
        // List<ServiceMethodBean> list = this.context.getServiceBeanList();
        // list.stream().forEach(System.out::println);
    }

    public static void main(String[] args) throws Exception {
        new ContextTest();
    }
}
