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
    @SuppressWarnings("unchecked")
    public <V> V getMsg(MSG_TYPE type, int code, Object ... args) {
        V msg = super.getValue("messages."+type.name().toLowerCase()+"."+type.name().toLowerCase()+code);
        return (V) Arrays.stream(args).filter(a -> a != null).reduce(msg, (ap, a) -> ap.toString().replaceFirst("\\{\\}", a.toString())).toString();
    }    

   /**
     * Set value to messages
     * @param expr
     * @param value
     */
    public <V> void setMsg(String expr, V value) {
        super.setValue(expr, value);
    }    

    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public <V> V getHttpMsg(int code) {
        return getMsg(MSG_TYPE.HTTP, code);
    }

    /**
     * Get HTTP message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V getHttpMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.HTTP, code, args);
    }

    /**
     * Get debug message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V getDebugMsg(int code, Object... args) {
        return getMsg(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get info message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V getInfoMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V getWarnMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V getErrorMsg(int code, Object ... args) {
        return getMsg(MSG_TYPE.ERROR, code, args);
    }
}
