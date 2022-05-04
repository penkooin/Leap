package org.chaostocosmos.leap.http.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.filters.BasicAuthFilter;
import org.chaostocosmos.leap.http.part.BodyPart;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.resources.Context;
import org.chaostocosmos.leap.http.resources.HostsManager;

@ServiceMapper(path="/deploy")
public class LeapDeployService extends AbstractLeapService implements IDeploy {
    
    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path = "/upload")
    @FilterMapper(preFilters = BasicAuthFilter.class)
    public void deploy(Request request, Response response) throws WASException, IOException {
        Map<String, String> headers = request.getReqHeader();
        BodyPart bodyPart = request.getBodyPart();        
        if(bodyPart == null) {
            throw new WASException(MSG_TYPE.HTTP, 400, "Service class file data is missing in request.");
        } else if(bodyPart.getContentType() == MIME_TYPE.MULTIPART_FORM_DATA) {
            MultiPart multipart = (MultiPart) bodyPart;
            String packages = headers.get("package");
            String classname = headers.get("classname");

            if(packages == null || classname == null) {
                throw new WASException(MSG_TYPE.HTTP, 400, "Package name or class name is missing in header of requeset.");
            }
            
            super.logger.debug("Deploying service... "+request.getReqHeader().toString());
            Path serviceClassesPath = HostsManager.get().getDynamicClaspaths(request.getRequestedHost());
            
            Path packagePath = Paths.get(packages.replace(".", File.separator));
            System.out.println("-----------------------------------------------------------------"+serviceClassesPath.resolve(packagePath).toAbsolutePath().toString());

            multipart.save(serviceClassesPath.resolve(packagePath).toAbsolutePath());
            super.logger.debug("Uploaded service saved: "+serviceClassesPath.resolve(packagePath).toAbsolutePath().toString());
            
            try {
                //Instantate the service
                ILeapService deployService = super.serviceManager.newServiceInstance(packages+"."+classname);
                //Add classpath of the service to ClassLoader
                super.serviceManager.getClassLoader().addPath(serviceClassesPath);
                //Add service to ServiceManager
                super.serviceManager.addService(deployService);
            } catch(NoClassDefFoundError | Exception e) {
                multipart.getFilePaths().stream().forEach(p -> deleteClean(serviceClassesPath.getFileName().toString(), p));
                super.logger.error(Context.getErrorMsg(19, e.getMessage()), e);
                super.logger.debug("Uploaded service delteed: "+serviceClassesPath.resolve(packagePath).toString());
            }
        } else {
            throw new WASException(MSG_TYPE.HTTP, 405, "Requested: "+bodyPart.getContentType().name());
        }
    }

    /**
     * Delete and clean directory structure of Path
     * @param path
     */
    public void deleteClean(String top, Path path) {
        if(!top.equals(path.getFileName().toString())) {
            if(path.toFile().isDirectory()) {            
                if(path.toFile().listFiles().length == 0) {
                    if(path.toFile().delete()) {
                        deleteClean(top, path.getParent());
                    }
                }
            } else {
                if(path.toFile().delete()) {
                    deleteClean(top, path.getParent());
                }
            }    
        }            
    }

    @Override
    public Throwable errorHandling(Response response, Throwable throwable) {
        //response.setStatusCode(response.getStatusCode());
        //response.setBody(throwable.getMessage());
        return throwable;
    }

    @Override
    public void deployService(Class<ILeapService> service) throws Exception {
    }

    @Override
    public void removeService(String serviceName) throws Exception {
    }
}
