package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.StreamUtils;

/**
 * Http parsing factory object
 * 
 * @author 9ins 
 */
public class HttpParser {    

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
        public HttpRequestDescriptor parseRequest(InputStream in) throws WASException {
            HttpRequestDescriptor desc = null;
            try {
                String requestLine = StreamUtils.readLine(in, StandardCharsets.ISO_8859_1);
                if(requestLine == null) {
                    throw new WASException(MSG_TYPE.ERROR, 9);
                }
                requestLine = URLDecoder.decode(requestLine, Context.charset());
                System.out.println(requestLine);
                List<String> headers = StreamUtils.readHeaders(in);
                headers.stream().forEach(System.out::println);
                Map<String, String> reqHeader = new HashMap<>();
                String method = requestLine.substring(0, requestLine.indexOf(" "));
                String contextPath = requestLine.substring(requestLine.indexOf(" ")+1, requestLine.lastIndexOf(" "));
                String protocol = requestLine.substring(requestLine.lastIndexOf(" ")+1);
                REQUEST_TYPE requestType = REQUEST_TYPE.valueOf(method);
                for(String header : headers) {
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
                Map<String, String> contextParam = new HashMap<>();
                int paramsIndex = contextPath.indexOf("?");
                if(paramsIndex != -1) {
                    String paramString = contextPath.substring(paramsIndex+1);
                    contextPath = contextPath.substring(0, paramsIndex);
                    System.out.println(paramString);
                    if(paramString.indexOf("&") != -1) {
                        String[] params = paramString.split("&", -1);
                        for(String param : params) {
                            String[] keyValue = param.split("=", -1);
                            contextParam.put(keyValue[0], keyValue[1]);
                        }    
                    }
                }
                String host = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;
                String contentType = reqHeader.get("Content-Type");                
                long contentLength = reqHeader.get("Content-Length") != null ? Long.parseLong(reqHeader.get("Content-Length")) : 0L;
                BodyPart bodyPart = null;
                if(contentType != null) {
                    MIME_TYPE mimeType = contentType.indexOf(";") != -1 ? MIME_TYPE.getMimeType(contentType.substring(0, contentType.indexOf(";"))) : MIME_TYPE.getMimeType(contentType);
                    String boundary = contentType != null ? contentType.substring(contentType.indexOf(";")+1) : null;
                    LoggerFactory.getLogger(host).debug("Context params: "+contextParam.toString());
                    if(contentLength > 0) {
                        switch(mimeType) {
                            case MULTIPART_FORM_DATA:
                                String[] splited = contentType.split("\\;");
                                contentType = splited[0].trim();
                                boundary = splited[1].substring(splited[1].indexOf("=") + 1).trim();
                                bodyPart = new MultiPart(host, mimeType, boundary, contentLength, in);
                                break;
                            case APPLICATION_X_WWW_FORM_URLENCODED:
                                break;
                            case APPLICATION_OCTET_STREAM:
                                break;
                            case IMAGE_GIF:
                            case IMAGE_PNG:
                            case IMAGE_JPEG:
                            case IMAGE_BMP:
                            case IMAGE_WEBP:
                            case AUDIO_MIDI:
                            case AUDIO_MPEG:
                            case AUDIO_WEBM:
                            case AUDIO_OGG:
                            case AUDIO_WAV:
                            case VIDEO_WEBM:
                            case VIDEO_OGG:
                                bodyPart = new BinaryPart(host, mimeType, contentLength, in);
                                break;
                            case TEXT_PLAIN:
                            case TEXT_CSS:
                            case TEXT_JAVASCRIPT:
                            case APPLICATION_XHTML_XML:
                            case APPLICATION_XML:
                            default:
                        }
                    }    
                }
                desc = new HttpRequestDescriptor(protocol, requestType, host, reqHeader, contentType, new byte[0], contextPath, contextParam, bodyPart, contentLength);
                HttpRequest request = HttpBuilder.buildHttpRequest(desc);
                desc.setHttpRequest(request);
            } catch (Exception e) {
                throw new WASException(MSG_TYPE.ERROR, 45, e);
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
