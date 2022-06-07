package org.chaostocosmos.leap.http.resources;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.transaction.NotSupportedException;

import org.junit.Before;
import org.junit.Test;    
    
public class ResourceMonitorTest {

    ThreadPoolExecutor threadpool;
    ResourceMonitor resourceMonitor;

    @Before
    public void setup() throws NotSupportedException{
        this.threadpool = new ThreadPoolExecutor(30, 50, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());        
        this.resourceMonitor = ResourceMonitor.get();
    }
        
    @Test
    public void test() {
        this.resourceMonitor.start();                
    }

    public static void main(String[] args) throws NotSupportedException {
        ResourceMonitorTest test =  new ResourceMonitorTest();
        test.setup();
        test.test();
    }
}
    