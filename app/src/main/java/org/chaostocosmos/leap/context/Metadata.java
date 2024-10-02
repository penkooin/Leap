package org.chaostocosmos.leap.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.common.data.DataStructureOpr;
import org.chaostocosmos.leap.enums.SERVER_EVENT;

/**
 * AbstractMeta
 * 
 * Meta data top level abstract class - Including meta data operation functionalities.
 * 
 * @author 9ins
 */
public class Metadata <T> {

    /**
     * List of metadata event classes
     */
    private List<MetaListener> metaListeners;

    /**
     * Metadata
     */
    T meta;

    /**
     * Constructs with metadata
     * @param meta
     */
    public Metadata(T meta) {
        this(meta, new ArrayList<>());
    }

    /**
     * Constructs with metadata, event classes
     * @param meta
     * @param metaListeners
     */
    public Metadata(T meta, List<MetaListener> metaListeners) {
        this.meta = meta;
        this.metaListeners = metaListeners;
    }

    /**
     * Add target event class to be received from
     * @param metaListeners
     */
    public void addEventClass(MetaListener metaListeners) {
        if(!this.metaListeners.contains(metaListeners)) {
            this.metaListeners.add(metaListeners);
        } else {
            throw new IllegalStateException("Specified event receiving class already exists in List: "+metaListeners.toString());
        }
    }

    /**
     * Remove target event receiving from
     * @param metaListeners
     */
    public void removeEventClass(MetaListener metaListeners) {
        this.metaListeners.remove(metaListeners);
    } 
    
    /**
     * Get value by expression
     * @param expr
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
     * @param expr
     * @param value
     */
    public void setValue(String expr, Object value) {
        DataStructureOpr.<Object> setValue(meta, expr, value);
        //System.out.println(this.getClass().getCanonicalName()+"="+Chart.class.getCanonicalName());
        Context.get().dispatchContextEvent(new MetaEvent<Metadata<?>>(this, SERVER_EVENT.CHANGED, this, expr, value));
    }

    /**
     * Add metadata value
     * @param expr
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void addValue(String expr, Object value) {
        Object parent = DataStructureOpr.<Object> getValue(meta, expr.substring(0, expr.lastIndexOf(".")));
        if(parent == null) {
            throw new IllegalArgumentException("Specified expression's parent is not exist: "+expr);
        } else if(parent instanceof List) {
            ((List<Object>) parent).add(value);
        } else if(parent instanceof Map) {
            ((Map<String, Object>) parent).put(expr.substring(expr.lastIndexOf(".")+1), value);
        } else {
            throw new RuntimeException("Parent data type is wired. Context data structure failed: "+parent);
        }
        Context.get().dispatchContextEvent(new MetaEvent<Metadata<?>>(this, SERVER_EVENT.ADDED, this, expr, value));
    }

    /**
     * Remove metadata value
     * @param expr
     */
    @SuppressWarnings("unchecked")
    public void removeValue(String expr) {
        Object parent = DataStructureOpr.<Object> getValue(meta, expr.substring(0, expr.lastIndexOf(".")));
        Object value = null;
        if(parent == null) {
            throw new IllegalArgumentException("Specified expression's parent is not exist: "+expr);
        } else if(parent instanceof List) {
            int idx = Integer.valueOf(expr.substring(expr.lastIndexOf(".") + 1));
            value = ((List<Object>) parent).remove(idx);
        } else if(parent instanceof Map) {
            value = ((Map<String, Object>) parent).remove(expr.substring(expr.lastIndexOf(".")+1));
        } else {
            throw new RuntimeException("Parent data type is wired. Context data structure failed: "+parent);
        }
        Context.get().dispatchContextEvent(new MetaEvent<Metadata<?>>(this, SERVER_EVENT.REMOVED, this, expr, value));
    }

    /**
     * Exists context value by specified expression
     * @param expr
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
