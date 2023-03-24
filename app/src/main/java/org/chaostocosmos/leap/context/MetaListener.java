package org.chaostocosmos.leap.context;

import java.util.EventListener;

/**
 * ContextListener
 * 
 * @author 9ins
 */
public interface MetaListener<T> extends EventListener {
    /**
     * receive Server context
     */
    public <V> void receiveContextEvent(MetaEvent<T, V> ce) throws Exception;
}

