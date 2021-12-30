package org.chaostocosmos.leap.http;

import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

/**
 * HttpRequest Builder object
 */
public class HttpBuilder {
    /**
     * Build HttpRequest object
     * @param requestDescriptor
     * @return
     * @throws WASException
     * @throws URISyntaxException
     */
    public static HttpRequest buildHttpRequest(final HttpRequestDescriptor requestDescriptor) throws WASException {
        HttpRequest.Builder request;
        try {
            request = HttpRequest.newBuilder(requestDescriptor.getUrl().toURI());
        } catch (URISyntaxException e) {
            throw new WASException(e);
        }
        //Doing comment because of header error
        //requestDescriptor.getReqHeader().forEach(request::header);
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
