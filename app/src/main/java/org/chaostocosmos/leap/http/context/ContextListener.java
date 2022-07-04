package org.chaostocosmos.leap.http.context;

import java.util.EventListener;

/**
 * ContextListener
 * 
 * @author 9ins
 */
public interface ContextListener<T> extends EventListener {
    /**
     * receive Server context
     */
    public void contextServer(ContextEvent<Server<?>> ce) throws Exception;
    /**
     * recieve Hosts context
     * @param ce
     * @throws Exception
     */
    public void contextHosts(ContextEvent<Hosts<?>> ce) throws Exception;
    /**
     * recieve messages context
     * @param ce
     * @throws Exception
     */
    public void contextMessages(ContextEvent<Messages<?>> ce) throws Exception;
    /**
     * receive mime context
     * @param ce
     * @throws Exception
     */
    public void contextMime(ContextEvent<Mime<?>> ce) throws Exception;
    /**
     * receive chart context 
     * @param ce
     * @throws Exception
     */
    public void contextChart(ContextEvent<Chart<?>> ce) throws Exception;
}

