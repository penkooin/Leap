package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;

@ServiceMapper(path="/deploy/service")
public class LeapServiceDeploy extends AbstractLeapService {
    
    public void deployService(HttpRequestDescriptor request, HttpResponseDescriptor response) {
        
    }    
}
