package org.chaostocosmos.leap.http.context;

import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.DataStructureOpr;

/**
 * AbstractMeta
 * 
 * Meta data top level abstract class - Including meta data operation functionalities.
 * 
 * @author 9ins
 */
public abstract class Metadata <T> {
    /**
     * Metadata
     */
    T meta;

    /**
     * Const
     * @param meta
     */
    public Metadata(T meta) {
        this.meta = meta;
    }

    /**
     * Get value by expression
     */
    public <V> V getValue(String expr) {
        V value = DataStructureOpr.<V> getValue(meta, expr);
        if(value != null) {
            return value;
        } else {
            throw new IllegalArgumentException("There isn't exist value on specified expresstion: "+expr);
        }
    }

    /**
     * Set value on specified position
     */
    public <V> void setValue(String expr, V value) {
        DataStructureOpr.<V> setValue(meta, expr, value);
        Context.dispatchContextEvent(EVENT_TYPE.CHANGED);
    }

    @SuppressWarnings("unchecked")
    public <V> void addValue(String expr, V value) {
        Object parent = DataStructureOpr.<V> getValue(meta, expr.substring(0, expr.lastIndexOf(".")));
        if(parent == null) {
            throw new IllegalArgumentException("Specified expression's parent is not exist: "+expr);
        } else if(parent instanceof List) {
            ((List<Object>) parent).add(value);
        } else if(parent instanceof Map) {
            ((Map<String, Object>) parent).put(expr.substring(expr.lastIndexOf(".")+1), value);
        } else {
            throw new RuntimeException("Parent data type is wired. Context data structure failed: "+parent);
        }
        Context.dispatchContextEvent(EVENT_TYPE.ADDED);
    }

    @SuppressWarnings("unchecked")
    public <V> void removeValue(String expr) {
        Object parent = DataStructureOpr.<V> getValue(meta, expr.substring(0, expr.lastIndexOf(".")));
        if(parent == null) {
            throw new IllegalArgumentException("Specified expression's parent is not exist: "+expr);
        } else if(parent instanceof List) {
            int idx = Integer.valueOf(expr.substring(expr.lastIndexOf(".")+1));
            ((List<Object>) parent).remove(idx);
        } else if(parent instanceof Map) {
            ((Map<String, Object>) parent).remove(expr.substring(expr.lastIndexOf(".")+1));
        } else {
            throw new RuntimeException("Parent data type is wired. Context data structure failed: "+parent);
        }
        Context.dispatchContextEvent(EVENT_TYPE.REMOVED);
    }

    /**
     * Exists context value by specified expression
     */
    public boolean exists(String expr) {
        Object value = DataStructureOpr.getValue(meta, expr);
        if(value != null) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Get metadata
     */
    public T getMeta() {
        return this.meta;
    }

    @Override
    public String toString() {
        return meta.toString();
    }    
}
