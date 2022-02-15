package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.part.BodyPart;

@ServiceMapper(path="/deploy/service")
public class LeapServiceDeploy extends AbstractLeapService {
    
    @MethodMappper(mappingMethod = REQUEST_TYPE.DEPLOY, path = "/")
    public void deployService(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        BodyPart bodyPart = request.getBodyPart();
    }
}
