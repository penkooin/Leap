package org.chaostocosmos.leap.http.service;

import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.enums.MIME;
import org.chaostocosmos.leap.http.enums.REQUEST;
import org.chaostocosmos.leap.http.inject.MethodIndicates;
import org.chaostocosmos.leap.http.inject.ServiceIndicates;

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

    @MethodIndicates(method = REQUEST.GET, path = "/error")
    public void error(Request request, Response response) throws Exception {
        //System.out.println(request.getContextParam().toString());
        String errorPage = super.resource.getErrorPage(request.getContextParam().entrySet().stream().collect(Collectors.toMap(k -> "@"+k.getKey(), v -> v.getValue())));
        response.setResponseCode(Integer.parseInt(request.getParameter("code").toString()));
        response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType());
        response.setBody(errorPage);
    }
    
    @MethodIndicates(method = REQUEST.GET, path = "/response")
    public void response(Request request, Response reponse) throws Exception {
        
    }  
}
