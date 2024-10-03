package org.chaostocosmos.leap.resource;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.client.FormData;
import org.chaostocosmos.leap.client.LeapClient;
import org.chaostocosmos.leap.client.MIME;
import org.chaostocosmos.leap.common.NetworkInterfaceManager;
import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.enums.SIZE;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.common.thread.ThreadPoolManager;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.context.Monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ResourceMonitor object
 * 
 * @author 9ins
 */
public class ResourceMonitor {

    /**
     * Monitor request interval limit milliseconds
     */
    public static final int INTERVAL_LIMIT_MILLIS = 3000;

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
     * X axis indent count
     */
    private static int xIndexIndent = 10;

    /**
     * Chart metadata
     */
    Monitor<?> chart;

    /**
     * Leap Client
     */
    LeapClient leapClient;

    /**
     * Monitor context path
     */
    String monitorContextPath;

    /**
     * Get resource monitor
     * @return
     * @throws IOException 
     */
    public static ResourceMonitor get() throws IOException {
        if(resourceMonitor == null) {
            resourceMonitor = new ResourceMonitor();
        }
        return resourceMonitor;
    }
    
    /**
     * Default constructor
     * @throws IOException 
     */
    private ResourceMonitor() throws IOException {
        this.chart = buildMonitorSchema();        
        this.fractionPoint = Constants.DEFAULT_FRACTION_POINT;
        this.monitorContextPath = Context.get().server().getMonitorContext();
        this.interval = Context.get().server().getMonitoringInterval();
        this.logger = LoggerFactory.createLoggerFor(Context.get().server().getLogs(), Context.get().server().getLogsLevel());
        String mac = NetworkInterfaceManager.getMacAddressByIp(InetAddress.getLocalHost().getHostAddress());
        Host<?> host = Context.get().hosts().getHosts().get(0);
        this.timer = new Timer(this.getClass().getName(), this.isDaemon);
        this.leapClient = LeapClient.build(host.getHost(), host.getPort())
                                    .addHeader("charset", host.charset())
                                    .addHeader("body-in-stream", false)
                                    .addHeader("mac-address", mac);
    }

    /**
     * Build monitoring schema
     * @return
     */
    private static Monitor<?> buildMonitorSchema() {
        Monitor<?> chart = Context.get().monitor();
        //Setting x index values
        chart.setValue("cpu.x-index", IntStream.range(0, chart.getValue("cpu.x-index")).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));        
        chart.setValue("memory.x-index", IntStream.range(0, chart.getValue("memory.x-index")).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));
        chart.setValue("thread.x-index", IntStream.range(0, chart.getValue("thread.x-index")).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));
        chart.setValue("heap.x-index", IntStream.range(0, chart.getValue("heap.x-index")).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));                       
        return chart;
    }

    /**
     * Start monitor timer
     */
    public void start() {        
        if(this.interval >= INTERVAL_LIMIT_MILLIS) {
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        logger.info("[THREAD-MONITOR] "
                                    + "  Core: " + ThreadPoolManager.get().getCorePoolSize()
                                    + "  Max: " + ThreadPoolManager.get().getMaximumPoolSize()
                                    + "  Active: "+ThreadPoolManager.get().getTaskCount()
                                    + "  Largest: "+ThreadPoolManager.get().getLargestPoolSize()
                                    + "  Queued size: "+ThreadPoolManager.get().getQueuedTaskCount()
                                    + "  Task completed: "+ThreadPoolManager.get().getCompletedTaskCount()
                            );
                        logger.info("[MEMORY-MONITOR] "
                                    + "  Process Max: " + getMaxMemory()
                                    + "  Process Used: " + getUsedMemory()
                                    + "  Process Free: " + getFreeMemory()
                                    + "  Physical Total: " + getPhysicalTotalMemory()
                                    + "  Physical Free: " + getPhysicalFreeMemory()
                                    + "  Process CPU load: " + getProcessCpuLoad()
                                    + "  Process CPU time: " + getProcessCpuTime()
                                    + "  System CPU load: " + getSystemCpuLoad()
                                );             
                        setProbingValues();
                        requestMonitorings();
                    } catch(SocketTimeoutException ste) {
                        logger.throwable(ste);
                    } catch (Exception e) {
                        logger.throwable(e);
                    }
                }
            }, this.interval, this.interval);    
        } else {
            this.logger.info("[MONITOR OFF] Leap system monitoring interval is too low value: "+this.interval+" milliseconds. To turn on system monitoring, Please set monitoring interval value over 3000 milliseconds.");
        }
    }

    /**
     * Stop timer
     * @throws IOException 
     */
    public void terminate() throws IOException {        
        this.leapClient.close();
        this.timer.cancel();
    }

    /**
     * Set probing values
     * @throws NotSupportedException
     */
    private void setProbingValues() throws NotSupportedException {
        List<Object> values = null;
        values = this.chart.getValue("cpu.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double processCpuLoad = SIZE.valueOf(this.chart.getValue("cpu.unit")).get(getProcessCpuLoad(), fractionPoint);
        values.add(processCpuLoad);
        values = this.chart.getValue("cpu.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double systemCpuLoad = SIZE.valueOf(this.chart.getValue("cpu.unit")).get(getSystemCpuLoad(), fractionPoint);
        values.add(systemCpuLoad);
        this.chart.setValue("cpu.y-index.0", processCpuLoad);
        this.chart.setValue("cpu.y-index.1", systemCpuLoad);
        
        values = this.chart.getValue("memory.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double physicalUsedMemory = SIZE.valueOf(this.chart.getValue("memory.unit")).get(getPhysicalUsedMemory(), fractionPoint);
        values.add(physicalUsedMemory);                
        values = this.chart.getValue("memory.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double freeMemory = SIZE.valueOf(this.chart.getValue("memory.unit")).get(getFreeMemory(), fractionPoint);
        values.add(freeMemory);
        values = this.chart.getValue("memory.elements.2.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double usedMemory = SIZE.valueOf(this.chart.getValue("memory.unit")).get(getUsedMemory(), fractionPoint);
        values.add(usedMemory);
        this.chart.setValue("memory.y-index.0", physicalUsedMemory);
        this.chart.setValue("memory.y-index.1", freeMemory);
        this.chart.setValue("memory.y-index.2", usedMemory);

        values = this.chart.getValue("thread.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        int corePoolSize = ThreadPoolManager.get().getCorePoolSize();
        values.add(corePoolSize);
        values = this.chart.getValue("thread.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        long activeTaskSize = ThreadPoolManager.get().getTaskCount() - ThreadPoolManager.get().getCompletedTaskCount();
        values.add(activeTaskSize);
        values = this.chart.getValue("thread.elements.2.values");
        if(values.size() > xIndexCnt) values.remove(0);
        int maximumPoolSize = ThreadPoolManager.get().getMaximumPoolSize();
        values.add(maximumPoolSize);
        values = this.chart.getValue("thread.elements.3.values");
        if(values.size() > xIndexCnt) values.remove(0);
        int queuedTaskSize = ThreadPoolManager.get().getQueuedTaskCount();
        values.add(queuedTaskSize);
        this.chart.setValue("thread.y-index.0", corePoolSize);
        this.chart.setValue("thread.y-index.1", activeTaskSize);
        this.chart.setValue("thread.y-index.2", maximumPoolSize);
        this.chart.setValue("thread.y-index.3", queuedTaskSize);

        values = this.chart.getValue("heap.elements.0.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double processHeapMax = SIZE.valueOf(this.chart.getValue("heap.unit")).get(getProcessHeapMax(), fractionPoint);
        values.add(processHeapMax);
        values = this.chart.getValue("heap.elements.1.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double processHeapInit = SIZE.valueOf(this.chart.getValue("heap.unit")).get(getProcessHeapInit(), fractionPoint);
        values.add(processHeapInit);
        values = this.chart.getValue("heap.elements.2.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double processHeapCommitted = SIZE.valueOf(this.chart.getValue("heap.unit")).get(getProcessHeapCommitted(), fractionPoint);
        values.add(processHeapCommitted);
        values = this.chart.getValue("heap.elements.3.values");
        if(values.size() > xIndexCnt) values.remove(0);
        double processHeapUsed = SIZE.valueOf(this.chart.getValue("heap.unit")).get(getProcessHeapUsed(), fractionPoint);
        values.add(processHeapUsed);
        this.chart.setValue("heap.y-index.0", processHeapMax);
        this.chart.setValue("heap.y-index.1", processHeapInit);
        this.chart.setValue("heap.y-index.2", processHeapCommitted);
        this.chart.setValue("heap.y-index.3", processHeapUsed);
    }

    /**
     * Request monitoring data to monitoring service
     * @throws IOException
     */
    private void requestMonitorings() throws IOException {
        String monitorJson = this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.chart.getMeta());
        Map<String, FormData<?>> formDatas = Map.of("chart", new FormData<byte[]>(MIME.TEXT_JSON, monitorJson.getBytes()));        
        this.leapClient.post(monitorContextPath, null, formDatas).close();
    }    

    /**
     * Get max memory bytes applied
     * @return
     * @throws NotSupportedException
     */
    public long getMaxMemory() throws NotSupportedException {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
    }

    /**
     * Get used memory size
     * @return
     * @throws NotSupportedException
     */
    public long getUsedMemory() throws NotSupportedException {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    /**
     * Get free memory size
     * @return
     * @throws NotSupportedException
     */
    public long getFreeMemory() throws NotSupportedException {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() - ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    /**
     * Get total physical memory size
     * @return
     * @throws NotSupportedException
     */
    public long getPhysicalTotalMemory() throws NotSupportedException {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
    }

    /**
     * Get total physical used memory
     * @return
     */
    public long getPhysicalUsedMemory() {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() - ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
    }

    /**
     * Get free physical memory size
     * @return
     * @throws NotSupportedException
     */
    public long getPhysicalFreeMemory() throws NotSupportedException {
        return ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
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
    public long getProcessCpuTime() throws NotSupportedException {
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
	public long getProcessHeapInit() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
		return value;
	}	

    /**
     * Get used heap memory amount
     * @return
     */
    public long getProcessHeapUsed() {
        long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        return value;
    }

	/**
     * Get commited heap memory amount
	 * @param unit
	 * @return
	 */
	public long getProcessHeapCommitted() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
		return value;
	}	

	/**
     * Get heap memory max amount
	 * @param unit
	 * @return
	 */
	public long getProcessHeapMax() {
		long value = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		return value;
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
    // private static Map<String, Object> buildMonitorSchema1() {
    //     return new HashMap<>() {{ 
    //         // INTERPOLATE.AKIMA
    //         // INTERPOLATE.DIVIDED_DIFFERENCE
    //         // INTERPOLATE.LINEAR
    //         // INTERPOLATE.LOESS
    //         // INTERPOLATE.NEVILLE
    //         // INTERPOLATE.SPLINE
    //         // INTERPOLATE.NONE
    //         SIZE unit = SIZE.valueOf(Context.server().getMonitoringUnit());
    //         double totalMemory = unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize(), Constants.DEFAULT_FRACTION_POINT);
    //         double usedMemory = unit.get(((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() - ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize(), Constants.DEFAULT_FRACTION_POINT);
    //         double processMemory = unit.get(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), Constants.DEFAULT_FRACTION_POINT);
    //         double heapUsed = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(), Constants.DEFAULT_FRACTION_POINT);
    //         double heapInit = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit(), Constants.DEFAULT_FRACTION_POINT);
    //         double heapMax = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax(), Constants.DEFAULT_FRACTION_POINT);
    //         double heapCommitted = unit.get(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted(), Constants.DEFAULT_FRACTION_POINT);
    //         int threadMax = Context.server().getThreadPoolMaxSize();
    //         int threadCore = Context.server().getThreadPoolCoreSize();
    //         put("cpu", new HashMap<String, Object>() {{
    //             put("id", "cpu");
    //             put("title", "cpu");
    //             put("codec", "png");
    //             put("save_path", "monitor/cpu.png");
    //             put("graph", "line");
    //             put("alpha", 0.6);
    //             put("interpolate", "linear");
    //             put("width", 800);
    //             put("height", 500);
    //             put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));
    //             put("yindex", Arrays.asList(50, 100));
    //             put("limit", 100);
    //             put("unit", " %");
    //             put("elements", Arrays.asList(
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Leap");
    //                     put("color", Arrays.asList(180,130,130));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "System");
    //                     put("color", Arrays.asList(180,180,140));
    //                     put("values", new ArrayList<>());
    //                 }}
    //                 )
    //             );                
    //         }});            
    //         put("memory", new HashMap<String, Object>() {{
    //             put("id", "memory");
    //             put("title", "memory");
    //             put("codec", "png");
    //             put("save_path", "monitor/memory.png");
    //             put("graph", "area");
    //             put("alpha", 0.6);
    //             put("interpolate", "spline");
    //             put("width", 800);
    //             put("height", 500);
    //             put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));
    //             put("yindex", Arrays.asList(processMemory, usedMemory));
    //             put("limit", totalMemory);
    //             put("unit", " "+unit.name());
    //             put("elements", Arrays.asList(
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Physical Used");
    //                     put("color", Arrays.asList(133, 193, 233));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Physical Free");
    //                     put("color", Arrays.asList(178, 186, 187));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Leap Used");
    //                     put("color", Arrays.asList(127,0,244));
    //                     put("values", new ArrayList<>());
    //                 }}               
    //                 )
    //             );
    //         }});     
    //         put("heap", new HashMap<String, Object>() {{
    //             put("id", "heap");
    //             put("title", "heap");
    //             put("codec", "png");
    //             put("save_path", "monitor/heap.png");
    //             put("graph", "area");
    //             put("alpha", 0.6);
    //             put("interpolate", "spline");
    //             put("width", 800);
    //             put("height", 500);
    //             put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));
    //             put("yindex", Arrays.asList(heapMax, heapInit, heapCommitted, heapUsed));
    //             put("limit", heapMax * 1.3);
    //             put("unit", " "+unit.name());
    //             put("elements", Arrays.asList(
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Max");
    //                     put("color", Arrays.asList(133, 157, 233));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Committed");
    //                     put("color", Arrays.asList(233, 138, 133));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Init");
    //                     put("color", Arrays.asList(200, 133, 233));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Used");
    //                     put("color", Arrays.asList(131, 203, 124));
    //                     put("values", new ArrayList<>());
    //                 }}               
    //                 )
    //             );
    //         }});     
    //         put("thread", new HashMap<String, Object>() {{
    //             put("id", "thread");
    //             put("title", "thread pool");
    //             put("codec", "png");
    //             put("save_path", "monitor/thread.png");
    //             put("graph", "line");
    //             put("alpha", 0.6);
    //             put("interpolate", "spline");
    //             put("width", 800);
    //             put("height", 500);
    //             put("xindex", IntStream.range(0, xIndexCnt).mapToObj(i -> i % xIndexIndent == 0 ? i+"" : "").collect(Collectors.toList()));
    //             put("yindex", Arrays.asList(threadCore, threadMax));
    //             put("limit", threadMax * 3);
    //             put("unit", " n");
    //             put("elements", Arrays.asList(
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Max");
    //                     put("color", Arrays.asList(180,130,130));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Core");
    //                     put("color", Arrays.asList(150,200,158));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Active");
    //                     put("color", Arrays.asList(150,130,158));
    //                     put("values", new ArrayList<>());
    //                 }},
    //                 new HashMap<String, Object>() {{ 
    //                     put("element", "Queued");
    //                     put("color", Arrays.asList(130,180,110));
    //                     put("values", new ArrayList<>());
    //                 }})
    //             );
    //         }});   
    //     }};     
    // }
}
