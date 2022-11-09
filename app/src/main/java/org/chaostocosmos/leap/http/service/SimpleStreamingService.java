package org.chaostocosmos.leap.http.service;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.MethodMapper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.MIME;
import org.chaostocosmos.leap.http.enums.REQUEST;

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
    public void streaming(Request request, Response response) {
        
    }

    @Override
    public Exception errorHandling(Response response, Exception e) {
        e.printStackTrace();
        return null;
    }    
}
