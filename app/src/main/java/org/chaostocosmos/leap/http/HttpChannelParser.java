package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.StreamUtils;

/**
 * HttpChannelParser
 * 
 * @author 9ins
 */
public class HttpChannelParser {

    /**
     * Http request parser
     */
    private static RequestParser requestParser;

    /**
     * Http response parser
     */
    private static ResponseParser responseParser;    

    /**
     * Get request parser object
     * @return
     * @throws IOException
     */
    public static RequestParser getRequestParser() {
        if(requestParser == null) {
            requestParser = new RequestParser();
        }
        return requestParser;
    }

    /**
     * Get response parser object
     * @return
     */
    public static ResponseParser getResponseParser() {
        if(responseParser == null) {
            responseParser = new ResponseParser();
        }
        return responseParser;
    }

    /**
     * Check request method
     * @param requestType
     * @return
     */
    public static boolean isValidType(String requestType) {
        if( REQUEST_TYPE.GET.name().equals(requestType) || 
            REQUEST_TYPE.POST.name().equals(requestType) || 
            REQUEST_TYPE.PUT.name().equals(requestType) || 
            REQUEST_TYPE.DELETE.name().equals(requestType)) {
            return true;
        }
        return false; 
    }

    /**
     * Request parser inner class
     * @author 9ins
     */
    public static class RequestParser {
        /**
         * Parse request
         * @throws IOException
         * @throws WASException
         */
        public HttpRequestDescriptor parseRequest(SocketChannel ch) throws WASException {
            ByteBuffer bb = ByteBuffer.allocate(1024);
            HttpRequestDescriptor desc;
            try {
                List<String> requestLines = StreamUtils.requestLines(ch);
                Map<String, String> reqHeader = new HashMap<>();
                if(requestLines.size() < 1) {
                    throw new WASException(MSG_TYPE.ERROR, 9);
                }
                String head = requestLines.remove(0);
                String[] token = head.split("\\s+");
                REQUEST_TYPE requestType = REQUEST_TYPE.valueOf(token[0]);
                String contextPath = token[1];
                String httpVersion = token[2];
                Map<String, String> contextParam = new HashMap<>();
                for(String header : requestLines) {
                    if(header == null || header.length() == 0)
                        break;
                    int idx = header.indexOf(":");
                    if (idx == -1) {
                        throw new WASException(MSG_TYPE.ERROR, 7, header);
                    }
                    //System.out.println(header.substring(0, idx)+"   "+header.substring(idx + 1, header.length()).trim());
                    reqHeader.put(header.substring(0, idx), header.substring(idx + 1, header.length()).trim());
                }
                String requestedHost = reqHeader.get("Host").toString().trim();
                String host = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;
                String contentType = reqHeader.get("Content-Type");
                String boundary = null;
                BodyPart multipart = null;
                if(contentType != null) {
                    String s = contentType.substring(contentType.indexOf(":")+1, contentType.indexOf(";")).trim().toUpperCase();
                    s = s.replace("/", "_").replace("-", "_");
                    MIME_TYPE mimeType = MIME_TYPE.valueOf(s);
                    long length = Long.parseLong(reqHeader.get("Content-Length"));
                    String[] splited = contentType.split("\\;");
                    contentType = splited[0].trim();
                    boundary = splited[1].substring(splited[1].indexOf("=") + 1).trim();
                    //multipart = new MultipartDescriptor(host, mimeType, boundary, length, in);
                }
                desc = new HttpRequestDescriptor(httpVersion, requestType, host, reqHeader, contentType, null, contextPath, contextParam, multipart, 0);
                HttpRequest request = HttpBuilder.buildHttpRequest(desc);
                desc.setHttpRequest(request);
            } catch (Exception e) {
                throw new WASException(e);
            }
            return desc;
        }
    }

    /**
     * Response parser inner class
     */
    public static class ResponseParser {
        /**
         * parse response
         * @return
         */
        public HttpResponseDescriptor createDummyHttpResponseDescriptor(final HttpRequestDescriptor request, 
                                                                        final int responseCode, 
                                                                        final byte[] responseBody, 
                                                                        final Map<String, Object> headers) {
            return new HttpResponseDescriptor(request);
        }
    }        
}
