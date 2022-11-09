package org.chaostocosmos.leap.http.service;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.enums.MIME;
import org.chaostocosmos.leap.http.enums.REQUEST;
import org.chaostocosmos.leap.http.inject.MethodIndicates;
import org.chaostocosmos.leap.http.inject.ServiceIndicates;

/**
 * SimpleStreamingService
 * 
 * @author 9ins
 */
@ServiceIndicates(path = "")
public class SimpleStreamingService extends AbstractStreamingService {

    public SimpleStreamingService() {
        super(MIME.VIDEO_MP4);
    } 

    @MethodIndicates(method = REQUEST.GET, path = "/video")
    public void streaming(Request request, Response response) {
        
    }

    @Override
    public Exception errorHandling(Response response, Exception e) {
        e.printStackTrace();
        return null;
    }    
}
