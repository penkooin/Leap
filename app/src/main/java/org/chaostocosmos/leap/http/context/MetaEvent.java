package org.chaostocosmos.leap.http.context;

import java.util.EventObject;

/**
 * Context Event object
 * 
 * @author 9ins
 */
public class MetaEvent <T, V> extends EventObject {
    /**
     * Metadata event type enum
     */
    EVENT_TYPE eventType;

    /**
     * Metadata 
     */
    Metadata<T> metadata;

    /**
     * Metadata path expression
     */
    String expr;

    /**
     * Event value
     */
    V value;
    
    /**
     * Constructor
     * @param eventObject
     * @param metaType
     * @param contextMap
     */
    public MetaEvent(Object eventSource, EVENT_TYPE eventType, Metadata<T> metadata, String expr, V value) {
        super(eventSource);
        this.eventType = eventType;
        this.metadata = metadata;
        this.expr = expr;
        this.value = value;
    }
    /**
     * Get event type
     * @return
     */
    public EVENT_TYPE getEventType() {
        return this.eventType;
    }
    /**
     * Get meta type
     * @return
     */
    public Metadata<T> getMetadata() {
        return this.metadata;
    }
    /**
     * Get value of
     * @return
     */
    public V getValue() {
        return this.value;
    }
    /**
     * Get path expression
     * @return
     */
    public String getPathExpression() {
        return this.expr;
    }
    /**
     * Get context map
     * @return
     */
    public V getValue(String pathExpr) {
        return this.metadata.getValue(pathExpr);
    }    
}
