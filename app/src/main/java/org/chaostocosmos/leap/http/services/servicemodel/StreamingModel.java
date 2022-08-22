package org.chaostocosmos.leap.http.services.servicemodel;

/**
 * IStreaming
 * 
 * @author 9ins
 */
public interface StreamingModel {

    /**
     * Forword streaming
     * @param position
     * @param seconds
     */
    public void forword(long position, int seconds);

    /**
     * Backword streaming
     * @param position
     * @param seconds
     */
    public void backword(long position, int seconds);

    /**
     * Replay streaming
     */
    public void replay();

    /**
     * Go previous
     */
    public void previous();
    
    /**
     * Go next
     */
    public void next();
}
