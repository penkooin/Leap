package org.chaostocosmos.leap.http.context;

import java.util.Arrays;

import org.chaostocosmos.leap.http.enums.MSG_TYPE;

/**
 * Messages 
 * 
 * Message configuration object 
 * 
 * @author 9ins
 */
public class Messages <T> extends Metadata <T> {

    /**
     * Constructor
     * @param messageMap
     */
    public Messages(T messagesMap) {
        super(messagesMap);
    }

    /**
     * Get message from messages.yml
     * @param type
     * @param code
     * @param args
     * @return
     */
    public String getMsg(MSG_TYPE type, int code, Object ... args) {
        String msg = super.getValue("messages."+type.name().toLowerCase()+"."+type.name().toLowerCase()+code);
        return Arrays.stream(args).filter(a -> a != null).reduce(msg, (ap, a) -> ap.toString().replaceFirst("\\{\\}", a.toString())).toString();
    }    

   /**
     * Set value to messages
     * @param expr
     * @param value
     */
    public void setMsg(String expr, String value) {
        super.setValue(expr, value);
    }    

    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public String getHttpMsg(int code) {
        return getMsg(MSG_TYPE.HTTP, code);
    }

    /**
     * Get HTTP message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public String getHttpMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.HTTP, code, args);
    }

    /**
     * Get debug message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public String getDebugMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get info message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public String getInfoMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public String getWarnMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public String getErrorMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.ERROR, code, args);
    }
}
