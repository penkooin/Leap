package org.chaostocosmos.leap;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.chaostocosmos.leap.common.ExceptionUtils;
import org.chaostocosmos.leap.enums.HTTP;

/**
 * WAS exception object
 * 
 * @author 9ins
 */
public class LeapException extends RuntimeException {
    /**
     * HTTP response code
     */
    HTTP resCode;
    /**
     * Constructs with response code
     * @param resCode
     */
    public LeapException(HTTP resCode) {
        this(resCode, ExceptionUtils.getStackTraces(new Exception()));
    }
    /**
     * Constructs with response code and message
     * @param resCode
     * @param message
     */
    public LeapException(HTTP resCode, String message) {
        this(resCode, new Throwable(message));
    } 
    /**
     * Constructs with response code and cause
     * @param resCode
     * @param cause
     */
    public LeapException(HTTP resCode, Throwable cause) {
        this(resCode, resCode.status(), cause);
    }
    /**
     * Constructs with response code, response, cause and headerKeyValue
     * @param resCode
     * @param message
     * @param cause
     */
    public LeapException(HTTP resCode, String message, Throwable cause) {
        super(message, cause);        
        this.resCode = resCode;
    }
    /**
     * Get RES_CODE
     * @return
     */
    public HTTP getResCode() {
        return this.resCode;
    }
    /**
     * Get Http Status text
     * @return
     */
    public String getStatus() {
        return this.resCode.status();
    }
    /**
     * Get code
     * @return
     */
    public int code() {
        return this.resCode.code();
    }
    /**
     * Get stack trace message
     * @return
     */
    public String getStackTraceMessage() {
        return getStackTraceMessage(this);
    }
    /**
     * Get stack trace message
     * @param t
     * @return
     */
    public static String getStackTraceMessage(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
