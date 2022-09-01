package org.chaostocosmos.leap.http;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ThreadTest {

    ExecutorService threadpool;

    public ThreadTest() throws InterruptedException {
        this.threadpool = Executors.newCachedThreadPool();
        for(int i=0; i<10; i++) {
            this.threadpool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        test();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        this.threadpool.shutdown();
        while(!this.threadpool.isTerminated()) {
            this.threadpool.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    ArrayList<Integer> list = new ArrayList<>();

    public synchronized void test() throws InterruptedException {
        for(int i=0; i<100; i++) {
            list.add(i);
            Thread.sleep(10);
        }
        System.out.println(Thread.currentThread().getName()+" = "+list.stream().map(i -> String.valueOf(i)).collect(Collectors.joining(" ")));          
        list.clear();
    }


    public static void main(String[] args) throws InterruptedException {
        new ThreadTest();
    }
    
}
