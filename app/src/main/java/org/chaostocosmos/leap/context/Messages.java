package org.chaostocosmos.leap.context;

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
     * Get http message by specified code
     * @param code
     * @return
     */
    public <V> V http(int code) {
        return super.getValue("messages.http.http"+code);
    }
}
