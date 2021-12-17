package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class HttpRequestTest {

    @Test
    public void testURI() throws MalformedURLException, URISyntaxException {
        List<String> list = new ArrayList<>();
        list.add("http://localhost:8080/../../..?a=b&b=c");
        list.add("http://localhost:8080/../../../abc.html");
        list.add("http://localhost:8080/time/getTime");
        for(String str : list) {
            System.out.println("----------------------------------------");
            URL url = new URL(str);
            URI uri = url.toURI();
            System.out.println("getHost :"+uri.getHost());
            System.out.println("getPort :"+uri.getPort());
            System.out.println("getQuery :"+uri.getQuery());
            System.out.println("getPath :"+uri.getPath());
            System.out.println("getRawPath :"+uri.getRawPath());
            System.out.println("getRawQuery :"+uri.getRawQuery());
            System.out.println("getFragment :"+uri.getFragment());
            System.out.println("getScheme :"+uri.getScheme());
            System.out.println("getRawAuthority :"+uri.getRawAuthority());
            System.out.println("getRawUserInfo :"+uri.getRawUserInfo());
            System.out.println("getAuthority :"+uri.getAuthority());            
        }
    }

    @Test
    public void testMediaType() throws IOException {
        String mimeType = Files.probeContentType(Paths.get("./config.json"));
        System.out.println(mimeType);
    }

    @Test 
    public void testPath() {
        Path path1 = Paths.get("/test/aaa").normalize();
        Path path2 = Paths.get("/test/aaa/../../test1").normalize();
        
        String s1 = path1.toFile().getAbsolutePath();
        String s2 = path2.toFile().getAbsolutePath();
        System.out.println(s1);
        System.out.println(s2);
        if(s2.startsWith(s1)) {
            System.out.println("s1 parent");
        } else {
            System.out.println("s1 child");
        }
    }
    
}
