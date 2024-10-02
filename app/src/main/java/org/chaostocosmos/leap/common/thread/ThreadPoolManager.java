package org.chaostocosmos.leap.common.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.context.Context;

/**
 * Thread pool manager
 * 
 * @author 9ins
 */
public class ThreadPoolManager {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(Context.get().server().getId());

    /**
     * ThreadPoolExecutor instance
     */
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * Runnable process waiting queue
     */
    private LinkedBlockingQueue<Runnable> threadQueue;

    /**
     * ThreadPoolManager static instance
     */
    private static ThreadPoolManager threadPoolManager;

    /**
     * Initialize thread pool
     * @throws InterruptedException
     */
    private ThreadPoolManager() throws InterruptedException {
        //initialize thread queue
        threadQueue = new LinkedBlockingQueue<Runnable>();        
        //initialize thread pool
        threadPoolExecutor = new ThreadPoolExecutor(Context.get().server().getThreadPoolCoreSize(), Context.get().server().getThreadPoolMaxSize(), Context.get().server().getThreadPoolKeepAlive(), TimeUnit.SECONDS, threadQueue);                
        logger.info("================================================================================");
        logger.info("ThreadPool initialized - CORE: "+Context.get().server().getThreadPoolCoreSize()+"   MAX: "+Context.get().server().getThreadPoolMaxSize()+"   KEEP-ALIVE WHEN IDLE(seconds): "+Context.get().server().getThreadPoolKeepAlive());    
    }

    /**
     * Get ThreadPoolManager
     * @return
     */
    public static ThreadPoolManager get() {
        if(threadPoolManager == null) {
            try {
                threadPoolManager = new ThreadPoolManager();
            } catch (InterruptedException e) {
                logger.throwable(e);
            }
        }
        return threadPoolManager;
    }

    /**
     * Execute runnable process
     * @param runnable
     */
    public void execute(Runnable runnable) {
        threadPoolExecutor.submit(runnable);
    }

    /**
     * Get thread pool core size
     * @return
     */
    public int getCorePoolSize() {
        if(threadPoolExecutor != null) {
            return threadPoolExecutor.getCorePoolSize();
        }
        return -1;
    }
    /**
     * Get thread pool active count
     * @return
     */
    public long getTaskCount() {
        if(threadPoolExecutor != null) {
            return threadPoolExecutor.getTaskCount();
        }
        return -1;
    }
    /**
     * Get thread pool largest size
     * @return
     */
    public int getLargestPoolSize() {
        if(threadPoolExecutor != null) {
            return threadPoolExecutor.getLargestPoolSize();
        }
        return -1;
    }
    /**
     * Get thread pool maximum size
     * @return
     */
    public int getMaximumPoolSize() {
        if(threadPoolExecutor != null) {
            return threadPoolExecutor.getMaximumPoolSize();
        }
        return -1;
    }
    /**
     * Get thread pool complated task count
     * @return
     */
    public long getCompletedTaskCount() {
        if(threadPoolExecutor != null) {
            return threadPoolExecutor.getCompletedTaskCount();
        }
        return -1;
    }
    /**
     * Get current queued task count in thread pool
     * @return
     */
    public int getQueuedTaskCount() {
        if(threadQueue != null && threadQueue.size() > 0) {
            return threadQueue.size();
        }
        return -1;
    }    
    /**
     * Shutdown thread pool
     * @throws InterruptedException
     */
    public synchronized void shutdown() throws InterruptedException { 
        if(threadPoolExecutor != null && threadPoolExecutor.getActiveCount() < 1) {
            threadQueue.clear();
            threadPoolExecutor.shutdown();
            int countDown = 0;
            while(!threadPoolExecutor.isTerminated()) {
                TimeUnit.SECONDS.sleep(1);
                logger.info("Waiting for termination server..."+countDown);
                countDown += 1;
            }
        }
        logger.info("[THREAD POOL] Thread pool is terminated...");
    }
}
