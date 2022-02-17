package org.chaostocosmos.leap.http.services;

import java.util.Map;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.part.BodyPart;

@ServiceMapper(path="/deploy/service")
public class LeapServiceDeploy extends AbstractLeapService {
    
    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path = "/")
    public void deployService(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        Map<String, String> headers = request.getReqHeader();
        BodyPart bodyPart = request.getBodyPart();
    }

    @Override
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable throwable) {
        return throwable;
    }
}
