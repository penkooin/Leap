package org.chaostocosmos.leap.http.resources;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.LeapApp;
import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UNIT;
import org.chaostocosmos.leap.http.context.Context;

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
    private ThreadPoolExecutor threadpool;
    /**
     * Unit of data size
     */
    private UNIT unit;
    /**
     * Fraction point of digit
     */
    private int fractionPoint;
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
     * Resource monitor instance
     */
    private static ResourceMonitor resourceMonitor = null;
    /**
     * Get resource monitor
     * @return
     */
    public static ResourceMonitor get() {
        if(resourceMonitor == null) {
            resourceMonitor = new ResourceMonitor();
        }                
        return resourceMonitor;
    }
    /**
     * Default constructor
     */
    private ResourceMonitor() {
        this.threadpool = LeapApp.getThreadPool();
        this.unit = Context.getServer().getMonitoringUnit();
        this.fractionPoint = Constants.DEFAULT_FRACTION_POINT;
        this.interval = Context.getServer().getMonitoringInterval();
        this.logger = LoggerFactory.createLoggerFor("monitoring", 
                                    LeapApp.getHomePath().resolve(Context.getServer().getMonitoringLogs()).normalize().toString(), 
                                    Context.getServer().getMonitoringLogLevel());
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
                        "[THREAD-MONITOR] "
                        + "  Core: " + getCorePoolSize()
                        + "  Max: " + getMaximumPoolSize()
                        + "  Active: "+getActiveCount()
                        + "  Largest: "+getLargestPoolSize()
                        + "  Queued size: "+getQueuedTaskCount()
                        + "  Task completed: "+getCompletedTaskCount()
                    );
                    logger.info(
                        "[MEMORY-MONITOR] "
                        + "  Process Max: "+getMaxMemory()+" "+unit.name()
                        + "  Process Total: "+getTotalMemory()+" "+unit.name()
                        + "  Process Used: "+getUsedMemory()+" "+unit.name()
                        + "  Process Free: "+getFreeMemory()+" "+unit.name()
                        + "  Physical Total: "+getTotalPhysicalMemory()+" "+unit.name()
                        + "  Physical Free: "+getFreePhysicalMemory()+" "+unit.name()
                        + "  Process CPU load: "+getProcessCpuLoad()+" "+UNIT.PCT.name()
                        + "  Process CPU time: "+getProcessCpuTime()+" "+UNIT.SE.name()
                        + "  System CPU load: "+getSystemCpuLoad()+" "+UNIT.PCT.name()
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
    /**
     * Get thread pool core pool size
     * @return
     */
    public int getCorePoolSize() {
        return threadpool.getCorePoolSize();
    }
    /**
     * Get thread pool active count
     * @return
     */
    public int getActiveCount() {
        return threadpool.getActiveCount();
    }
    /**
     * Get thread pool largest size
     * @return
     */
    public int getLargestPoolSize() {
        return threadpool.getLargestPoolSize();
    }
    /**
     * Get thread pool maximum size
     * @return
     */
    public int getMaximumPoolSize() {
        return threadpool.getMaximumPoolSize();
    }
    /**
     * Get thread pool complated task count
     * @return
     */
    public long getCompletedTaskCount() {
        return threadpool.getCompletedTaskCount();
    }
    /**
     * Get current queued task count in thread pool
     * @return
     */
    public int getQueuedTaskCount() {
        return threadpool.getQueue().size();
    }
    /**
     * Get max memory bytes
     * @return
     */
    public long getMaxMemoryBytes() {
        return Runtime.getRuntime().maxMemory();
    }
    /**
     * Get max memory bytes applied with fraction ImageProcessingException
     * @return
     * @throws NotSupportedException
     */
    public double getMaxMemory() throws NotSupportedException {
        return unit.get(getMaxMemoryBytes(), fractionPoint);
    }
    /**
     * Get used memory bytes
     * @return
     */
    public long getUsedMemoryBytes() {
        return getTotalMemoryBytes() - getFreeMemoryBytes();
    }
    /**
     * Get used memory size
     * @return
     * @throws NotSupportedException
     */
    public double getUsedMemory() throws NotSupportedException {
        return unit.get(getUsedMemoryBytes(), fractionPoint);
    }
    /**
     * Get total memory bytes
     * @return
     */
    public long getTotalMemoryBytes() {
        return Runtime.getRuntime().totalMemory();
    }
    /**
     * Get total memory size
     * @return
     * @throws NotSupportedException
     */
    public double getTotalMemory() throws NotSupportedException {
        return unit.get(getTotalMemoryBytes(), fractionPoint);
    }
    /**
     * Get free memory bytes
     * @return
     */
    public long getFreeMemoryBytes() {
        return Runtime.getRuntime().freeMemory();
    }  
    /**
     * Get free memory size
     * @return
     * @throws NotSupportedException
     */
    public double getFreeMemory() throws NotSupportedException {
        return unit.get(getFreeMemoryBytes(), fractionPoint);
    }
    /**
     * Get total physical memory bytes
     * @return
     */
    public long getTotalPhysicalMemoryBytes() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
    }
    /**
     * Get total physical memory size
     * @return
     * @throws NotSupportedException
     */
    public double getTotalPhysicalMemory() throws NotSupportedException {
        return unit.get(getTotalPhysicalMemoryBytes(), fractionPoint);
    }
    /**
     * Get free physical memory bytes
     * @return
     */
    public long getFreePhysicalMemoryBytes() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
    }
    /**
     * Get free physical memory size
     * @return
     * @throws NotSupportedException
     */
    public double getFreePhysicalMemory() throws NotSupportedException {
        return unit.get(getFreePhysicalMemoryBytes(), fractionPoint);
    }
    /**
     * Get available processors
     * @return
     */
    public long getAvailableProcessors() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors();
    }
    /**
     * Get process CPU load
     * @return
     */
    public double getProcessCpuLoad() {
        double load = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
        return (Math.round(load * 100d) * 100d) / 100d;
    }
    /**
     * Get process CPU time
     * @return
     * @throws NotSupportedException
     */
    public double getProcessCpuTime() throws NotSupportedException {
        long nano = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getProcessCpuTime();
        return TimeUnit.SECONDS.convert(nano, TimeUnit.NANOSECONDS);
    }
    /**
     * Get system CPU load
     * @return
     */
    public double getSystemCpuLoad() {
        double load = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad();
        return (Math.round(load * 100d) * 100d) / 100d;
    }
    /**
     * Get system CPU load average
     * @return
     */
    public double getSystemLoadAverage() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getSystemLoadAverage();
    }
	/**
     * Get heap memory init amout
	 * @param unit
	 * @return
	 */
	public float getProcessHeapInit() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
		return (float) this.unit.applyUnit(value, this.fractionPoint);
	}	
	/**
     * Get heap memory usage
	 * @param unit
	 * @return
	 */
	public float getProcessHeapUsed(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		return (float) UNIT.MB.applyUnit(value, this.fractionPoint);
	}
	/**
     * Get commited heap memory amount
	 * @param unit
	 * @return
	 */
	public float getProcessHeapCommitted(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
		return (float) UNIT.MB.applyUnit(value, this.fractionPoint);
	}	
	/**
     * Get heap memory max amount
	 * @param unit
	 * @return
	 */
	public float getProcessHeapMax(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		return (float) UNIT.MB.applyUnit(value, this.fractionPoint);
	}
	/**
	 * Get process memory usage
	 * @param unit
	 * @return
	 */
	public float getProcessMemoryUsed(UNIT unit) {
		Runtime runtime = Runtime.getRuntime();
		float value = runtime.totalMemory() - runtime.freeMemory();
		return (float) UNIT.MB.applyUnit(value, this.fractionPoint);
	}
}
