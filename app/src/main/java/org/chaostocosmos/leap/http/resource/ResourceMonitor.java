package org.chaostocosmos.leap.http.resource;

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
import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.common.UNIT;
import org.chaostocosmos.leap.http.context.Chart;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Level;
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
     * X axis index count
     */
    private static int xIndexCnt = 100;

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
        this.unit = UNIT.valueOf(Context.getServer().<String> getMonitoringUnit());
        this.fractionPoint = Constants.DEFAULT_FRACTION_POINT;
        this.interval = Context.getServer().<Integer> getMonitoringInterval().longValue();
        this.logger = LoggerFactory.createLoggerFor("monitoring", 
                      LeapApp.getHomePath().resolve(Context.getServer().<String> getMonitoringLogs()).normalize().toString(), 
                      Arrays.asList(Context.getServer().<String> getMonitoringLogLevel().split(",")).stream().map(s -> Level.valueOf(s)).collect(Collectors.toList()));        
    }

    /**
     * Build monitoring schema
     * @return
     */
    private static Map<String, Object> buildMonitorSchema() {
        Chart<Map<String, Object>> chart = Context.getChart();
        //Setting x index values
        chart.setValue("cpu.x-index", IntStream.range(0, chart.getValue("cpu.x-index")).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));        
        chart.setValue("memory.x-index", IntStream.range(0, chart.getValue("memory.x-index")).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
        chart.setValue("thread.x-index", IntStream.range(0, chart.getValue("thread.x-index")).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
        chart.setValue("heap.x-index", IntStream.range(0, chart.getValue("heap.x-index")).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));                       
        return chart.getMeta();
    }

    /**
     * Start monitor timer
     */
    public void start() {        
        if(this.interval >= 3000) {
            this.timer = new Timer(this.getClass().getName(), this.isDaemon);
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        logger.info(
                            "[THREAD-MONITOR] "
                            + "  Core: " + getCorePoolSize()
                            + "  Max: " + getMaximumPoolSize()
                            + "  Active: "+getTaskCount()
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
        } else {
            this.logger.info("[MONITOR OFF] Leap system monitoring interval is too low value: "+this.interval+" milliseconds. To turn on system monitoring, Please set monitoring interval value over 3000 milliseconds.");
        }
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
        values = super.getValue("cpu.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getProcessCpuLoad());                
        values = super.getValue("cpu.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getSystemCpuLoad());
        super.setValue("cpu.y-index.0", getProcessCpuLoad());
        super.setValue("cpu.y-index.1", getSystemCpuLoad());
        
        values = super.getValue("memory.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getPhysicalUsedMemory());                
        values = super.getValue("memory.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getFreeMemory());
        values = super.getValue("memory.elements.2.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getUsedMemory());
        super.setValue("memory.y-index.0", getPhysicalUsedMemory());
        super.setValue("memory.y-index.1", getFreeMemory());
        super.setValue("memory.y-index.2", getUsedMemory());

        values = super.getValue("thread.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getCorePoolSize());
        values = super.getValue("thread.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getTaskCount() - getCompletedTaskCount());
        values = super.getValue("thread.elements.2.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getMaximumPoolSize());
        values = super.getValue("thread.elements.3.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getQueuedTaskCount());
        super.setValue("thread.y-index.0", getCorePoolSize());
        super.setValue("thread.y-index.1", getTaskCount());
        super.setValue("thread.y-index.2", getMaximumPoolSize());
        super.setValue("thread.y-index.3", getQueuedTaskCount());

        values = super.getValue("heap.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getProcessHeapMax());
        values = super.getValue("heap.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getProcessHeapInit());
        values = super.getValue("heap.elements.2.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getProcessHeapCommitted());
        values = super.getValue("heap.elements.3.values");
        if(values.size() > xIndexCnt) values.remove(0);
        values.add(getProcessHeapUsed());
        super.setValue("heap.y-index.0", getProcessHeapMax());
        super.setValue("heap.y-index.1", getProcessHeapInit());
        super.setValue("heap.y-index.2", getProcessHeapCommitted());
        super.setValue("heap.y-index.3", getProcessHeapUsed());
    }
    /**
     * Request monitoring data to monitoring service
     * @throws IOException
     */
    private void requestMonitorings() throws IOException {
        String monitorJson = this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(super.getMeta());
        Map<String, FormData<?>> formDatas = Map.of("chart", new FormData<byte[]>(MIME.TEXT_JSON, monitorJson.getBytes()));
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
    public long getTaskCount() {
        return LeapApp.getThreadPool().getTaskCount();
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

    /**
     * Build monitor schema Map
     * @return
     */
    private static Map<String, Object> buildMonitorSchema1() {
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
            put("cpu", new HashMap<String, Object>() {{
                put("id", "cpu");
                put("title", "cpu");
                put("codec", "png");
                put("save_path", "monitor/cpu.png");
                put("graph", "line");
                put("alpha", 0.6);
                put("interpolate", "linear");
                put("width", 800);
                put("height", 500);
                put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("yindex", Arrays.asList(50, 100));
                put("limit", 100);
                put("unit", " %");
                put("elements", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("element", "Leap");
                        put("color", Arrays.asList(180,130,130));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "System");
                        put("color", Arrays.asList(180,180,140));
                        put("values", new ArrayList<>());
                    }}
                    )
                );                
            }});            
            put("memory", new HashMap<String, Object>() {{
                put("id", "memory");
                put("title", "memory");
                put("codec", "png");
                put("save_path", "monitor/memory.png");
                put("graph", "area");
                put("alpha", 0.6);
                put("interpolate", "spline");
                put("width", 800);
                put("height", 500);
                put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("yindex", Arrays.asList(processMemory, usedMemory));
                put("limit", totalMemory);
                put("unit", " "+unit.name());
                put("elements", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("element", "Physical Used");
                        put("color", Arrays.asList(133, 193, 233));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Physical Free");
                        put("color", Arrays.asList(178, 186, 187));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Leap Used");
                        put("color", Arrays.asList(127,0,244));
                        put("values", new ArrayList<>());
                    }}               
                    )
                );
            }});     
            put("heap", new HashMap<String, Object>() {{
                put("id", "heap");
                put("title", "heap");
                put("codec", "png");
                put("save_path", "monitor/heap.png");
                put("graph", "area");
                put("alpha", 0.6);
                put("interpolate", "spline");
                put("width", 800);
                put("height", 500);
                put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("yindex", Arrays.asList(heapMax, heapInit, heapCommitted, heapUsed));
                put("limit", heapMax * 1.3);
                put("unit", " "+unit.name());
                put("elements", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("element", "Max");
                        put("color", Arrays.asList(133, 157, 233));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Committed");
                        put("color", Arrays.asList(233, 138, 133));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Init");
                        put("color", Arrays.asList(200, 133, 233));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Used");
                        put("color", Arrays.asList(131, 203, 124));
                        put("values", new ArrayList<>());
                    }}               
                    )
                );
            }});     
            put("thread", new HashMap<String, Object>() {{
                put("id", "thread");
                put("title", "thread pool");
                put("codec", "png");
                put("save_path", "monitor/thread.png");
                put("graph", "line");
                put("alpha", 0.6);
                put("interpolate", "spline");
                put("width", 800);
                put("height", 500);
                put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % 2 == 0 ? i+"" : "").collect(Collectors.toList()));
                put("yindex", Arrays.asList(threadCore, threadMax));
                put("limit", threadMax * 3);
                put("unit", " n");
                put("elements", Arrays.asList(
                    new HashMap<String, Object>() {{ 
                        put("element", "Max");
                        put("color", Arrays.asList(180,130,130));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Core");
                        put("color", Arrays.asList(150,200,158));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Active");
                        put("color", Arrays.asList(150,130,158));
                        put("values", new ArrayList<>());
                    }},
                    new HashMap<String, Object>() {{ 
                        put("element", "Queued");
                        put("color", Arrays.asList(130,180,110));
                        put("values", new ArrayList<>());
                    }})
                );
            }});   
        }};     
    }
}
