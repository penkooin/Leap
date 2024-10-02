package org.chaostocosmos.leap.context;

import java.util.EventObject;

import org.chaostocosmos.leap.enums.SERVER_EVENT;

/**
 * Context Event object
 * 
 * @author 9ins
 */
public class MetaEvent <T extends Metadata<?>> extends EventObject {

    /**
     * Metadata event type enum
     */
    SERVER_EVENT eventType;

    /**
     * Metadata 
     */
    T metadata;

    /**
     * Metadata path expression
     */
    String expr;

    /**
     * Event value
     */
    Object value;

    /**
     * Constructor
     * @param eventSource
     * @param eventType
     * @param metadata
     * @param expr
     * @param value
     */
    public MetaEvent(Object eventSource, SERVER_EVENT eventType, T metadata, String expr, Object value) {
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
    public SERVER_EVENT getEventType() {
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
     * Get value of
     * @return
     */
    public Object getValue() {
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
    public Object getValue(String pathExpr) {
        return this.metadata.getValue(pathExpr);
    }    
}
