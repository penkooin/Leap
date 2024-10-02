package org.chaostocosmos.leap.common.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**  
 * Logger : 
 * Description : 
 * Logging for debug information
 *  
 * Modification Information  
 *  ---------   ---------   -------------------------------
 *  20161118	9ins		draft
 *  
 * @author 9ins
 * @since 20161118
 * @version 1.0
 */
public class Logger {

    /**
     * File separator
     */
    public static String FS = System.getProperty("file.separator");    

	/**
	 * align column size
	 */
	public static final int ALIGN_COUNT = 180;	

	/**
	 * log file duration to split (default : 12 hour)
	 */
	public static final int DEFAULT_LIMIT_HOUR = 12;

	/**
	 * log file size to split(default : 100MB)
	 */
	public static final int DEFAULT_LIMIT_SIZE = 1024 * 1024 * 100;

    /**
     * default log file date pattern
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy_MM_dd_HH_mm_ss";

    /**
     * default log file suffix
     */
    public static final String DEFAULT_LOG_SUFFIX = ".log";

    /**
     * default charset name
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * default log level
     */
    public static final LEVEL DEFAULT_LOG_LEVEL = LEVEL.INFO;

    /**
	 * encoding(default : UTF-8)
	 */
	public String charset;

	/**
	 * log file size to split(default : 100MB)
	 */
	public long logFileLimitSize;

    /**
     * Log change interval
     */
    private long chageLogFileInterval;    

    /**
     * Whether appending
     */
	private boolean isAppendMode;

    /**
     * Whether print system IO
     */
    private boolean isSysout = true;;

    /**
     * PrintWriter object
     */
    private PrintWriter log;    

    /**
     * PrintStream object
     */
    private PrintStream ps;    

    /**
     * Start time
     */
    private long startTime;    

    /**
     * Log file object
     */
    private File logFile;    

    /**
     * Log directory
     */
    private String logDir;    

    /**
     * log file prefix
     */
    private String logFilePrefix;

	/**
	 * log file suffix
	 */
    private String logFileSuffix;    

    /**
     * log file date pattern
     */
    private String logFileDatePattern;

    /**
     * Logger name
     */
    private String loggerName;

    /**
     * Log level filtering flag
     */
    public Map<LEVEL, Boolean> FLAG = new HashMap<>() {{
        put(LEVEL.INFO, false);
        put(LEVEL.WARN, false);
        put(LEVEL.DEBUG, false);
        put(LEVEL.ERROR, false);
        put(LEVEL.FATAL, false);
        put(LEVEL.EXCEP, false);
    }};

    /**
     * Constructor
     * @param logPath
     */
    public Logger(Path logPath) {
        this(
            logPath, 
            DEFAULT_LOG_LEVEL
            );
    }

    /**
     * Constructor
     * @param logPath
     * @param logLevel
     */
    public Logger(
                Path logPath, 
                LEVEL logLevel
                ) {
        this(
            logPath, 
            DEFAULT_DATE_PATTERN, 
            logLevel
            );
    }

    /**
     * Constructor
     * @param logPath
     * @param logLevel
     * @param charset
     */
    public Logger(
                Path logPath, 
                LEVEL logLevel, 
                String charset
                ) {
        this(
            logPath, 
            DEFAULT_DATE_PATTERN, 
            logLevel, 
            charset
            );
    }

    /**
     * Constructor
     * @param logPath
     * @param logFilePattern
     * @param logLevel
     */
    public Logger(
                Path logPath, 
                String logFilePattern, 
                LEVEL logLevel
                ) {
        this(
            logPath, 
            logFilePattern, 
            logLevel, 
            DEFAULT_CHARSET
            );
    }

    /**
     * Constructor
     * @param logPath
     * @param logFilePattern
     * @param logLevel
     * @param charset
     */
    public Logger(
                Path logPath, 
                String logFilePattern, 
                LEVEL logLevel, 
                String charset
                ) {
        this(
            logPath, 
            logFilePattern, 
            logLevel, 
            true, 
            charset
            );
    }

    /**
     * Constructor
     * @param logPath
     * @param logFilePattern
     * @param logLevel
     * @param isAppendMode
     * @param charset
     */
    public Logger(
                Path logPath, 
                String logFilePattern, 
                LEVEL logLevel, 
                boolean isAppendMode, 
                String charset
                ) {
        this(
            DEFAULT_LIMIT_HOUR,
            DEFAULT_LIMIT_SIZE,
            logPath,
            logFilePattern, 
            logLevel, 
            isAppendMode, 
            charset
            );
    }

    /**
     * Constructor
     * @param logFileLimitHour
     * @param logFileLimitSize
     * @param logPath
     * @param logFileDatePattern
     * @param logLevel
     * @param isAppendMode
     * @param charset
     */
    public Logger(int logFileLimitHour, 
                  int logFileLimitSize, 
                  Path logPath,
                  String logFileDatePattern,
                  LEVEL logLevel, 
                  boolean isAppendMode,
                  String charset) {
    	init(
            logFileLimitHour, 
            logFileLimitSize, 
            logPath,
            logFileDatePattern, 
            logLevel, 
            isAppendMode, 
            charset
            );
    }    

    /**
     * Init Logger
     * @param logFileLimitHour
     * @param logFileLimitSize
     * @param logDir
     * @param logFilePrefix
     * @param logFileSuffix
     * @param logFileDatePattern
     * @param logLevel
     * @param isAppendMode
     * @param charset
     */
    private void init(
                      int logFileLimitHour, 
                      int logFileLimitSize, 
                      Path logPath,
                      String logFileDatePattern,
                      LEVEL logLevel, 
                      boolean isAppendMode,
                      String charset
                      ) {
        this.chageLogFileInterval = (long) (logFileLimitHour * 60 * 60 * 1000);
        this.logFileLimitSize = logFileLimitSize;
        this.logDir = logPath.getParent().toString();        
        String logFileName = logPath.toFile().getName();
        this.logFilePrefix = logFileName.indexOf(".") != -1 ? logFileName.substring(0, logFileName.indexOf(".")) : logFileName;
        this.logFileSuffix = logFileName.indexOf(".") != -1 ? logFileName.substring(logFileName.indexOf(".")) : DEFAULT_LOG_SUFFIX;
        this.loggerName = logFilePrefix;
        this.logFileDatePattern = logFileDatePattern;
        this.isAppendMode = isAppendMode;
    	this.charset = charset;
    	this.ps = System.out;
    	this.startTime = System.currentTimeMillis();        
        this.logFile = new File(this.logDir + FS + this.logFilePrefix + this.logFileSuffix);
        setLevel(logLevel);
  		createLogFile(startTime);
    }       

    /**
     * create log file 
     * @param millis
     * @throws IOException 
     */
    private void createLogFile(long millis) {
        try {            
            if(this.logFile.exists()) {
                if(log != null) {
                    log.close();
                } 
                if(this.logFile.length() > this.logFileLimitSize) {
                    File oldFile = new File(getLogFileName(millis));
                    //System.out.println("file : "+newFile.toString());
                    if(!this.logFile.renameTo(oldFile)) {
                        throw new IOException("Can't change log file name. BEFORE : "+this.logFile.getAbsolutePath());
                    }
                    this.logFile = new File(this.logFile.getAbsolutePath());
                }
            } else {
                if(!this.logFile.getParentFile().exists()) {
                    this.logFile.getParentFile().mkdirs();
                }
                if(!this.logFile.createNewFile()) {
                    throw new IOException("Can't create new file : "+this.logFile.getAbsolutePath());
                }
            }
            this.isAppendMode = true;
            log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.logFile, this.isAppendMode), charset), true);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    /**
     * get log file name
     * @param millis
     * @return
     * @throws IOException
     */
    public String getLogFileName(long millis) {
    	SimpleDateFormat df = new SimpleDateFormat(this.logFileDatePattern);
    	String date = df.format(new Date());
        return this.logDir + this.logFilePrefix + "_" + date + this.logFileSuffix;
    }
    
    /**
     * to compare log duration time
     * @throws IOException 
     */
    private void compare() {
    	long currentMillis = System.currentTimeMillis();
    	long elapse = currentMillis-this.startTime;
    	if(elapse > this.chageLogFileInterval || this.logFile.length() > this.logFileLimitSize) {
    		this.startTime = currentMillis;
   			createLogFile(this.startTime);
    	} 
    }    
    
    /**
     * log for infomation
     * @param msg
     */
    public void info(String msg) {
    	info(msg, new Object[0]);
    }

    /**
     * log for information
     * @param msg
     * @param params
     */
    public void info(String msg, Object... params) {
    	synchronized(log) {
            if(FLAG.get(LEVEL.INFO)) {
                compare();
                StackTraceElement[] ste = (new Exception()).getStackTrace();
                String className = ste[ste.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ste[ste.length-1].getLineNumber();
            	String msgStr = replaceParams("[INFO]["+className+":"+lineNumber+"]"+alignString(msg), params);
                if(isSysout) {
                	stdOut(msgStr);
                }
                log.println(msgStr);
            }
    	}
    }
    
    /**
     * log for warning
     * @param msg
     */
    public void warn(String msg) {
    	info(msg, new Object[0]);
    }

    /**
     * log for warning with parameters
     * @param msg
     * @param params
     */
    public void warn(String msg, Object... params) {
    	synchronized(log) {
            if(FLAG.get(LEVEL.WARN)) {
                compare();
                StackTraceElement[] ste = (new Exception()).getStackTrace();
                String className = ste[ste.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ste[ste.length-1].getLineNumber();
            	String msgStr = replaceParams("[WARN]["+className+":"+lineNumber+"]"+alignString(msg), params);
                if(isSysout) {
                	stdOut(msgStr);
                }
                log.println(msgStr);
            }
    	}
    }

    /**
     * log for debug
     * @param msg
     */
    public void debug(String msg) {
    	debug(msg, new Object[0]);
    }

    /**
     * log for debug
     * @param msg
     * @param params
     */
    public void debug(String msg, Object... params) {
        synchronized (log) {
            if(FLAG.get(LEVEL.DEBUG)) {
                compare();
                StackTraceElement[] ste = (new Exception()).getStackTrace();
                String className = ste[ste.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ste[ste.length-1].getLineNumber();
            	String msgStr = replaceParams("[DEBUG]["+className+":"+lineNumber+"]"+alignString(msg), params);
                if(isSysout) {
                	stdOut(msgStr);
                }
                log.println(msgStr);
            }
    	}
    }    
    
    /**
     * log for error
     * @param msg
     */
    public void error(String msg) {
    	error(msg, new Object[0]);
    }
    
    /**
     * log for error
     * @param msg
     * @param isSysOut
     */
    public void error(String msg, Object... params) {
        synchronized (log) {
            if(FLAG.get(LEVEL.ERROR)) {
                compare();
                StackTraceElement[] ste = (new Exception()).getStackTrace(); 
                String className = ste[ste.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ste[ste.length-1].getLineNumber();
            	String msgStr = replaceParams("[ERROR]["+className+":"+lineNumber+"]"+alignString(msg), params);
                if(isSysout) {
                	stdOut(msgStr);
                }
                log.println(msgStr);
            }
    	}
    }    

    /**
     * log for fatal
     * @param msg
     */
    public void fatal(String msg) {
    	fatal(msg, new Object[0]);
    }
    
    /**
     * log for fatal
     * @param msg
     * @param params
     */
    public void fatal(String msg, Object... params) {
        synchronized (log) {
            if(FLAG.get(LEVEL.FATAL)) {
                compare();
                StackTraceElement[] ste = (new Exception()).getStackTrace();
                String className = ste[ste.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ste[ste.length-1].getLineNumber();
            	String msgStr = replaceParams("[FATAL]["+className+":"+lineNumber+"]"+alignString(msg), params);  
                if(isSysout) {
                	stdOut(msgStr);
                }
            log.println(msgStr);
            }
    	}
    }    

    /**
     * log for throwable
     * @param e
     * @return
     */
    public String throwable(Throwable e) {
    	return throwable(e, new Object[0]);
    }
    
    /**
     * log for throwable
     * @param e
     * @param isSysout
     * @return
     */
    public String throwable(Throwable e, Object... params) {
    	String allMsg = "";
        synchronized(log) {
        	if(FLAG.get(LEVEL.EXCEP)) {
                compare();
                if(e != null) {
                	StackTraceElement[] ste = (new Exception()).getStackTrace();
                    String className = ste[ste.length-1].getClassName();
                    className = className.substring(className.lastIndexOf(".")+1);
                    int lineNumber = ste[ste.length-1].getLineNumber();
                	String msgStr = replaceParams("[THROWABLE]["+className+":"+lineNumber+"]"+alignString(e.toString()+" : "+e.getMessage()), params);  
                  	if(isSysout) {
                  		stdOut(msgStr);
                  	}
               		log.println(msgStr);
               		allMsg += msgStr;
                    StackTraceElement[] elements = e.getStackTrace();
                    for(int i=0; i<elements.length; i++) {
                    	String msg = "\tat "+elements[i].toString();
                       	if(isSysout) {
                       		stdOut(msg);
                       	}
                    	log.println(msg);
                    	allMsg += msg;
                    }
                }
        	}
    	}
        return allMsg;
    }

    /**
     * Replace message parameters
     * @param msg
     * @param params
     * @return
     */
    public String replaceParams(String msg, Object[] params) {
        if(params == null || params.length < 1) {
            return msg;
        }
        String replacedMsg = "";
        for(int i=0; i<params.length; i++) {
            int start = msg.indexOf("{");
            int end = msg.indexOf("}");
            if(start != -1 && end != -1 && end > start) {
                String part = msg.substring(0, start);
                replacedMsg += part + params[i];
                msg = msg.substring(end);
            }
        }
        return replacedMsg;
    }
    
    /**
     * log for HEX string
     * @param bytes
     */
    public void debugHexCode(byte[] bytes) {
    	debugHexCode(bytes, true);
    }
    
	/**
	 * log for HEX string
	 * @param bytes
	 * @param isSysOut
	 */
    public void debugHexCode(byte[] bytes, boolean isSysOut) {
    	String hex = "";
        for(int i=0; i<bytes.length; i++) {
            String str = Integer.toHexString((new Byte(bytes[i])).intValue());
            hex += (str.length() > 2)?str.substring(str.length()-2)+" ":(str.length()==1)?"0"+str+" ":str+" ";
        }
        this.debug(hex, new Object[0]);
    }
    
    /**
     * log for Map
     * @param map
     */
    public void debugMap(Map<?, ?> map) {
    	this.debugMap(map, true);
    }
    
    /**
     * log for Map
     * @param map
     * @param isSysOut
     */
    public void debugMap(Map<?, ?> map, boolean isSysOut) {
    	String str = "";
    	Iterator<?> iter = map.keySet().iterator();
    	while(iter.hasNext()) {
    		Object key = iter.next();
    		str += key + "=" + map.get(key)+System.getProperty("line.separator");
    	}
    	this.debug(str, new Object[0]);
    }
    
    /**
     * get aligned string
     * @param msg
     * @return
     */
    public String alignString(String msg) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(System.currentTimeMillis());
        String year = cal.get(Calendar.YEAR)+"";
        int m = cal.get(Calendar.MONTH)+1;
        String month = (m<10)?"0"+m:m+"";
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String day = (d<10)?"0"+d:d+"";
        int h = cal.get(Calendar.HOUR_OF_DAY);
        String hour = (h<10)?"0"+h:h+"";
        int mi = cal.get(Calendar.MINUTE);
        String minute = (mi<10)?"0"+mi:mi+"";
        int s = cal.get(Calendar.SECOND);
        String second = (s<10)?"0"+s:s+"";
    	String msgStr = "["+ year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second + "] " + msg;
    	//int tab = alignCharNum - msgStr.length();
    	//for(int i=0; i<tab; i++)
    	//	msgStr += " ";
    	return msgStr;
    }

    /**
     * Enable system output 
     */
    public void enablePrintSysIO() {
        this.isSysout = true;
    }

    /**
     * Disable system output
     */
    public void disablePrintSysIO() {
        this.isSysout = false;
    }
    
    /**
     * print to std IO 
     * @param str
     */
	public void stdOut(String str) {
		ps.println(str);
	}

    /**
     * Set log level
     * @param logLevel
     */
    public void setLevel(LEVEL logLevel) {
        setLevel(logLevel, false);
    }

    /**
     * Set log level
     * @param logLevel
     * @param levelOnly
     */
    public void setLevel(LEVEL logLevel, boolean levelOnly) {
        FLAG.entrySet().stream().forEach(e -> FLAG.put(e.getKey(), false));
        if(levelOnly) {
            FLAG.put(logLevel, true);
            return;
        }        
        switch(logLevel) {
            case DEBUG:
                FLAG.put(LEVEL.DEBUG, true);
            case INFO:            
                FLAG.put(LEVEL.INFO, true);
            case WARN:
                FLAG.put(LEVEL.WARN, true);
            case ERROR:
                FLAG.put(LEVEL.ERROR, true);
            case FATAL:
                FLAG.put(LEVEL.FATAL, true);
            case EXCEP:
                FLAG.put(LEVEL.EXCEP, true);   
            break;
            default:
                throw new RuntimeException("LEVEL is not valid: "+logLevel);
        }
    }

    /**
     * Get log level activation checker
     * @param logLevel
     * @return
     */
    public boolean getLevelFlag(LEVEL logLevel) {
        return FLAG.get(logLevel);
    }	

    /**
     * Get Logger name
     * @return
     */
    public String getLoggerName() {
        return this.loggerName;
    }
}

