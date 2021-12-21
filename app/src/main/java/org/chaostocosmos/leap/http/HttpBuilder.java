package org.chaostocosmos.leap.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

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
    public static HttpRequest buildHttpRequest(final HttpRequestDescriptor requestDescriptor) throws WASException, URISyntaxException {
        HttpRequest.Builder request = HttpRequest.newBuilder(requestDescriptor.getUrl().toURI());
        //Do comment because of header error
        //requestDescriptor.getReqHeader().forEach(request::header);
        if(requestDescriptor.getRequestType() == REQUEST_TYPE.GET) {            
            return request.GET().build();
        } else if(requestDescriptor.getRequestType() == REQUEST_TYPE.POST) {
            return request.POST(BodyPublishers.ofString(requestDescriptor.getReqBody(), Context.getServerCharset())).build();
        } else {
            throw new WASException(MSG_TYPE.ERROR, "error009", requestDescriptor.getRequestType().name());
        }
    }
    /**
     * Build dummy HttpResponse object
     * @param responseParser
     * @return
     */
    public static HttpResponse<Object> buildDummyHttpResponse() {

        return new HttpResponse<Object>() {

            @Override
            public int statusCode() {
                return 0;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<Object>> previousResponse() {
                return null;
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public Object body() {
                return null;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return null;
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public Version version() {
                return null;
            }
        };
    }
}
