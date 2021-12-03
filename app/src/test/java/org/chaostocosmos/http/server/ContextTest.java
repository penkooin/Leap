package org.chaostocosmos.http.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import org.chaostocosmos.http.Context;
import org.chaostocosmos.http.ResourceHelper;
import org.chaostocosmos.http.servlet.ServletBean;
import org.junit.jupiter.api.Test;

public class ContextTest {

    Context context;

    public ContextTest() throws URISyntaxException, IOException {
        this.context = Context.getInstance();
    }

    @Test
    public void testGetServerPort() {
        int port = this.context.getServerPort();

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
    public void testGetResouce() throws IOException, URISyntaxException {
        int code = 404;
        Path path = this.context.getResponseResource(code);
        String html = ResourceHelper.getResourceContents(path);
        System.out.println(html);
        //String res = Files.readString(new File(Context.class.getClass().getResource("/WEB-INF/static/"+code+".html").toURI()).toPath());
        //assertEquals(res, html);
    }
    
    @Test
    public void testGetMsg() {
        String type = "debug";
        String code = "debug001";
        String str = this.context.getDebugMsg(code, "a", "b", "c");
        System.out.println(str);
    }

    @Test
    public void testGetServletBeanList() {
        List<ServletBean> list = this.context.getServletBeanList();
        list.stream().forEach(System.out::println);
    }
}
