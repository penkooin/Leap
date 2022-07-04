package org.chaostocosmos.leap.http.context;

/**
 * ContextAdapter
 * 
 * @author 9ins
 */
public abstract class ContextAdapter<T> implements ContextListener<T> {

    @Override
    public void contextServer(ContextEvent<Server<?>> ce) throws Exception {
    }

    @Override
    public void contextHosts(ContextEvent<Hosts<?>> ce) throws Exception {
    }

    @Override
    public void contextMessages(ContextEvent<Messages<?>> ce) throws Exception {
    }

    @Override
    public void contextMime(ContextEvent<Mime<?>> ce) throws Exception {        
    }

    @Override
    public void contextChart(ContextEvent<Chart<?>> ce) throws Exception {
    }
}
