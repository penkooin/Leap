package org.chaostocosmos.leap.spring.entity;

/**
 * Message
 * 
 * @author 9ins
 */
public class Message {

    /**
     * From whom
     */
    private String from;

    /**
     * To whom
     */
    private String to;

    /**
     * Message contents
     */
    private String content;

    /**
     * Get from whom
     * @return
     */
    public String getFrom() {
        return this.from;
    }

    /**
     * Set from whom
     * @param from
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Get to whom
     * @return
     */
    public String getTo() {
        return this.to;
    }

    /**
     * Set to whom
     * @param to
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Get message contents
     * @return
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set message contents
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }    
}
