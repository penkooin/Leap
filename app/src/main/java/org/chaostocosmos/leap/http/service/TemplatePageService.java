package org.chaostocosmos.leap.http.service;

import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.MethodIndicates;
import org.chaostocosmos.leap.http.annotation.ServiceIndicates;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;

/**
 * TemplatePageService
 * 
 * @author 9ins
 */
@ServiceIndicates(path = "")
public class TemplatePageService extends AbstractService {

    @Override
    public Exception errorHandling(Response response, Exception throwable) {
        return throwable;
    }

    @MethodIndicates(mappingMethod = REQUEST_TYPE.GET, path = "/error")
    public void error(Request request, Response response) throws Exception {
        //System.out.println(request.getContextParam().toString());
        String errorPage = super.resource.getErrorPage(request.getContextParam().entrySet().stream().collect(Collectors.toMap(k -> "@"+k.getKey(), v -> v.getValue())));
        response.setResponseCode(Integer.parseInt(request.getParameter("code").toString()));
        response.addHeader("Content-Type", MIME_TYPE.TEXT_HTML.mimeType());
        response.setBody(errorPage);
    }
    
    @MethodIndicates(mappingMethod = REQUEST_TYPE.GET, path = "/response")
    public void response(Request request, Response reponse) throws Exception {
        
    }  
}
