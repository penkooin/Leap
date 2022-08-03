package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;

/**
 * SimpleStreamingService
 * 
 * @author 9ins
 */
@ServiceMapper(path = "")
public class SimpleStreamingService extends AbstractStreamingService {

    public SimpleStreamingService() {
        super(MIME_TYPE.VIDEO_MP4);
    }

    @MethodMappper(mappingMethod = REQUEST_TYPE.GET, path = "/video")
    public void streaming(Request request, Response response) {
        
    }

    @Override
    public Throwable errorHandling(Response response, Throwable t) throws Throwable {
        t.printStackTrace();
        return null;
    }    
}
