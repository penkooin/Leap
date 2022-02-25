package org.chaostocosmos.leap.http.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.ClassUtils;
import org.chaostocosmos.leap.http.commons.DynamicURLClassLoader;
import org.chaostocosmos.leap.http.commons.HostsManager;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.BasicAuthFilter;
import org.chaostocosmos.leap.http.part.BodyPart;
import org.chaostocosmos.leap.http.part.MultiPart;

@ServiceMapper(path="/deploy")
public class LeapServiceDeploy extends AbstractLeapService {
    
    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path = "/upload")
    @FilterMapper(preFilters = BasicAuthFilter.class)
    public void deployService(HttpRequestDescriptor request, HttpResponseDescriptor response) throws WASException, IOException {
        Map<String, String> headers = request.getReqHeader();
        BodyPart bodyPart = request.getBodyPart();        
        if(bodyPart.getContentType() == MIME_TYPE.MULTIPART_FORM_DATA) {
            MultiPart multipart = (MultiPart) bodyPart;
            String packages = headers.get("package");
            String classname = headers.get("classname");
            
            super.logger.debug("Deploying service... "+request.getReqHeader().toString());
            Path serviceClassesPath = HostsManager.get().getDynamicClaspaths(request.getRequestedHost());
            DynamicURLClassLoader classLoader = ClassUtils.getClassLoader();
            classLoader.addPath(serviceClassesPath);

            Path packagePath = Paths.get(packages.replace(".", File.separator));
            multipart.save(serviceClassesPath.resolve(packagePath));

            ILeapService deployService = super.serviceManager.newServiceInstance(packages+"."+classname);
            //(ILeapService)ClassUtils.instantiate(packages+"."+classname);
            super.serviceManager.addService(deployService);
        } else {
            throw new WASException(MSG_TYPE.ERROR, 54, "Requested: "+bodyPart.getContentType().name());
        }
    }

    @Override
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable throwable) {
        response.addHeader("WWW-Authenticate", "Basic");
        response.setStatusCode(401);
        response.setBody("".getBytes());
        return null;
    }
}
