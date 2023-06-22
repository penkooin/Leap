package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.common.StreamUtils;
import org.chaostocosmos.leap.part.BinaryPart;
import org.chaostocosmos.leap.part.BodyPart;
import org.chaostocosmos.leap.part.KeyValuePart;
import org.chaostocosmos.leap.part.MultiPart;
import org.chaostocosmos.leap.part.TextPart;

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
        if( REQUEST.GET.name().equals(requestType) || 
            REQUEST.POST.name().equals(requestType) || 
            REQUEST.PUT.name().equals(requestType) || 
            REQUEST.DELETE.name().equals(requestType)) {
            return true;
        }
        return false; 
    }

    /**
     * Print http request
     * @param in
     * @throws IOException
     */
    private void printRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        in.close();
    }
    
    /**
     * Request parser inner class
     * @author 9ins
     */
    public static class RequestParser {
        /**
         * Parse request
         * @throws IOException
         * @throws LeapException
         */
        public Request parseRequest(InetAddress inetAddress, InputStream inStream) throws Exception {
            long requestMillis = System.currentTimeMillis();
            StringBuffer debug = new StringBuffer();
            String requestLine = StreamUtils.readLine(inStream, StandardCharsets.ISO_8859_1);
            if(requestLine == null) {
                throw new Exception(Context.get().messages().<String>error(1));
            }
            requestLine = URLDecoder.decode(requestLine, StandardCharsets.UTF_8);
            debug.append("============================== [REQUEST] "+requestLine+" =============================="+System.lineSeparator());
            String[] linesplited = requestLine.split(" ");
            if(linesplited.length != 3) {
                throw new LeapException(HTTP.RES417, Context.get().messages().<String>error(400, "Requested line is something wrong: "+requestLine));
            }
            String method = linesplited[0];
            if(!Arrays.asList(REQUEST.values()).stream().anyMatch(R -> R.name().equals(method))) {
                throw new LeapException(HTTP.RES405, Context.get().messages().<String>error(26, method));
            }
            String contextPath = linesplited[1];
            String protocolVersion = requestLine.substring(requestLine.lastIndexOf(" ") + 1);
            String protocol = protocolVersion.indexOf("/") != -1 ? protocolVersion.substring(0, protocolVersion.indexOf("/")).toLowerCase() : protocolVersion;
            List<String> headerLines = StreamUtils.readHeaders(inStream);
            Map<String, String> headerMap = new HashMap<>();
            REQUEST requestType = REQUEST.valueOf(method);
            for(String header : headerLines) {
                if(header == null || header.length() == 0)
                    break;
                int idx = header.indexOf(":");
                if (idx == -1) {
                    throw new LeapException(HTTP.RES417, Context.get().messages().<String>error(3, header));
                }
                debug.append(header.substring(0, idx)+":   "+header.substring(idx + 1, header.length()).trim()+Constants.LS);
                headerMap.put(header.substring(0, idx), header.substring(idx + 1, header.length()).trim());
            }
            String requestedHost = headerMap.get("Host").toString().trim();
            Map<String, Object> queryParam = new HashMap<>();
            int paramsIndex = contextPath.indexOf("?");
            String queryParamString = contextPath.substring(paramsIndex + 1);
            URI requestURI = new URI(protocol+"://"+requestedHost + contextPath);
            if(paramsIndex != -1) {
                contextPath = contextPath.substring(0, paramsIndex);
                String[] params = queryParamString.split("&", -1);
                for(String param : params) {
                    String[] keyValue = param.split("=", -1);
                    if(keyValue.length > 1) {
                        queryParam.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            String hostName = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;
            //Get host ID from request host name
            String hostId = Context.get().hosts().getHostId(hostName);
            //Get Host object by requested host name
            Host<?> host = Context.get().host(hostId);
            if(host == null) {
                throw new LeapException(HTTP.RES417, Context.get().messages().<String>error(24, hostName));
            }
            //Get content type from requested header
            String contentType = headerMap.get("Content-Type");
            //System.out.println(headerMap.toString());
            //Get cookies
            Map<String, String> cookies = new HashMap<>();
            if(headerMap.get("Cookie") != null) {
                //System.out.println(headerMap.get("Cookie")+"  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                String[] cookieArr = headerMap.get("Cookie").trim().split(";");
                //System.out.println(Arrays.toString(cookieArr));
                for(String cookie : cookieArr) {
                    String key = cookie.substring(0, cookie.indexOf("=")).trim();
                    String value = cookie.substring(cookie.indexOf("=")+1);
                    cookies.putIfAbsent(key, value.equals("null") ? "" : value);
                }
            }
            //Get content length from requested header
            long contentLength = headerMap.get("Content-Length") != null ? Long.parseLong(headerMap.get("Content-Length")) : 0L;
            if(!headerMap.containsKey("Range") && !host.checkRequestAttack(inetAddress.getHostAddress(), protocol+"://"+hostName + contextPath)) {
                LoggerFactory.getLogger(requestedHost).warn("[CLIENT BLOCKED] Too many requested client blocking: "+inetAddress.getHostAddress());
                throw new LeapException(HTTP.RES429, requestedHost+" requested too many on short period!!!");
            }
            if(!Context.get().hosts().isExistHostname(hostName)) {
                throw new LeapException(HTTP.RES417, Context.get().messages().<String>error(400, "Requested host ID not exist in this server. ID: "+hostName));
            }            
            debug.append("Host ID: "+hostId);
            Charset charset = Context.get().hosts().charset(hostId);
            LoggerFactory.getLogger(hostId).debug(debug.toString());
            BodyPart bodyPart = null;
            MIME mimeType = null;
            if(contentType != null) {
                mimeType = contentType.indexOf(";") != -1 ? MIME.mimeType(contentType.substring(0, contentType.indexOf(";"))) : MIME.mimeType(contentType);
                String boundary = contentType != null ? contentType.substring(contentType.indexOf(";")+1) : null;
                String bodyInStream = headerMap.get("body-in-stream");
                boolean preLoadBody = bodyInStream == null ? false : !Boolean.valueOf(bodyInStream);
                LoggerFactory.getLogger(hostId).debug("MIME: "+mimeType+"  BOUNDARY: "+boundary+"  PRE-LOAD-BODY: "+preLoadBody+"  CONTEXT-PARAMS: "+queryParam.toString());

                if(contentLength > 0) {
                    switch(mimeType) {
                        case MULTIPART_FORM_DATA:
                            String[] splited = contentType.split("\\;");
                            contentType = splited[0].trim();
                            boundary = splited[1].substring(splited[1].indexOf("=") + 1).trim();                            
                            bodyPart = new MultiPart(hostId, mimeType, boundary, contentLength, inStream, preLoadBody, charset);
                            break;
                        case APPLICATION_X_WWW_FORM_URLENCODED:
                            bodyPart = new KeyValuePart(hostId, mimeType, contentLength, inStream, preLoadBody, charset);
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
                            bodyPart = new BinaryPart(hostId, mimeType, contentLength, inStream, false, charset);
                            break;
                        case TEXT_PLAIN:
                        case TEXT_CSS:
                        case TEXT_JAVASCRIPT:
                        case APPLICATION_XHTML_XML:
                        case APPLICATION_XML:
                        case TEXT_HTML:
                        case TEXT_XML:
                        case TEXT_JSON:
                            bodyPart = new TextPart(hostId, mimeType, contentLength, inStream, false, charset);
                        default:
                    }
                }
            }            
            return new Request(requestMillis, PROTOCOL.valueOf(protocol.toUpperCase()), hostId, hostName, protocol, requestType, headerMap, mimeType, contextPath, requestURI, queryParam, bodyPart, contentLength, charset, cookies, null);
        }
    }

    /**
     * Response parser inner class
     */
    public static class ResponseParser {
        /**
         * parse response
         * @return
         * @throws LeapException
         */
        public Response buildResponse(final Request request, 
                                      final int statusCode, 
                                      final Object body, 
                                      final Map<String, List<String>> headers) {
            return new Response(request, statusCode, body, headers);
        }
    }    
}