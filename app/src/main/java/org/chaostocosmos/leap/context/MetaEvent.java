package org.chaostocosmos.leap.context;

import java.util.EventObject;

/**
 * Context Event object
 * 
 * @author 9ins
 */
public class MetaEvent <T extends Metadata<?>> extends EventObject {

    /**
     * Metadata event type enum
     */
    META_EVENT_TYPE eventType;

    /**
     * Metadata 
     */
    T metadata;

    /**
     * Metadata path expression
     */
    String expr;

    /**
     * Original value
     */
    Object original;

    /**
     * Changed value
     */
    Object changed;

    /**
     * Constructor
     * @param eventSource
     * @param eventType
     * @param metadata
     * @param expr
     * @param original
     * @param changed
     */
    public MetaEvent(Object eventSource, META_EVENT_TYPE eventType, T metadata, String expr, Object original, Object changed) {
        super(eventSource);
        this.eventType = eventType;
        this.metadata = metadata;
        this.expr = expr;
        this.original = original;
        this.changed = changed;
    }

    /**
     * Get event type
     * @return
     */
    public META_EVENT_TYPE getEventType() {
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
     * Get original value
     * @return
     */
    public Object getOriginal() {
        return this.original;
    }

    /**
     * Get changed value
     * @return
     */
    public Object getChanged() {
        return this.changed;
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

    @Override
    public String toString() {
        return "MetaEvent [eventType=" + eventType + ", metadata=" + metadata + ", expr=" + expr + ", original="
                + original + ", changed=" + changed + "]";
    }        
}
