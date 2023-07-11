package org.chaostocosmos.leap.spring.entity;

/**
 * Message
 * 
 * @author 9ins
 */
public class Message {

    private String from;
    private String to;
    private String content;

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }    
}
