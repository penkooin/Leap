package org.chaostocosmos.leap.http.context;

import java.util.EventObject;

/**
 * Context Event object
 * 
 * @author 9ins
 */
public class ContextEvent <T> extends EventObject {
    /**
     * Metadata event type enum
     */
    EVENT_TYPE eventType;
    /**
     * Metadata 
     */
    T metadata;
    /**
     * Constructor
     * @param eventObject
     * @param metaType
     * @param contextMap
     */
    public ContextEvent(Object eventSource, EVENT_TYPE eventType, T metadata) {
        super(eventSource);
        this.eventType = eventType;
        this.metadata = metadata;
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
    public T getMetadata() {
        return this.metadata;
    }
    /**
     * Get context map
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V getMetadataValue(String pathExpr) {
        return ((Metadata<V>)this.metadata).getValue(pathExpr);
    }
}
