package org.chaostocosmos.leap.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.resources.ResourceHelper;
import org.junit.jupiter.api.Test;

/**
 * Context test
 */
public class ContextTest {

    public ContextTest() throws URISyntaxException, IOException, WASException {    
    }

    @Test
    public void testGetServerPort() {
        int port = Context.getHosts().getDefaultPort();
        System.out.println("port: "+port);
        assertEquals(8080, port);
    }

    @Test
    public void testGetThreadPoolCoreSize() {
        int cnt = Context.getServer().getThreadPoolCoreSize();
        System.out.println("cnt: "+cnt);
        assertEquals(50, cnt);
    }

    @Test
    public void testGetResouce() throws IOException, URISyntaxException, WASException {
        int code = 404;
        Path path = ResourceHelper.getResponseResourcePath("locahost");
        //byte[] bytes = ResourceHelper.getResourceContent("response.html");
        //System.out.println(new String(bytes));
        //String res = Files.readString(new File(Context.class.getClass().getResource("/WEB-INF/static/"+code+".html").toURI()).toPath());
        //assertEquals(res, html);
    }
    
    @Test
    public void testGetMsg() {
        String type = "debug";
        int code = 1;
        String str = Context.getMessages().getDebugMsg(code, "a", "b", "c");
        System.out.println(str);
    }

    @Test
    public void testGetServletBeanList() {
        // List<ServiceMethodBean> list = this.context.getServiceBeanList();
        // list.stream().forEach(System.out::println);
    }

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("/a/b/c/aaa.txt");
        Path path1 = Paths.get("/a/b");
        System.out.println(path.toString().substring(path1.toString().length()));
    }
}
