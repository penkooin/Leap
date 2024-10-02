package org.chaostocosmos.leap.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.exception.ExceptionUtils;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.part.Part;
import org.chaostocosmos.leap.service.abstraction.AbstractService;
import org.chaostocosmos.leap.service.model.DeployModel;
import org.chaostocosmos.leap.service.model.ServiceModel;

/**
 * Hot deploy service 
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath="/deploy")
public class DeployService extends AbstractService implements DeployModel {
    
    /**
     * Add service 
     * @param request
     * @param response
     * @throws LeapException
     * @throws IOException
     */
    @MethodMapper(method = REQUEST.POST, mappingPath = "/service/add")
    public void add(HttpRequest request, HttpResponse response) throws LeapException, IOException {
        final Map<String, List<?>> headers = request.getReqHeader();
        final Part bodyPart = request.getBodyPart();
        if(bodyPart == null) {
            throw new LeapException(HTTP.RES417, "Service class file data is missing in request.");
        } else if(bodyPart.getContentType() == MIME.MULTIPART_FORM_DATA) {
            Object className = headers.get("serviceClassNames");
            if(className == null) {
                throw new LeapException(HTTP.RES417, "Service class name is missiong in request.");
            }
            String qualifiedClassName = className.toString();
            if(qualifiedClassName.toString().startsWith("[") && qualifiedClassName.toString().endsWith("]")) {
                qualifiedClassName = qualifiedClassName.substring(qualifiedClassName.indexOf("[")+1, qualifiedClassName.lastIndexOf("]"));
                if(qualifiedClassName == null || qualifiedClassName.equals("")) {
                    throw new LeapException(HTTP.RES412, "Service class name array is empty!!!");
                }
                super.logger.debug("Deploying service... "+request.getReqHeader().toString());
                Arrays.asList(qualifiedClassName.split(",")).stream().forEach(cls -> {
                    if(cls == null) {
                        throw new LeapException(HTTP.RES412, "Service full qualifiedClassName is missing in the header of request.");
                    }
                    cls = cls.trim();
                    Path serviceClassPath = super.serviceManager.getHost().getClasses();
                    Path qualifiedClassPath = Paths.get(cls.replace(".", File.separator));
                    //System.out.println("-----------------------------------------------------------------"+serviceClassPath.resolve(qualifiedClassPath).toAbsolutePath().toString());        
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
                        super.logger.error(e.getMessage(), e);
                        super.logger.debug("[DEPLOY] Exception in servie deploy process: " + serviceClassPath.resolve(qualifiedClassPath).toString());
                        throw new LeapException(HTTP.RES412, ExceptionUtils.getStackTraces(e));
                    }        
                });
            } else {
                throw new LeapException(HTTP.RES404, "Service class name must be array!!!");
            }            
        } else {
            throw new LeapException(HTTP.RES405, "Requested: "+bodyPart.getContentType().name());
        }
    }

    @MethodMapper(method = REQUEST.GET, mappingPath = "/service/delete")
    public void delete(HttpRequest request, HttpResponse response) throws IOException, 
                                                                          URISyntaxException, 
                                                                          NotSupportedException, 
                                                                          NoSuchMethodException, 
                                                                          SecurityException, 
                                                                          IllegalArgumentException, 
                                                                          InvocationTargetException, 
                                                                          ClassNotFoundException, 
                                                                          InstantiationException, 
                                                                          IllegalAccessException {
        String qualifiedClassName = (String) request.getContextParameter("serviceClassNames");
        if(qualifiedClassName.startsWith("[") && qualifiedClassName.endsWith("]")) {
            qualifiedClassName = qualifiedClassName.substring(qualifiedClassName.indexOf("[")+1, qualifiedClassName.lastIndexOf("]"));
            if(qualifiedClassName == null || qualifiedClassName.equals("")) {
                throw new LeapException(HTTP.RES412, "Service class name is empty!!!");
            }
            List<String> classNames = Arrays.asList(qualifiedClassName.split(",")).stream().map(c -> c.trim()).collect(Collectors.toList());
            for(String className : classNames) {
                super.serviceManager.initialize();
                Path serviceClassPath = super.serviceManager.getHost().getClasses();
                serviceClassPath = serviceClassPath.resolve(className);
                if(serviceClassPath.toFile().exists()) {
                    serviceClassPath.toFile().delete();
                    super.logger.info("[DEPLOY] Delete service -  class: "+className+"  path: "+serviceClassPath.toString());
                } else {
                    throw new LeapException(HTTP.RES404, "Service class file not found: "+serviceClassPath.toString());
                }
            }
        } else {
            throw new LeapException(HTTP.RES404, "Service class name must be array!!!");
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
    public Exception errorHandling(HttpResponse response, Exception throwable) {
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
