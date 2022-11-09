package org.chaostocosmos.leap.http.service;

import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.inject.ServiceIndicates;

/**
 * AbstractWebSocketService
 * 
 * @author 9ins
 */
@ServiceIndicates(path = "/wss")
public class WebSocketService extends AbstractService { 

    @Override
    public Exception errorHandling(Response response, Exception t) {
        return t;
    }

    //private Session session;    
    
}
