package org.chaostocosmos.leap.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.HTTP;

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
    Map<String, List<String>> headers = new HashMap<>();
    
    /**
     * HTTP response code
     */
    HTTP resCode;

    /**
     * HTTP response message
     */
    String responseMessage;

    /**
     * Constructs with response code
     * @param resCode
     */
    public HTTPException(HTTP resCode) {
        this(resCode, new Exception("HTTP Exception strikes!!!"));
    }

    /**
     * Constructs with response code and error code
     * @param resCode
     * @param errorCode
     */
    public HTTPException(HTTP resCode, int errorCode) {
        this(resCode, Context.messages().<String> error(errorCode));
    }

    /**
     * Constructs with response code, error code and error parameters
     * @param resCode
     * @param errorCode
     * @param errorParams
     */
    public HTTPException(HTTP resCode, int errorCode, Object errorParams) {
        this(resCode, Context.messages().<String> error(errorCode, errorParams));
    }

    /**
     * Constructs with response code and error message
     * @param resCode
     * @param message
     */
    public HTTPException(HTTP resCode, String message) {
        this(resCode, new Exception(message));
    }

    /**
     * Constructs with response code and cause
     * @param resCode
     * @param cause
     */
    public HTTPException(HTTP resCode, Throwable cause) {
        this(resCode, Context.messages().<String>http(resCode.code()), cause);
    }

    /**
     * Constructs with response code, cause and headers
     * @param resCode
     * @param message
     * @param headers
     */
    public HTTPException(HTTP resCode, String message, Map<String, List<String>> headers) {
        this(resCode, Context.messages().get(type, resCode.code()), new Exception(message), headers.entrySet().stream().map(e -> List.of(e.getKey(), e.getValue())).flatMap(l -> l.stream()).toArray());
    }

    /**
     * Constructs with response code, cause and headers
     * @param resCode
     * @param cause
     * @param headers
     */
    public HTTPException(HTTP resCode, Throwable cause, Map<String, List<String>> headers) {
        this(resCode, Context.messages().get(type, resCode.code()), cause, headers.entrySet().stream().map(e -> List.of(e.getKey(), e.getValue())).flatMap(l -> l.stream()).toArray());
    }

    /**
     * Constructs with response code, cause and headerkeyValue
     * @param resCode
     * @param message
     * @param headerkeyValue
     */
    public HTTPException(HTTP resCode, String message, Object ... headerkeyValue) {
        this(resCode, Context.messages().get(type, resCode.code()), new Exception(message), headerkeyValue);
    }

    /**
     * Constructs with response code, cause and headerkeyValue
     * @param resCode
     * @param cause
     * @param headerkeyValue
     */
    public HTTPException(HTTP resCode, Throwable cause, Object ... headerkeyValue) {
        this(resCode, Context.messages().get(type, resCode.code()), cause, headerkeyValue);
    }

    /**
     * Constructs with response code, response, cause and headerKeyValue
     * @param resCode
     * @param responseMessage
     * @param cause
     * @param headerKeyValue
     */
    public HTTPException(HTTP resCode, String responseMessage, Throwable cause, Object ... headerkeyValue) {
        super(cause);
        this.resCode = resCode;
        this.responseMessage = responseMessage;
        if(headerkeyValue != null && headerkeyValue.length % 2 != 0) {
            throw new RuntimeException("Header key / value must be even. "+Arrays.toString(headerkeyValue));
        }
        if(resCode == HTTP.RES401) {
            addHeader("WWW-Authenticate", "Basic");
        }
        for(int i=0; i < headerkeyValue.length / 2; i++) {
            addHeader((String) headerkeyValue[i*2], (String) headerkeyValue[i*2+1]);
        }
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
    public HTTP getResCode() {
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
        return this.responseMessage;
    }

    /**
     * Get stack trace message
     * @return
     */
    public String getStackTraceMessage() {
        return getStackTraceMessage(new Exception(getMessage()));
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
            ", resMessage='" + responseMessage + "'" +
            "}";
    }
}
