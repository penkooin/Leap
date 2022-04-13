package org.chaostocosmos.leap.http.resources;


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

import org.chaostocosmos.leap.http.commons.Unit;
import org.chaostocosmos.leap.http.enums.UNIT;

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
	
	public static final double PCT = 100d;
	public static final long SEC = 1000000000L; //nano sec
	public static final long KB = 1024;
	public static final long MB = 1024*1024;	
	public static final long GB = 1024*1024*1024;
	public static final long TB = 1024*1024*1024*1024;
	public static final long PB = 1024*1024*1024*1024*1024;

    /**
     * ThreadPool
     */
    private static ThreadPoolExecutor threadpool;

    /**
     * Unit of quantity
     */
    private Unit unit;

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
	 * Constructs
	 * @param threadpool_
	 * @param interval
	 * @param isDaemon
	 * @param unit
	 * @param fractionPoint
	 * @param logger
	 */
	public SystemMonitor(ThreadPoolExecutor threadpool_, long interval, boolean isDaemon, Unit unit, int fractionPoint, Logger logger) {
		threadpool = threadpool_;
		this.interval = interval;
		this.isDaemon = isDaemon;
		this.unit = unit;
		this.fractionPoint = fractionPoint;
		this.logger = logger;		
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
                    logger.info(
                        "[THREAD-MONITOR] ThreadPool - "
                        + "  Core: " + threadpool.getCorePoolSize()
                        + "  Max: " + threadpool.getMaximumPoolSize()
                        + "  Active: "+threadpool.getActiveCount()
                        + "  Largest: "+threadpool.getLargestPoolSize()
                        + "  Queued size: "+threadpool.getQueue().size()
                        + "  Task completed: "+threadpool.getCompletedTaskCount()
                    );
                    logger.info(
                        "[MEMORY-MONITOR] Memory - "
						+ "Total Mem : "+SystemMonitor.getTotalPhysicalMemorySize(UNIT.GB)
						+ "Physical Mem : "+SystemMonitor.getFreePhysicalMemorySize(UNIT.GB)
						+ "Virtual Mem : "+SystemMonitor.getCommittedVirtualMemorySize(UNIT.GB)
						+ "System CPU : "+SystemMonitor.getSystemCpuLoad(UNIT.PCT)
						+ "Process CPU : "+SystemMonitor.getProcessCpuLoad(UNIT.PCT)
						+ "Process Time : "+SystemMonitor.getProcessCpuTime(UNIT.SEC)
						+ "Init : "+getProcessHeapInit(UNIT.MB)
						+ "Used : "+getProcessHeapUsed(UNIT.MB)
						+ "Committed : "+getProcessHeapCommitted(UNIT.MB)
						+ "Max : "+getProcessHeapMax(UNIT.MB)
                    );    
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
	public static double getPlatformMBeanAttribute(String attr, UNIT unit) throws AttributeNotFoundException, 
																				  InstanceNotFoundException, 
																				  MalformedObjectNameException, 
																				  MBeanException, 
																				  ReflectionException {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		Object attribute = mBeanServer.getAttribute(new ObjectName("java.lang","type","OperatingSystem"), attr);
		if(attribute != null) {
			double value = Double.parseDouble(attribute+"");
			return applyUnit(value, unit.toString());
		}
		return 0d;
	}
	
	/**
	 * Apply unit.
	 * @param value
	 * @param unit
	 * @return
	 */
	public static double applyUnit(double value, String unit) {
		double retValue = 0d; 
		if(unit.equalsIgnoreCase("%") || unit.equalsIgnoreCase("PCT") || unit.equalsIgnoreCase("PERCENT")) {
			retValue = Math.round(value*PCT*PCT)/PCT;
		} else if(unit.equalsIgnoreCase("QTY")) {
			retValue = value;
		} else if(unit.equalsIgnoreCase("NO")) {
			retValue = value;
		} else if(unit.equalsIgnoreCase("B")) {
			retValue = value;
		} else if(unit.equalsIgnoreCase("KB")) {
			retValue = Math.round(value/KB*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("MB")) {
			retValue = Math.round(value/MB*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("GB")) {
			retValue = Math.round(value/GB*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("TB")) {
			retValue = Math.round(value/TB*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("PB")) {
			retValue = Math.round(value/PB*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("NS")) {
			retValue = Math.round(value/1000000000d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("MS")) {
			retValue = Math.round(value/1000000d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("ML")) {
			retValue = Math.round(value/1000d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("SEC")) {
			retValue = value;
		} else if(unit.equalsIgnoreCase("MIN")) {
			retValue = Math.round(value/60d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("HR")) {
			retValue = Math.round(value/60d/60d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("DY")) {
			retValue = Math.round(value/60d/60d/24d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("WK")) {
			retValue = Math.round(value/60d/60d/24d/7d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("MO")) {
			retValue = Math.round(value/60d/60d/24d/7d/30d*1000d)/1000d;
		} else if(unit.equalsIgnoreCase("YR")) {
			retValue = Math.round(value/60d/60d/24d/7d/30d/365d*1000d)/1000d;
		} else {
			throw new IllegalArgumentException("UNIT param is wrong value : "+unit);
		}
		retValue = (retValue < 0)?retValue*-1:retValue;
		return retValue;
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
	public static float getFreePhysicalMemorySize(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static float getCommittedVirtualMemorySize(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static float getFreeSwapSpaceSize(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static float getTotalPhysicalMemorySize(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static float getTotalSwapSpaceSize(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static double getProcessCpuLoad(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static double getProcessCpuTime(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
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
	public static double getSystemCpuLoad(UNIT unit) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException {
		//OperatingSystemMXBean mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//return Math.round(mxBean.getSystemCpuLoad()*10000d)/100d;
		return getPlatformMBeanAttribute("SystemCpuLoad", unit);
	}
	
	/**
     * Get heap memory init amout
	 * @param unit
	 * @return
	 */
	public static float getProcessHeapInit(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
		return (float) applyUnit(value, unit.name());
	}
	
	/**
     * Get heap memory usage
	 * @param unit
	 * @return
	 */
	public static float getProcessHeapUsed(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		return (float) applyUnit(value, unit.name());
	}

	/**
     * Get commited heap memory amount
	 * @param unit
	 * @return
	 */
	public static float getProcessHeapCommitted(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
		return (float) applyUnit(value, unit.name());		
	}
	
	/**
     * Get heap memory max amount
	 * @param unit
	 * @return
	 */
	public static float getProcessHeapMax(UNIT unit) {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		return (float) applyUnit(value, unit.name());		
	}

	/**
	 * Get process memory usage
	 * @param unit
	 * @return
	 */
	public static float getProcessMemoryUsed(UNIT unit) {
		Runtime runtime = Runtime.getRuntime();
		float value = runtime.totalMemory() - runtime.freeMemory();
		return (float) applyUnit(value, unit.name());
	}

	public static int getThreadPoolActiveCount() {
		return threadpool.getActiveCount();
	}

	public static int getThreadPoolCoreSize() {
		return threadpool.getCorePoolSize();
	}

	public static int getThreadPoolMaxSize() {
		return threadpool.getMaximumPoolSize();
	}

	public static long getThreadPoolCompletedTask() {
		return threadpool.getCompletedTaskCount();
	}

	public static int getThreadPoolLargestSize() {
		return threadpool.getLargestPoolSize();
	}

	public static int getThreadPoolQueuedTask() {
		return threadpool.getQueue().size();
	}

	public static long getThreadPoolKeepAlive(TimeUnit unit) {
		return threadpool.getKeepAliveTime(unit);
	}
}
