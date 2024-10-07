package org.chaostocosmos.leap.http.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.resource.ResourceMonitor;
import org.junit.Before;
import org.junit.Test;

public class ResourceMonitorTest {

    ThreadPoolExecutor threadpool;
    ResourceMonitor resourceMonitor;

    @Before
    public void setup() throws NotSupportedException {
        //this.threadpool = new ThreadPoolExecutor(30, 50, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());        
        //this.resourceMonitor = ResourceMonitor.get();
    }
        
    @Test
    public void test() {
    }

    @Test
    public String resolvePlaceHolderTest(String htmlPage, Map<String, Object> placeHolderValueMap) {
        String regex = Constants.PLACEHOLDER_REGEX;
        Pattern ptrn = Pattern.compile(regex);
        Matcher matcher = ptrn.matcher(htmlPage);
        while(matcher.find()) {
            String match = matcher.group(1);
            if(placeHolderValueMap.containsKey(match)) {
                htmlPage = htmlPage.replace(match, placeHolderValueMap.get(match).toString());
            }            
        }
        return htmlPage;
    }


    public static void main(String[] args) throws NotSupportedException, IOException {
        ResourceMonitorTest test =  new ResourceMonitorTest();
        Map<String, Object> map = Map.of("@code", 200, "@message", "hello");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("webapp/static/templates/error.html")));
        String str = "";
        String line;
        while((line = br.readLine()) != null) {
            str += line;
        }
        br.close();;
        str = test.resolvePlaceHolderTest(str, map);
        System.out.println(str);
    }
}
    