package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.services.session.Session;

/**
 * AbstractWebSocketService
 * 
 * @author 9ins
 */
@ServiceMapper(path = "/wss")
public class WebSocketService extends AbstractService {

    @Override
    public Throwable errorHandling(Response response, Throwable t) throws Throwable {
        return t;
    }

    private Session session;


    

    
    
}
