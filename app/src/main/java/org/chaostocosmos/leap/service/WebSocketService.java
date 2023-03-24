package org.chaostocosmos.leap.service;

import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.Response;

/**
 * AbstractWebSocketService
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "/wss")
public class WebSocketService extends AbstractService { 

    @Override
    public Exception errorHandling(Response response, Exception t) {
        return t;
    }

    //private Session session;    
    
}
