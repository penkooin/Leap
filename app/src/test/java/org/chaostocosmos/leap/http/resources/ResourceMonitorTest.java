package org.chaostocosmos.leap.http.resources;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.chaostocosmos.leap.http.commons.Unit;
import org.junit.Before;
import org.junit.Test;    
    
public class ResourceMonitorTest {

    ThreadPoolExecutor threadpool;
    ResourceMonitor resourceMonitor;

    @Before
    public void setup(){
        this.threadpool = new ThreadPoolExecutor(30, 50, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());        
        this.resourceMonitor = new ResourceMonitor(this.threadpool, 3000, false, Unit.MB, 2, null);
    }
        
    @Test
    public void test() {
        this.resourceMonitor.start();                
    }

    public static void main(String[] args) {
        ResourceMonitorTest test =  new ResourceMonitorTest();
        test.setup();
        test.test();
    }
}
    