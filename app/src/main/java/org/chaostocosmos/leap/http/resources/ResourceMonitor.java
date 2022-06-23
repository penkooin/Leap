package org.chaostocosmos.leap.http.resources;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.LeapApp;
import org.chaostocosmos.leap.http.client.FormData;
import org.chaostocosmos.leap.http.client.LeapClient;
import org.chaostocosmos.leap.http.client.MIME;
import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.UNIT;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Logger;

/**
 * ResourceMonitor object
 * 
 * @author 9ins
 */
public class ResourceMonitor extends Metadata<Map<String, Object>> {
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
     * Gson parser
     */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    /**
     * Jackson Json 
     */
    private final ObjectMapper mapper = new ObjectMapper();
    /**
     * Resource monitor instance
     */
    private static ResourceMonitor resourceMonitor = null;
    /**
     * Get resource monitor
     * @return
     * @throws NotSupportedException
     */
    public static ResourceMonitor get() throws NotSupportedException {
        if(resourceMonitor == null) {
            resourceMonitor = new ResourceMonitor();
            resourceMonitor.start();
        }
        return resourceMonitor;
    }
    /**
     * Default constructor
     * @throws NotSupportedException
     */
    private ResourceMonitor() throws NotSupportedException {
        super(buildMonitorSchema());
        this.unit = Context.getServer().getMonitoringUnit();
        this.fractionPoint = Constants.DEFAULT_FRACTION_POINT;
        this.interval = Context.getServer().getMonitoringInterval();
        this.logger = LoggerFactory.createLoggerFor("monitoring", 
                      LeapApp.getHomePath().resolve(Context.getServer().getMonitoringLogs()).normalize().toString(), 
                      Context.getServer().getMonitoringLogLevel());        
    }
    /**
     * Build monitor schema Map
     * @return
     */
    private static Map<String, Object> buildMonitorSchema() {
        return new HashMap<>() {{ 
            // INTERPOLATE.AKIMA
            // INTERPOLATE.DIVIDED_DIFFERENCE
            // INTERPOLATE.LINEAR
            // INTERPOLATE.LOESS
            // INTERPOLATE.NEVILLE
            // INTERPOLATE.SPLINE
            // INTERPOLATE.NONE
            UNIT unit = Context.getServer().getMonitoringUnit();
            double totalMemory = unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize(), Constants.DEFAULT_FRACTION_POINT);
            double usedMemory = unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() - ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize(), Constants.DEFAULT_FRACTION_POINT);
            double processMemory = unit.get(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), Constants.DEFAULT_FRACTION_POINT);
            double heapUsed = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(), Constants.DEFAULT_FRACTION_POINT);
            double heapInit = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit(), Constants.DEFAULT_FRACTION_POINT);
            double heapMax = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), Constants.DEFAULT_FRACTION_POINT);
            double heapCommitted = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted(), Constants.DEFAULT_FRACTION_POINT);
            int threadMax = Context.getServer().getThreadPoolMaxSize();
            int threadCore = Context.getServer().getThreadPoolCoreSize();
            put("CPU", new HashMap<String, Object>() {{
                put("ID", "CPU");
                put("TITLE", "Leap CPU Statistics");
                put("CODEC", "PNG");
                put("SAVE_PATH", "monitor/cpu.png");
                put("GRAPH", "LINE");
                put("ALPHA", 0.5);
                put("INTERPOLATE", "LINEAR");
                put("WIDTH", 800);
                put("HEIGHT", 500);
                put("XINDEX", IntStream.range(0, 50).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("YINDEX", Arrays.asList(50, 100));
                put("LIMIT", 100);
                put("UNIT", " %");
                put("ELEMENTS", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap CPU load");
                        put("LABEL", "Leap CPU load");
                        put("COLOR", Arrays.asList(180,130,130));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "System CPU load");
                        put("LABEL", "System CPU load");
                        put("COLOR", Arrays.asList(180,180,140));
                        put("VALUES", new ArrayList<>());
                    }}
                    )
                );                
            }});            
            put("MEMORY", new HashMap<String, Object>() {{
                put("ID", "MEMORY");
                put("TITLE", "Leap Memory Statistics");
                put("CODEC", "PNG");
                put("SAVE_PATH", "monitor/memory.png");
                put("GRAPH", "AREA");
                put("ALPHA", 0.5);
                put("INTERPOLATE", "SPLINE");
                put("WIDTH", 800);
                put("HEIGHT", 500);
                put("XINDEX", IntStream.range(0, 50).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("YINDEX", Arrays.asList(processMemory, usedMemory));
                put("LIMIT", usedMemory * 2);
                put("UNIT", " "+unit.name());
                put("ELEMENTS", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Physical used");
                        put("LABEL", "Physical used");
                        put("COLOR", Arrays.asList(133, 193, 233));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Process free");
                        put("LABEL", "Process free");
                        put("COLOR", Arrays.asList(178, 186, 187));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Process used");
                        put("LABEL", "Process used");
                        put("COLOR", Arrays.asList(127,0,244));
                        put("VALUES", new ArrayList<>());
                    }}               
                    )
                );
            }});     
            put("HEAP", new HashMap<String, Object>() {{
                put("ID", "HEAP");
                put("TITLE", "Leap heap statistics");
                put("CODEC", "PNG");
                put("SAVE_PATH", "monitor/heap.png");
                put("GRAPH", "AREA");
                put("ALPHA", 0.3);
                put("INTERPOLATE", "SPLINE");
                put("WIDTH", 800);
                put("HEIGHT", 500);
                put("XINDEX", IntStream.range(0, 50).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("YINDEX", Arrays.asList(heapMax, heapInit, heapCommitted, heapUsed));
                put("LIMIT", heapMax * 1.3);
                put("UNIT", " "+unit.name());
                put("ELEMENTS", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Heap max");
                        put("LABEL", "Heap max");
                        put("COLOR", Arrays.asList(133, 157, 233));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Heap committed");
                        put("LABEL", "Heap committed");
                        put("COLOR", Arrays.asList(233, 138, 133));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Heap init");
                        put("LABEL", "Heap init");
                        put("COLOR", Arrays.asList(200, 133, 233));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Heap used");
                        put("LABEL", "Heap used");
                        put("COLOR", Arrays.asList(142, 233, 133));
                        put("VALUES", new ArrayList<>());
                    }}               
                    )
                );
            }});     
            put("THREAD", new HashMap<String, Object>() {{
                put("ID", "THREAD");
                put("TITLE", "Leap Thread Pool Statistics");
                put("CODEC", "PNG");
                put("SAVE_PATH", "monitor/thread.png");
                put("GRAPH", "LINE");
                put("ALPHA", 0.5);
                put("INTERPOLATE", "SPLINE");
                put("WIDTH", 800);
                put("HEIGHT", 500);
                put("XINDEX", IntStream.range(0, 50).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("YINDEX", Arrays.asList(threadCore, threadMax));
                put("LIMIT", threadMax * 3);
                put("UNIT", " n");
                put("ELEMENTS", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread max");
                        put("LABEL", "max");
                        put("COLOR", Arrays.asList(180,130,130));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread core");
                        put("LABEL", "core");
                        put("COLOR", Arrays.asList(150,200,158));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread active");
                        put("LABEL", "active");
                        put("COLOR", Arrays.asList(150,130,158));
                        put("VALUES", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("ELEMENT", "Leap thread queued");
                        put("LABEL", "queued");
                        put("COLOR", Arrays.asList(130,180,110));
                        put("VALUES", new ArrayList<>());
                    }})
                );
            }});   
        }};     
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
                        + "  Process Used: "+getUsedMemory()+" "+unit.name()
                        + "  Process Free: "+getFreeMemory()+" "+unit.name()
                        + "  Physical Total: "+getPhysicalTotalMemory()+" "+unit.name()
                        + "  Physical Free: "+getPhysicalFreeMemory()+" "+unit.name()
                        + "  Process CPU load: "+getProcessCpuLoad()+" "+UNIT.PCT.name()
                        + "  Process CPU time: "+getProcessCpuTime()+" "+UNIT.SE.name()
                        + "  System CPU load: "+getSystemCpuLoad()+" "+UNIT.PCT.name()
                    );             
                    setProbingValues();           
                    requestMonitorings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, this.interval);
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
     * Set probing values
     * @throws NotSupportedException
     */
    private void setProbingValues() throws NotSupportedException {
        List<Object> values = null;
        values = super.getValue("CPU.ELEMENTS.0.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getProcessCpuLoad());
        
        values = super.getValue("CPU.ELEMENTS.1.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getSystemCpuLoad());
        
        values = super.getValue("MEMORY.ELEMENTS.0.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getPhysicalUsedMemory());
                
        values = super.getValue("MEMORY.ELEMENTS.1.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getFreeMemory());

        values = super.getValue("MEMORY.ELEMENTS.2.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getUsedMemory());

        values = super.getValue("THREAD.ELEMENTS.0.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getCorePoolSize());

        values = super.getValue("THREAD.ELEMENTS.1.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getActiveCount());

        values = super.getValue("THREAD.ELEMENTS.2.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getMaximumPoolSize());

        values = super.getValue("THREAD.ELEMENTS.3.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getQueuedTaskCount());

        values = super.getValue("HEAP.ELEMENTS.0.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getProcessHeapMax());

        values = super.getValue("HEAP.ELEMENTS.1.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getProcessHeapInit());

        values = super.getValue("HEAP.ELEMENTS.2.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getProcessHeapCommitted());

        values = super.getValue("HEAP.ELEMENTS.3.VALUES");
        if(values.size() > 50) values.remove(0);
        values.add(getProcessHeapUsed());
    }
    /**
     * Request monitoring data to monitoring service
     * @throws IOException
     */
    private void requestMonitorings() throws IOException {
        String monitorJson = this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(super.getMeta());
        Map<String, FormData<?>> formDatas = Map.of("CHART", new FormData<byte[]>(MIME.TEXT_JSON, monitorJson.getBytes()));
        LeapClient.build(Context.getHosts().getDefaultHost().getHost(), Context.getHosts().getDefaultHost().getPort())
                  .addHeader("charset", "utf-8")
                  .addHeader("body-in-stream", false)
                  .post("/monitor/chart/image", null, formDatas);
    }    
    /**
     * Get thread pool core pool size
     * @return
     */
    public int getCorePoolSize() {
        return LeapApp.getThreadPool().getCorePoolSize();
    }
    /**
     * Get thread pool active count
     * @return
     */
    public int getActiveCount() {
        return LeapApp.getThreadPool().getActiveCount();
    }
    /**
     * Get thread pool largest size
     * @return
     */
    public int getLargestPoolSize() {
        return LeapApp.getThreadPool().getLargestPoolSize();
    }
    /**
     * Get thread pool maximum size
     * @return
     */
    public int getMaximumPoolSize() {
        return LeapApp.getThreadPool().getMaximumPoolSize();
    }
    /**
     * Get thread pool complated task count
     * @return
     */
    public long getCompletedTaskCount() {
        return LeapApp.getThreadPool().getCompletedTaskCount();
    }
    /**
     * Get current queued task count in thread pool
     * @return
     */
    public int getQueuedTaskCount() {
        return LeapApp.getThreadPool().getQueue().size();
    }
    /**
     * Get max memory bytes applied
     * @return
     * @throws NotSupportedException
     */
    public double getMaxMemory() throws NotSupportedException {
        return unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), fractionPoint);
    }
    /**
     * Get used memory size
     * @return
     * @throws NotSupportedException
     */
    public double getUsedMemory() throws NotSupportedException {
        return unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(), fractionPoint);
    }
    /**
     * Get free memory size
     * @return
     * @throws NotSupportedException
     */
    public double getFreeMemory() throws NotSupportedException {
        return unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() - ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(), fractionPoint);
    }
    /**
     * Get total physical memory size
     * @return
     * @throws NotSupportedException
     */
    public double getPhysicalTotalMemory() throws NotSupportedException {
        return unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize(), fractionPoint);
    }
    /**
     * Get total physical used memory
     * @return
     */
    public double getPhysicalUsedMemory() {
        return unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() - ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize(), fractionPoint);
    }
    /**
     * Get free physical memory size
     * @return
     * @throws NotSupportedException
     */
    public double getPhysicalFreeMemory() throws NotSupportedException {
        return unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize(), fractionPoint);
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
	public double getProcessHeapInit() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
		return (double) this.unit.applyUnit(value, this.fractionPoint);
	}	
    /**
     * Get used heap memory amount
     * @return
     */
    public double getProcessHeapUsed() {
        long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        return (double) this.unit.applyUnit(value, this.fractionPoint);
    }
	/**
     * Get commited heap memory amount
	 * @param unit
	 * @return
	 */
	public double getProcessHeapCommitted() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
		return (double) this.unit.applyUnit(value, this.fractionPoint);
	}	
	/**
     * Get heap memory max amount
	 * @param unit
	 * @return
	 */
	public double getProcessHeapMax() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		return (double) this.unit.applyUnit(value, this.fractionPoint);
	}
    /**
     * Get available processors
     * @return
     */
    public long getAvailableProcessors() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors();
    }
}
