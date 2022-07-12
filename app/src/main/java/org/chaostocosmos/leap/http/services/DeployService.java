package org.chaostocosmos.leap.http.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.commons.ExceptionUtils;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.part.Part;
import org.chaostocosmos.leap.http.services.filters.BasicAuthFilter;
import org.chaostocosmos.leap.http.services.model.DeployModel;
import org.chaostocosmos.leap.http.services.model.ServiceModel;

@ServiceMapper(path="/deploy")
public class DeployService extends AbstractService implements DeployModel {
    
    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path = "/service/add")
    @FilterMapper(preFilters = BasicAuthFilter.class)
    public void add(Request request, Response response) throws WASException, IOException {
        final Map<String, String> headers = request.getReqHeader();
        final Part bodyPart = request.getBodyPart();
        if(bodyPart == null) {
            throw new WASException(MSG_TYPE.HTTP, 400, "Service class file data is missing in request.");
        } else if(bodyPart.getContentType() == MIME_TYPE.MULTIPART_FORM_DATA) {            
            String qualifiedClassName = headers.get("serviceClassNames");
            if(qualifiedClassName.startsWith("[") && qualifiedClassName.endsWith("]")) {
                qualifiedClassName = qualifiedClassName.substring(qualifiedClassName.indexOf("[")+1, qualifiedClassName.lastIndexOf("]"));
                if(qualifiedClassName == null || qualifiedClassName.equals("")) {
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), "Service class name array is empty!!!");
                }
                super.logger.debug("Deploying service... "+request.getReqHeader().toString());
                Arrays.asList(qualifiedClassName.split(",")).stream().forEach(cls -> {
                    if(cls == null) {
                        throw new WASException(MSG_TYPE.HTTP, 400, "Service full qualifiedClassName is missing in header of request.");
                    }        
                    cls = cls.trim();
                    Path serviceClassPath = super.serviceManager.getHost().getDynamicClasspaths();                
                    Path qualifiedClassPath = Paths.get(cls.replace(".", File.separator));
                    System.out.println("-----------------------------------------------------------------"+serviceClassPath.resolve(qualifiedClassPath).toAbsolutePath().toString());        
                    MultiPart multipart = (MultiPart) bodyPart;
                    try {
                        //Save multi part service files
                        multipart.save(serviceClassPath.resolve(qualifiedClassPath).toAbsolutePath());
                        super.logger.debug("[DEPLOY] Added service: "+serviceClassPath.resolve(qualifiedClassPath).toAbsolutePath().toString());
                        //Instantate the service
                        ServiceModel deployService = super.serviceManager.newServiceInstance(cls);
                        //Add classpath of the service to ClassLoader
                        super.serviceManager.getClassLoader().addPath(serviceClassPath);
                        //Add service to ServiceManager
                        super.serviceManager.initialize();
                    } catch(NoClassDefFoundError | Exception e) {
                        multipart.getFilePaths().stream().forEach(p -> deleteClean(serviceClassPath.getFileName().toString(), p));
                        super.logger.error(Context.getMessages().getErrorMsg(19, e.getMessage()), e);
                        super.logger.debug("[DEPLOY] Exception in servie deploy process: "+serviceClassPath.resolve(qualifiedClassPath).toString());
                        throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), ExceptionUtils.getStackTraces(e));
                    }        
                });
            } else {
                throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Service class name must be array!!!");
            }            
        } else {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES405.code(), "Requested: "+bodyPart.getContentType().name());
        }
    }

    @MethodMappper(mappingMethod = REQUEST_TYPE.GET, path = "/service/delete")
    @FilterMapper(preFilters = BasicAuthFilter.class)
    public void delete(Request request, Response response) throws IOException, URISyntaxException, NotSupportedException {
        String qualifiedClassName = request.getParameter("serviceClassNames");
        if(qualifiedClassName.startsWith("[") && qualifiedClassName.endsWith("]")) {
            qualifiedClassName = qualifiedClassName.substring(qualifiedClassName.indexOf("[")+1, qualifiedClassName.lastIndexOf("]"));
            if(qualifiedClassName == null || qualifiedClassName.equals("")) {
                throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), "Service class name is empty!!!");
            }
            List<String> classNames = Arrays.asList(qualifiedClassName.split(",")).stream().map(c -> c.trim()).collect(Collectors.toList());
            for(String className : classNames) {
                super.serviceManager.initialize();
                Path serviceClassPath = super.serviceManager.getHost().getDynamicClassPaths().resolve(className);
                if(serviceClassPath.toFile().exists()) {
                    serviceClassPath.toFile().delete();
                    super.logger.info("[DEPLOY] Delete service -  class: "+className+"  path: "+serviceClassPath.toString());
                } else {
                    throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Service class file not found: "+serviceClassPath.toString());
                }
            }
        } else {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Service class name must be array!!!");
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
    public void deployService(Class<ServiceModel> service) throws Exception {
    }

    @Override
    public void removeService(String serviceName) throws Exception {
    }
}
