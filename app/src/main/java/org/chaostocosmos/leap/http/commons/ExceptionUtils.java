package org.chaostocosmos.leap.http.commons;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ExceptionUtils
 * 
 * @author 9ins
 */
public class ExceptionUtils {
    /**
     * Get stack strace messages
     * @param throwable
     * @return
     */
    public static String getStackTraces(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        return trace.substring(trace.indexOf(System.lineSeparator()));
    }    
}
