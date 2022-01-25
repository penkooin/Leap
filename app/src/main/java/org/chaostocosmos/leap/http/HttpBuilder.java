package org.chaostocosmos.leap.http;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

/**
 * HttpRequest Builder object
 * 
 * @param 9ins
 */
public class HttpBuilder {

    /**
     * Build HttpRequest object
     * @param requestDescriptor
     * @return
     * @throws WASException
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public static HttpRequest buildHttpRequest(final HttpRequestDescriptor requestDescriptor) throws WASException, MalformedURLException {
        HttpRequest.Builder request;
        try {
            String url = !requestDescriptor.getRequestedHost().startsWith("http") ? "http://"+requestDescriptor.getRequestedHost() : requestDescriptor.getRequestedHost();
            request = HttpRequest.newBuilder(new URL(url).toURI());
        } catch (URISyntaxException e) {
            throw new WASException(e);
        }
        //Doing comment because of header error
        //requestDescriptor.getReqHeader().forEach(request::header);
        System.out.println("----------------------------"+new String(requestDescriptor.getReqBody()));
        if(requestDescriptor.getRequestType() == REQUEST_TYPE.GET) {    
            return request.GET().build();
        } else if(requestDescriptor.getRequestType() == REQUEST_TYPE.POST) {

            return request.POST(BodyPublishers.ofByteArray(requestDescriptor.getReqBody())).build();
        } else {
            throw new WASException(MSG_TYPE.ERROR, 9, requestDescriptor.getRequestType().name());
        }
    }

    /**
     * Build dummy HttpResponse object
     * @param responseParser
     * @return
     */
    public static HttpResponseDescriptor buildHttpResponse(final HttpRequestDescriptor httpRequestDescriptor) {
         return new HttpResponseDescriptor(httpRequestDescriptor);
    }
}
