package org.chaostocosmos.leap.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * WAS exception object
 * 
 * @author 9ins
 */
public class HTTPException extends RuntimeException {
    /**
     * Message type
     */
    public static final MSG_TYPE type = MSG_TYPE.HTTP;

    /**
     * Header Map
     */
    Map<String, List<String>> headers;
    
    /**
     * HTTP response code
     */
    RES_CODE resCode;

    /**
     * HTTP response message
     */
    String response;

    /**
     * Error stack trace message
     */
    String stackTraceMessage;

    /**
     * Constructs with response code
     * @param resCode
     */
    public HTTPException(RES_CODE resCode) {
        this(resCode, new Exception());
    }

    /**
     * Constructor with type, code, throwable
     * @param resCode
     * @param cause
     */
    public HTTPException(RES_CODE resCode, Throwable cause) {
        this(resCode, new HashMap<String, List<String>>(), getStackTraceMessage(cause));
    }

    /**
     * Constructs with response code and error message
     * @param resCode
     * @param cause
     */
    public HTTPException(RES_CODE resCode, String cause) {
        this(resCode, new HashMap<String, List<String>>(), Context.getMessages().<String>getHttpMsg(resCode.code()), cause);
    }

    /**
     * Constructs with response code and headers
     * @param resCode
     * @param headers
     */
    public HTTPException(RES_CODE resCode, Map<String, List<String>> headers) {
        this(resCode, headers, Context.getMessages().getMsg(type, resCode.code()));
    }

    /**
     * Constructs with response code, response headers, caused error
     * @param resCode
     * @param headers
     * @param cause
     */
    public HTTPException(RES_CODE resCode, Map<String, List<String>> headers, String cause) {
        this(resCode, headers, Context.getMessages().getMsg(type, resCode.code()), cause);
    }

    /**
     * Constructs with response code, response headers, response message, caused error
     * @param resCode
     * @param headers
     * @param response
     * @param cause
     */
    public HTTPException(RES_CODE resCode, Map<String, List<String>> headers, String response, String cause) {
        super(cause);
        this.resCode = resCode;
        this.headers = headers;
        this.response = response;
        this.stackTraceMessage = cause;
    }

    /**
     * Get MSG_TYPE
     * @return
     */
    public MSG_TYPE getMessageType() {
        return type;
    }

    /**
     * Get RES_CODE
     * @return
     */
    public RES_CODE getResCode() {
        return this.resCode;
    }

    /**
     * Get code
     * @return
     */
    public int code() {
        return this.resCode.code();
    }

    /**
     * Get header Map of HTTPException
     * @return
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Add response header attribute
     * @param attr
     * @param value
     */
    public void addHeader(String attr, String value) {
        List<String> values = null;
        if(this.headers.containsKey(attr)) {
            values = this.headers.get(attr);
            if(values == null) {
                values = new ArrayList<String>();
            }
        } else {
            values = new ArrayList<String>();
            this.headers.put(attr, values);
        }
        values.add(value);
    }

    /**
     * Get response message
     */
    public String getResponse() {
        return this.response;
    }

    /**
     * Get stack trace message
     * @return
     */
    public String getStackTraceMessage() {
        return this.stackTraceMessage;
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

    @Override
    public String toString() {
        return "{" +
            " headers='" + headers + "'" +
            ", resCode='" + resCode + "'" +
            ", resMessage='" + response + "'" +
            ", stackTraceMessage='" + stackTraceMessage + "'" +
            "}";
    }

}
