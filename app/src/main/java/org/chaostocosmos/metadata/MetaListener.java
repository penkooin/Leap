package org.chaostocosmos.metadata;

/**
 * Metadata listener
 * 
 * @author 9ins
 */
public interface MetaListener {

    /**
     * When metadata injected to object field. 
     * As using getEventSource, It's able to get event object.
     * @param e
     */
    public <T> void metadataInjected(MetaEvent<T> e);

    /**
     * When metadata changed 
     * @param e
     */
    public <T> void metadataChanged(MetaEvent<T> e);
    
    /**
     * When metadata removed
     * @param e
     */
    public <T> void metadataRemoved(MetaEvent<T> e);

    /**
     * When metadata created
     * @param e
     */
    public <T> void metadataCreated(MetaEvent<T> e);    
}
