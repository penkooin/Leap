package org.chaostocosmos.leap.service;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.service.abstraction.AbstractStreamingService;

/**
 * SimpleStreamingService
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "")
public class SimpleStreamingService extends AbstractStreamingService {

    public SimpleStreamingService() {
        super(MIME.VIDEO_MP4);
    } 

    @MethodMapper(method = REQUEST.GET, mappingPath = "/video")
    public void streaming(HttpRequest request, HttpResponse response) throws Exception {
        super.streaming(request, response);        
    }

    @Override
    public Exception errorHandling(HttpResponse response, Exception e) {
        e.printStackTrace();
        return null;
    }    
}
