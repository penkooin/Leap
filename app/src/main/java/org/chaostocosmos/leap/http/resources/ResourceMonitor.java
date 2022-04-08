package org.chaostocosmos.leap.http.resources;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.commons.Unit;

import ch.qos.logback.classic.Logger;

/**
 * ResourceMonitor object
 * 
 * @author 9ins
 */
public class ResourceMonitor {
    /**
     * ThreadPool
     */
    private static ThreadPoolExecutor threadpool;

    /**
     * Unit of quantity
     */
    private static Unit unit;

    /**
     * Fraction point of digit
     */
    private static int fractionPoint;

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Interval
     */
    private long interval;

    /**
     * Timer
     */
    private Timer timer;

    /**
     * Whether daemon thread
     */
    private boolean isDaemon;

    /**
     * Creates with ThreadPool
     * @param threadpool_
     * @param interval_
     * @param isDaemon
     * @param unit_
     * @param fractionPoint_
     * @param logger
     */
    public ResourceMonitor(ThreadPoolExecutor threadpool_, long interval_, boolean isDaemon, Unit unit_, int fractionPoint_, Logger logger) {
        unit = unit_;
        threadpool = threadpool_;
        fractionPoint = fractionPoint_;
        this.interval = interval_;
        this.isDaemon = isDaemon;
        this.logger = logger;
    }

    /**
     * Start monitor timer
     */
    public void start() {
        this.timer = new Timer(this.getClass().getName(), this.isDaemon);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info(
                        "[THREAD-MONITOR] ThreadPool - "
                        + "  Core: " + getCorePoolSize()
                        + "  Max: " + getMaximumPoolSize()
                        + "  Active: "+getActiveCount()
                        + "  Largest: "+getLargestPoolSize()
                        + "  Queued size: "+getQueuedTaskCount()
                        + "  Task completed: "+getCompletedTaskCount()
                    );
                    logger.info(
                        "[MEMORY-MONITOR] Memory - "
                        + "  Total: "+getTotalMemory()+" "+unit.name()
                        + "  Max: "+getMaxMemory()+" "+unit.name()
                        + "  Used: "+getUsedMemory()+" "+unit.name()
                        + "  Free: "+getFreeMemory()+" "+unit.name()
                        + "  Total physical: "+getTotalPhysicalMemory()+" "+unit.name()
                        + "  Free physical: "+getFreePhysicalMemory()+" "+unit.name()
                        + "  Process CPU load: "+getProcessCpuLoad()+" "+Unit.PER.name()
                        + "  Process CPU time: "+getProcessCpuTime()+" "+Unit.SE.name()
                        + "  System CPU load: "+getSystemCpuLoad()+" "+Unit.PER.name()
                        + "  System CPU load AVG: "+getSystemLoadAverage()+" "+Unit.PER.name()
                    );    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, this.interval, this.interval);
    }

    /**
     * Stop timer
     */
    public void stop() {
        if(this.timer != null) {
            this.timer.cancel();
        }
    }

    public static int getCorePoolSize() {
        return threadpool.getCorePoolSize();
    }

    public static int getActiveCount() {
        return threadpool.getActiveCount();
    }

    public static int getLargestPoolSize() {
        return threadpool.getLargestPoolSize();
    }

    public static int getMaximumPoolSize() {
        return threadpool.getMaximumPoolSize();
    }

    public static long getCompletedTaskCount() {
        return threadpool.getCompletedTaskCount();
    }

    public static int getQueuedTaskCount() {
        return threadpool.getQueue().size();
    }

    public static long getMaxMemoryBytes() {
        return Runtime.getRuntime().maxMemory();
    }

    public static double getMaxMemory() throws NotSupportedException {
        return unit.get(getMaxMemoryBytes(), fractionPoint);
    }
    
    public static long getUsedMemoryBytes() {
        return getMaxMemoryBytes() - getFreeMemoryBytes();
    }

    public static double getUsedMemory() throws NotSupportedException {
        return unit.get(getUsedMemoryBytes(), fractionPoint);
    }
    
    public static long getTotalMemoryBytes() {
        return Runtime.getRuntime().totalMemory();
    }

    public static double getTotalMemory() throws NotSupportedException {
        return unit.get(getTotalMemoryBytes(), fractionPoint);
    }
    
    public static long getFreeMemoryBytes() {
        return Runtime.getRuntime().freeMemory();
    }  

    public static double getFreeMemory() throws NotSupportedException {
        return unit.get(getFreeMemoryBytes(), fractionPoint);
    }

    public static long getTotalPhysicalMemoryBytes() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
    }

    public static double getTotalPhysicalMemory() throws NotSupportedException {
        return unit.get(getTotalPhysicalMemoryBytes(), fractionPoint);
    }

    public static long getFreePhysicalMemoryBytes() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
    }

    public static double getFreePhysicalMemory() throws NotSupportedException {
        return unit.get(getFreePhysicalMemoryBytes(), fractionPoint);
    }

    public static long getAvailableProcessors() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors();
    }

    public static double getProcessCpuLoad() {
        double load = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
        return (Math.round(load * 100d) * 100d) / 100d;
    }

    public static double getProcessCpuTime() throws NotSupportedException {
        long nano = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getProcessCpuTime();
        return TimeUnit.SECONDS.convert(nano, TimeUnit.NANOSECONDS);
    }

    public static double getSystemCpuLoad() {
        double load = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad();
        return (Math.round(load * 100d) * 100d) / 100d;
    }

    public static double getSystemLoadAverage() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getSystemLoadAverage();
    }

}
