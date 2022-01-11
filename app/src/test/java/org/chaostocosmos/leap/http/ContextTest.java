package org.chaostocosmos.leap.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Context test
 */
public class ContextTest {

    Context context;

    public ContextTest() throws URISyntaxException, IOException {    
        this.context = Context.getInstance();
    }

    @Test
    public void testGetServerPort() {
        int port = this.context.getDefaultPort();
        System.out.println("port: "+port);
        assertEquals(8080, port);
    }

    @Test
    public void testGetThreadPoolCoreSize() {
        int cnt = this.context.getThreadPoolCoreSize();
        System.out.println("cnt: "+cnt);
        assertEquals(50, cnt);
    }

    @Test
    public void testGetResouce() throws IOException, URISyntaxException, WASException {
        int code = 404;
        Path path = ResourceHelper.getResponseResourcePath("locahost", code);
        byte[] bytes = ResourceHelper.getInstance().getResourceContent("locahost", "response.html");
        System.out.println(new String(bytes));
        //String res = Files.readString(new File(Context.class.getClass().getResource("/WEB-INF/static/"+code+".html").toURI()).toPath());
        //assertEquals(res, html);
    }
    
    @Test
    public void testGetMsg() {
        String type = "debug";
        int code = 1;
        String str = this.context.getDebugMsg(code, "a", "b", "c");
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
