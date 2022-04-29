package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.StreamUtils;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.part.BinaryPart;
import org.chaostocosmos.leap.http.part.BodyPart;
import org.chaostocosmos.leap.http.part.KeyValuePart;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.part.TextPart;
import org.chaostocosmos.leap.http.resources.HostsManager;

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
    public static RequestParser buildRequestParser() {
        if(requestParser == null) {
            requestParser = new RequestParser();
        }
        return requestParser;
    }

    /**
     * Get response parser object
     * @return
     */
    public static ResponseParser buildResponseParser() {
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
        public HttpRequestDescriptor parseRequest(InputStream in) throws IOException {
            String requestLine = StreamUtils.readLine(in, StandardCharsets.ISO_8859_1);
            if(requestLine == null) {
                throw new WASException(MSG_TYPE.ERROR, 1);
            }
            requestLine = URLDecoder.decode(requestLine, StandardCharsets.UTF_8);
            System.out.println(requestLine);
            String method = requestLine.substring(0, requestLine.indexOf(" "));
            if(!Arrays.asList(REQUEST_TYPE.values()).stream().anyMatch(R -> R.name().equals(method))) {
                throw new WASException(MSG_TYPE.ERROR, 2, method);
            }
            String contextPath = requestLine.substring(requestLine.indexOf(" ")+1, requestLine.lastIndexOf(" "));
            String protocol = requestLine.substring(requestLine.lastIndexOf(" ")+1);
            List<String> headerLines = StreamUtils.readHeaders(in);
            Map<String, String> headerMap = new HashMap<>();
            REQUEST_TYPE requestType = REQUEST_TYPE.valueOf(method);
            for(String header : headerLines) {
                if(header == null || header.length() == 0)
                    break;
                int idx = header.indexOf(":");
                if (idx == -1) {
                    throw new WASException(MSG_TYPE.ERROR, 3, header);
                }
                //System.out.println(header.substring(0, idx)+"   "+header.substring(idx + 1, header.length()).trim());
                headerMap.put(header.substring(0, idx), header.substring(idx + 1, header.length()).trim());
            }
            String requestedHost = headerMap.get("Host").toString().trim();
            Map<String, String> contextParam = new HashMap<>();
            int paramsIndex = contextPath.indexOf("?");
            if(paramsIndex != -1) {
                String paramString = contextPath.substring(paramsIndex+1);
                contextPath = contextPath.substring(0, paramsIndex);
                String[] params = paramString.split("&", -1);
                for(String param : params) {
                    String[] keyValue = param.split("=", -1);
                    System.out.println(Arrays.toString(keyValue));
                    contextParam.put(keyValue[0], keyValue[1]);
                }
            }
            String host = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;
            String debug = "";
            debug += "------------------------------ REQ: "+requestLine+" ------------------------------"+System.lineSeparator();
            debug += headerLines.stream().collect(Collectors.joining(System.lineSeparator()));
            String contentType = headerMap.get("Content-Type");
            long contentLength = headerMap.get("Content-Length") != null ? Long.parseLong(headerMap.get("Content-Length")) : 0L;
            /*
            byte[] bytes = new byte[(int)contentLength];
            in.read(bytes);
            System.out.println(new String(bytes));
            */
            if(!HostsManager.get().isExistHost(host)) {
                throw new WASException(MSG_TYPE.HTTP, 400, "Requested host not exist in this server: "+host);
            }
            Charset charset = HostsManager.get().charset(host);
            LoggerFactory.getLogger(host).debug(debug);
            BodyPart bodyPart = null;
            if(contentType != null) {
                MIME_TYPE mimeType = contentType.indexOf(";") != -1 ? MIME_TYPE.getMimeType(contentType.substring(0, contentType.indexOf(";"))) : MIME_TYPE.getMimeType(contentType);
                String boundary = contentType != null ? contentType.substring(contentType.indexOf(";")+1) : null;
                LoggerFactory.getLogger(host).debug("Context params: "+contextParam.toString());
                LoggerFactory.getLogger(host).debug("Mime Type: "+mimeType);
                if(contentLength > 0) {
                    switch(mimeType) {
                        case MULTIPART_FORM_DATA:
                            String[] splited = contentType.split("\\;");
                            contentType = splited[0].trim();
                            boundary = splited[1].substring(splited[1].indexOf("=") + 1).trim();
                            bodyPart = new MultiPart(host, mimeType, boundary, contentLength, in, false, charset);
                            break;
                        case APPLICATION_X_WWW_FORM_URLENCODED:
                            bodyPart = new KeyValuePart(host, mimeType, contentLength, in, false, charset);
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
                            bodyPart = new BinaryPart(host, mimeType, contentLength, in, false, charset);
                            break;
                        case TEXT_PLAIN:
                        case TEXT_CSS:
                        case TEXT_JAVASCRIPT:
                        case APPLICATION_XHTML_XML:
                        case APPLICATION_XML:
                        case TEXT_HTML:
                        case TEXT_XML:
                        case TEXT_JSON:
                            bodyPart = new TextPart(host, mimeType, contentLength, in, false, charset);
                        default:
                    }
                }
            }
            HttpRequestDescriptor desc = new HttpRequestDescriptor(protocol, requestType, host, headerMap, contentType, new byte[0], contextPath, contextParam, bodyPart, contentLength);
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
         * @throws WASException
         */
        public HttpResponseDescriptor buildResponse(final HttpRequestDescriptor request, 
                                                    final int statusCode, 
                                                    final Object body, 
                                                    final Map<String, List<Object>> headers) {
            return new HttpResponseDescriptor(request, statusCode, body, headers);
        }
    }    
}
