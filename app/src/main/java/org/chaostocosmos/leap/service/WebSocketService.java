package org.chaostocosmos.leap.service;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.http.HttpTransfer;
import org.chaostocosmos.leap.service.abstraction.AbstractService;

/**
 * AbstractWebSocketService
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "/wss")
public class WebSocketService extends AbstractService { 

    @MethodMapper(method=REQUEST.GET, mappingPath = "/websocket")
    public Object handleWebSocket(String str, HttpResponse response, HttpRequest request, Integer num, HttpTransfer transfer) {
        super.getHost().getLogger().debug(str);
        super.getHost().getLogger().debug(response.toString());
        super.getHost().getLogger().debug(request.toString());
        super.getHost().getLogger().debug(num+"");
        super.getHost().getLogger().debug(transfer.toString());
        return "This is web socket context";
    }

    @Override
    public Exception errorHandling(HttpResponse response, Exception t) {
        return t;
    }    
}
