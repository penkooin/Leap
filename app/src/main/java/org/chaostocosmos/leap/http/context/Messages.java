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
    public <V> V get(MSG_TYPE type, int code, Object ... args) {
        return get(type, type.name().toLowerCase()+String.format("%03d", code), args);
    }

    /**
     * Get message from messages.yml
     * @param <V>
     * @param type
     * @param code
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V get(MSG_TYPE type, String code, Object ... args) {
        V msg = super.getValue("messages."+type.name().toLowerCase()+"."+code);
        if(args.length == 0) {
            return (V) msg.toString().replace("{}", "");
        }
        return (V) Arrays.stream(args).filter(a -> a != null).reduce(msg, (ap, a) -> ap.toString().replaceFirst("\\{\\}", a.toString())).toString();
    }

   /**
     * Set value to messages
     * @param expr
     * @param value
     */
    public <V> void set(String expr, V value) {
        super.setValue(expr, value);
    }    

    /**
     * Get http message by specified code
     * @param code
     * @return
     */
    public <V> V http(int code) {
        return get(MSG_TYPE.HTTP, code);
    }

    /**
     * Get HTTP message
     * @param code
     * @param args
     * @return
     */
    public <V> V http(int code, Object... args) {
        return get(MSG_TYPE.HTTP, code, args);
    }

    /**
     * Get HTTP message
     * @param <V>
     * @param code
     * @return
     */
    public <V> V http(String code) {
        return get(MSG_TYPE.HTTP, code);
    }

    /**
     * Get HTTP message
     * @param <V>
     * @param code
     * @param args
     * @return
     */
    public <V> V http(String code, Object ... args) {
        return get(MSG_TYPE.HTTP, code);
    }

    /**
     * Get debug message
     * @param code
     * @param args
     * @return
     */
    public <V> V debug(int code) {
        return get(MSG_TYPE.DEBUG, code);
    } 

    /**
     * Get debug message
     * @param code
     * @param args
     * @return
     */
    public <V> V debug(int code, Object... args) {
        return get(MSG_TYPE.DEBUG, code, args);
    } 

    /**
     * Get debug message
     * @param <V>
     * @param code
     * @return
     */
    public <V> V debug(String code) {
        return get(MSG_TYPE.DEBUG, code);
    }

    /**
     * Get debug message
     * @param <V>
     * @param code
     * @param args
     * @return
     */
    public <V> V debug(String code, Object... args) {
        return get(MSG_TYPE.DEBUG, code);
    }

    /**
     * Get info message
     * @param code
     * @param args
     * @return
     */
    public <V> V info(int code, Object ... args) {
        return get(MSG_TYPE.INFO, code, args);
    }

    /**
     * Get warn message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V warn(int code, Object ... args) {
        return get(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get warn message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V warn(String code, Object ... args) {
        return get(MSG_TYPE.WARN, code, args);
    }

    /**
     * Get error message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V error(int code, Object ... args) {
        return get(MSG_TYPE.ERROR, code, args);
    }

    /**
     * Get error message from messages.yml
     * @param code
     * @param args
     * @return
     */
    public <V> V error(String code, Object ... args) {
        return get(MSG_TYPE.ERROR, code, args);
    }
}
