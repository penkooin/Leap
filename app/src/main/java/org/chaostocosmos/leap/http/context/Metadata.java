package org.chaostocosmos.leap.http.context;

import org.chaostocosmos.leap.http.commons.DataStructureOpr;

/**
 * AbstractMeta
 * 
 * Meta data top level abstract class - Including meta data operation functionalities.
 * 
 * @author 9ins
 */
public abstract class Metadata <T> {

    T meta;

    /**
     * Const
     * @param meta
     */
    public Metadata(T meta) {
        this.meta = meta;
    }

    public <V> V getValue(String expr) {
        return DataStructureOpr.<V> getValue(meta, expr);
    }

    public <V> V getValue(Object ... expr) {
        return DataStructureOpr.<V> findValue(meta, expr);
    }
    
    public <V> void setValue(String expr, V value) {
        DataStructureOpr.<V> setValue(meta, expr, value);
    }

    public T getMeta() {
        return this.meta;
    }

    @Override
    public String toString() {
        return meta.toString();
    }    
}
