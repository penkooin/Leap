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
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.common.StreamUtils;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.PROTOCOL;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.part.BinaryPart;
import org.chaostocosmos.leap.http.part.BodyPart;
import org.chaostocosmos.leap.http.part.KeyValuePart;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.part.TextPart;

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
         * 
         * @throws IOException
         * @throws HTTPException
         */
        public Request parseRequest(InetAddress inetAddress, InputStream inStream) throws Exception {
            long requestMillis = System.currentTimeMillis();
            StringBuffer debug = new StringBuffer();
            String requestLine = StreamUtils.readLine(inStream, StandardCharsets.ISO_8859_1);
            if(requestLine == null) {
                throw new Exception(Context.getMessages().<String>getErrorMsg(1));
            }
            requestLine = URLDecoder.decode(requestLine, StandardCharsets.UTF_8);
            debug.append("============================== [REQUEST] "+requestLine+" =============================="+System.lineSeparator());
            String[] linesplited = requestLine.split(" ");        
            if(linesplited.length != 3) {
                throw new HTTPException(RES_CODE.RES417, Context.getMessages().<String>getErrorMsg(400, "Requested line is something wrong: "+requestLine));
            }
            String method = linesplited[0];
            if(!Arrays.asList(REQUEST_TYPE.values()).stream().anyMatch(R -> R.name().equals(method))) {
                throw new HTTPException(RES_CODE.RES405, Context.getMessages().<String>getErrorMsg(26, method));
            }
            String contextPath = linesplited[1];
            String protocolVersion = requestLine.substring(requestLine.lastIndexOf(" ") + 1);
            String protocol = protocolVersion.indexOf("/") != -1 ? protocolVersion.substring(0, protocolVersion.indexOf("/")).toLowerCase() : protocolVersion;
            List<String> headerLines = StreamUtils.readHeaders(inStream);
            Map<String, String> headerMap = new HashMap<>();
            REQUEST_TYPE requestType = REQUEST_TYPE.valueOf(method);
            for(String header : headerLines) {
                if(header == null || header.length() == 0)
                    break;
                int idx = header.indexOf(":");
                if (idx == -1) {
                    throw new HTTPException(RES_CODE.RES417, Context.getMessages().<String>getErrorMsg(3, header));
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
            String hostId = Context.getHosts().getHostId(hostName);

            //Get Host object by requested host name
            Host<?> host = Context.getHost(hostId);
            if(host == null) {
                throw new HTTPException(RES_CODE.RES417, Context.getMessages().<String>getErrorMsg(24, hostName));
            }
            
            //Get content type from requested header
            String contentType = headerMap.get("Content-Type");

            //Get cookies
            Map<String, String> cookies = null;
            if(headerMap.get("Cookie") != null) {
                String[] cookieArr = headerMap.get("Cookie").trim().split(";");
                System.out.println(Arrays.toString(cookieArr));
                cookies = Arrays.asList(cookieArr).stream().map(a -> new String[]{a.substring(0, a.indexOf("=")).trim(), a.substring(a.indexOf("=")+1)}).collect(Collectors.toMap(k -> k[0], v -> v[1]));
            }
            
            //Get content length from requested header
            long contentLength = headerMap.get("Content-Length") != null ? Long.parseLong(headerMap.get("Content-Length")) : 0L;
            if(!headerMap.containsKey("Range") && !host.checkRequestAttack(inetAddress.getHostAddress(), protocol+"://"+hostName + contextPath)) {
                LoggerFactory.getLogger(requestedHost).warn("[CLIENT BLOCKED] Too many requested client blocking: "+inetAddress.getHostAddress());
                throw new HTTPException(RES_CODE.RES429, requestedHost+" requested too many on short period!!!");
            }
            if(!Context.getHosts().isExistHostname(hostName)) {
                throw new HTTPException(RES_CODE.RES417, Context.getMessages().<String>getErrorMsg(400, "Requested host ID not exist in this server. ID: "+hostName));
            }            
            debug.append("Host ID: "+hostId);
            Charset charset = Context.getHosts().charset(hostId);
            LoggerFactory.getLogger(hostId).debug(debug.toString());
            BodyPart bodyPart = null;
            MIME_TYPE mimeType = null;
            if(contentType != null) {
                mimeType = contentType.indexOf(";") != -1 ? MIME_TYPE.mimeType(contentType.substring(0, contentType.indexOf(";"))) : MIME_TYPE.mimeType(contentType);
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
         * @throws HTTPException
         */
        public Response buildResponse(final Request request, 
                                      final int statusCode, 
                                      final Object body, 
                                      final Map<String, List<String>> headers) {
            return new Response(request, statusCode, body, headers);
        }
    }    
}
