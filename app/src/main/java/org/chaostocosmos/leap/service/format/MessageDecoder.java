package org.chaostocosmos.leap.service.format;

import org.chaostocosmos.leap.spring.entity.Message;

import com.google.gson.Gson;

/**
 * MessageDecoder
 * 
 * @author 9ins
 */
public class MessageDecoder {

    private static Gson gson = new Gson();

    public void destroy() {
    }

    public Message decode(String message) {
        return gson.fromJson(message, Message.class);
    }

    public boolean willDecode(String message) {
        return (message != null);
    }    
}

