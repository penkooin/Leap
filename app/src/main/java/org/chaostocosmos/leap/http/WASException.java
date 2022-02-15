package org.chaostocosmos.leap.http;

import org.chaostocosmos.leap.http.enums.MSG_TYPE;

/**
 * WAS exception object
 * 
 * @author 9ins
 */
public class WASException extends Exception {
    /**
     * Message type on config.yml
     */
    MSG_TYPE type;
    
    /**
     * Exception code
     */
    int code;

    /**
     * message
     */
    String message;

    /**
     * Constructor with type, code 
     * @type
     * @code
     */
    public WASException(MSG_TYPE type, int code) {
        this(type, code, null, new Object[0]);
    }

    /**
     * Constructor with type, code, throwable
     * @param type
     * @param code
     * @param cause
     */
    public WASException(MSG_TYPE type, int code, Throwable cause) {
        this(type, code, cause, cause.toString());
    }

    /**
     * Constructor with type, code, params
     * @param type
     * @param code
     * @param args
     */
    public WASException(MSG_TYPE type, int code, Object... args) {
        this(type, code, null, args);
    }

    /**
     * Constructor with code, arguments
     * @param type
     * @param httpCode
     * @param code
     * @param cause
     * @param args
     */
    public WASException(MSG_TYPE type, int code, Throwable cause, Object... args) { 
        super(cause);
        if(cause != null) {
            super.setStackTrace(cause.getStackTrace());
        }
        this.type = type;
        this.code = code;
        this.message = Context.getMsg(type, code, args);
    }

    /**
     * Constructor with caused exception
     * @param cause
     */
    public WASException(Throwable cause) {
        super(cause);
    }

    /**
     * Get MSG_TYPE
     * @return
     */
    public MSG_TYPE getMessageType() {
        return this.type;
    }

    /**
     * Get code
     * @return
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Get message
     */
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
