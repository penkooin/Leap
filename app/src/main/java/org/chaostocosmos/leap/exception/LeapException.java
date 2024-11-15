package org.chaostocosmos.leap.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.chaostocosmos.leap.context.Context;
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
    HTTP resObj;    

    /**
     * Constructs with response code
     * @param resObj
     */
    public LeapException(HTTP resObj) {
        this(resObj, new Object[0]);
    }

    /**
     * Constructs with response code and parameters
     * @param resObj
     * @param params
     */
    public LeapException(HTTP resObj, Object... params) {
        super(Context.get().message().message(resObj.code(), params));
        this.resObj = resObj;
    }

    /**
     * Constructs with response code and message
     * @param resObj
     * @param message
     */
    public LeapException(HTTP resObj, String message) {
        super(resObj.toString()+" "+message);
        this.resObj = resObj;
    } 

    /**
     * Constructs with respose object, cause
     * @param resObj
     * @param cause
     */
    public LeapException(HTTP resObj, Throwable cause) {
        this(resObj, cause, new Object[0]);
    }

    /**
     * Constructs with response object, cause and message params
     * @param resObj
     * @param cause
     * @param params
     */
    public LeapException(HTTP resObj, Throwable cause, Object... params) {
        super(Context.get().message().message(resObj.code(), params), cause);
        this.resObj = resObj;
    }

    /**
     * Get RES_CODE
     * @return
     */
    public HTTP getHTTP() {
        return this.resObj;
    }

    /**
     * Get Http Status text
     * @return
     */
    public String getStatus() {
        return this.resObj.status();
    }

    /**
     * Get code
     * @return
     */
    public int code() {
        return this.resObj.code();
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
