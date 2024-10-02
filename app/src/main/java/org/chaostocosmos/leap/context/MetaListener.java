package org.chaostocosmos.leap.context;

import java.util.EventListener;

/**
 * ContextListener
 * 
 * @author 9ins
 */
public interface MetaListener extends EventListener {
    
    /**
     * receive Server context
     */
    public void receiveContextEvent(MetaEvent<Metadata<?>> ce) throws Exception;
}

