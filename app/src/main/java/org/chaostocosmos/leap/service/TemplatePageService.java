package org.chaostocosmos.leap.service;

import java.util.stream.Collectors;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.PreFilters;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.service.filter.BasicAuthFilter;

/**
 * TemplatePageService
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "")
public class TemplatePageService extends AbstractService {

    @Override
    public Exception errorHandling(HttpResponse response, Exception throwable) {
        return throwable;
    }

    @MethodMapper(method = REQUEST.GET, mappingPath = "/error")
    @PreFilters(filterClasses = {BasicAuthFilter.class})
    public void error(HttpRequest request, HttpResponse response) throws Exception {
        //System.out.println(request.getContextParam().toString());
        String errorPage = super.resourcesModel.getErrorPage(request.getContextParam().entrySet().stream().collect(Collectors.toMap(k -> "@"+k.getKey(), v -> v.getValue())));
        response.setResponseCode(Integer.parseInt(request.getParameter("code").toString()));
        response.addHeader("Content-Type", MIME.TEXT_HTML.mimeType());
        response.setBody(errorPage);
    }
    
    @MethodMapper(method = REQUEST.GET, mappingPath = "/response")
    public void response(HttpRequest request, HttpResponse reponse) throws Exception {
        
    }  
}
