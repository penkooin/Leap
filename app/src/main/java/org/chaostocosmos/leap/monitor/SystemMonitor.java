package org.chaostocosmos.leap.monitor;


import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.enums.TIME;

import ch.qos.logback.classic.Logger;

//Doing well over JDK 1.7 
//import com.sun.management.OperatingSystemMXBean;

/**
 * This class provide information of process and performance. 
 * And it's deal with amount of memory and cpu etc. 
 * Using MBean & MBean(Management eXtend Bean) of Java JMX.
 * 
 * @author 9ins
 * @since 2018.10.16
 * @version 1.0
 */
public class SystemMonitor {	
    /**
     * ThreadPool
     */
    private ThreadPoolExecutor threadpool;

    /**
     * Unit of quantity
     */
    private TIME sizeUnit;

	/**
	 * Unit of time
	 */
	private TIME timeUnit;

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
	 * System monitor instance
	 */
	private static SystemMonitor systemMonitor;

	/**
	 * Get system monitor instance
	 * @return
	 */
	public static SystemMonitor get() {
		if(systemMonitor == null) {
			systemMonitor = new SystemMonitor();
		}
		return systemMonitor;
	}
	
	/**
	 * initialize 
	 * @param threadpool
	 * @param interval
	 * @param isDaemon
	 * @param sizeUnit
	 * @param fractionPoint
	 * @param timeUnit
	 * @param logger
	 */
	public void initialize(ThreadPoolExecutor threadpool, 
						   long interval, 
						   boolean isDaemon, 
						   TIME sizeUnit, 
						   int fractionPoint, 
						   TIME timeUnit,
						   Logger logger) {
		this.threadpool = threadpool;
		this.interval = interval;
		this.isDaemon = isDaemon;
		this.sizeUnit = sizeUnit;
		this.timeUnit = timeUnit;
		this.fractionPoint = fractionPoint;
		this.logger = logger;		
	}

	/**
	 * Constructs
	 */
	public SystemMonitor() {
	}

	/**
	 * Start monitoring
	 */
	public void start() {
		if(this.timer != null) {
			this.timer.cancel();
		}
        this.timer = new Timer(this.getClass().getName(), this.isDaemon);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("[THREAD-MONITOR] ThreadPool - Core: " + threadpool.getCorePoolSize()+"  Max: " + threadpool.getMaximumPoolSize()+"  Active: "+threadpool.getActiveCount()+"  Largest: "+threadpool.getLargestPoolSize()+"  Queued size: "+threadpool.getQueue().size()+"  Task completed: "+threadpool.getCompletedTaskCount());
                    logger.info("[SYSTEM-MONITOR] CPU & MEM - Total Mem : "+getTotalPhysicalMemorySize(sizeUnit)+"  Physical Mem : "+getFreePhysicalMemorySize(sizeUnit)+"  Virtual Mem : "+getCommittedVirtualMemorySize(sizeUnit)+"  System CPU : "+getSystemCpuLoad(timeUnit)+"  Process CPU : "+getProcessCpuLoad(timeUnit)+"  Process Time : "+getProcessCpuTime(timeUnit)+"  Init : "+getProcessHeapInit(sizeUnit)+"  Used : "+getProcessHeapUsed(sizeUnit)+"  Committed : "+getProcessHeapCommitted(sizeUnit)+ "  Max : "+getProcessHeapMax(sizeUnit));    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, this.interval, this.interval);
	}	

	/**
	 * Stop monitoring
	 */
	public void stop() {
		if(this.timer != null) {
			this.timer.cancel();
		}
	}

	/**
	 * Get platform MBean attributes
	 * @param attr
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public double getPlatformMBeanAttribute(String attr, TIME unit) throws AttributeNotFoundException, 
																		   InstanceNotFoundException, 
																		   MalformedObjectNameException, 
																		   MBeanException, 
																		   ReflectionException {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		Object attribute = mBeanServer.getAttribute(new ObjectName("java.lang","type","OperatingSystem"), attr);
		if(attribute != null) {
			long value = Long.parseLong(attribute+"");
			return SIZE.MB.get(value, fractionPoint);
		}
		return 0d;
	}

	/**
	 * Get physical memory amount
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public float getFreePhysicalMemorySize(TIME unit) throws AttributeNotFoundException, 
															 InstanceNotFoundException, 
															 MalformedObjectNameException, 
															 MBeanException, 
															 ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round((float)mxBean.getFreePhysicalMemorySize()/(MB)*100f)/100f;
		return (float) getPlatformMBeanAttribute("FreePhysicalMemorySize", unit);
	}
	
	/**
	 * Get commited virtual memory
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public float getCommittedVirtualMemorySize(TIME unit) throws AttributeNotFoundException, 
																 InstanceNotFoundException, 
																 MalformedObjectNameException, 
																 MBeanException, 
																 ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round((float)mxBean.getCommittedVirtualMemorySize()/(MB)*100f)/100f;
		return (float) getPlatformMBeanAttribute("CommittedVirtualMemorySize", unit);
	}

	/**
	 * Get idled swap memory amount
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public float getFreeSwapSpaceSize(TIME unit) throws AttributeNotFoundException, 
														InstanceNotFoundException, 
														MalformedObjectNameException, 
														MBeanException, 
														ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round((float)mxBean.getFreeSwapSpaceSize()/(MB)*100f)/100f;
		return (float) getPlatformMBeanAttribute("FreeSwapSpaceSize", unit);
	}

	/**
	 * Get total physical memory amount
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public float getTotalPhysicalMemorySize(TIME unit) throws AttributeNotFoundException, 
															  InstanceNotFoundException, 
															  MalformedObjectNameException, 
															  MBeanException, 
															  ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round((float)mxBean.getTotalPhysicalMemorySize()/(MB)*100f)/100f;		
		return (float) getPlatformMBeanAttribute("TotalPhysicalMemorySize", unit);
	}

	/**
	 * Get total amount of swap
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public float getTotalSwapSpaceSize(TIME unit) throws AttributeNotFoundException, 
														 InstanceNotFoundException, 
														 MalformedObjectNameException, 
														 MBeanException, 
														 ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round((float)mxBean.getTotalSwapSpaceSize()/(MB)*100f)/100f;
		return (float) getPlatformMBeanAttribute("TotalSwapSpaceSize", unit);
	}

	/**
	 * Get CPU usage of JVM process
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public double getProcessCpuLoad(TIME unit) throws AttributeNotFoundException, 
													  InstanceNotFoundException, 
													  MalformedObjectNameException, 
													  MBeanException, 
													  ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round(mxBean.getProcessCpuLoad()*10000d)/100d;
		return getPlatformMBeanAttribute("ProcessCpuLoad", unit);
	}
	
	/**
     * Get CPU time of JVM process
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public double getProcessCpuTime(TIME unit) throws AttributeNotFoundException, 
													  InstanceNotFoundException, 
													  MalformedObjectNameException, 
													  MBeanException, 
													  ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return mxBean.getProcessCpuTime();
		return getPlatformMBeanAttribute("ProcessCpuTime", unit); //nano sec 
	}
	
	/**
     * Get system CPU usage
	 * @param unit
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public double getSystemCpuLoad(TIME unit) throws AttributeNotFoundException, 
													 InstanceNotFoundException, 
													 MalformedObjectNameException, 
													 MBeanException, 
													 ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round(mxBean.getSystemCpuLoad()*10000d)/100d;
		return getPlatformMBeanAttribute("SystemCpuLoad", unit);
	}
	
	/**
     * Get heap memory init amout
	 * @param unit
	 * @return
	 */
	public float getProcessHeapInit(TIME unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
		return (float) SIZE.MB.get(value, 2);
	}
	
	/**
     * Get heap memory usage
	 * @param unit
	 * @return
	 */
	public float getProcessHeapUsed(TIME unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		return (float) SIZE.MB.get(value, 2);
	}

	/**
     * Get commited heap memory amount
	 * @param unit
	 * @return
	 */
	public float getProcessHeapCommitted(TIME unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
		return (float) SIZE.MB.get(value, 2);
	}
	
	/**
     * Get heap memory max amount
	 * @param unit
	 * @return
	 */
	public float getProcessHeapMax(TIME unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		return (float) SIZE.MB.get(value, 2);
	}

	/**
	 * Get process memory usage
	 * @param unit
	 * @return
	 */
	public float getProcessMemoryUsed(TIME unit) {
		Runtime runtime = Runtime.getRuntime();
		long value = runtime.totalMemory() - runtime.freeMemory();
		return (float) SIZE.MB.get(value, 2);
	}

	public int getThreadPoolActiveCount() {
		return threadpool.getActiveCount();
	}

	public int getThreadPoolCoreSize() {
		return threadpool.getCorePoolSize();
	}

	public int getThreadPoolMaxSize() {
		return threadpool.getMaximumPoolSize();
	}

	public long getThreadPoolCompletedTask() {
		return threadpool.getCompletedTaskCount();
	}

	public int getThreadPoolLargestSize() {
		return threadpool.getLargestPoolSize();
	}

	public int getThreadPoolQueuedTask() {
		return threadpool.getQueue().size();
	}

	public long getThreadPoolKeepAlive(TimeUnit unit) {
		return threadpool.getKeepAliveTime(unit);
	}
}
