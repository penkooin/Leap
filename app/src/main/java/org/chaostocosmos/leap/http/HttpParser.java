package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.enums.PROTOCOL;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.enums.REQUEST_LINE;
import org.chaostocosmos.leap.exception.LeapException;
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
     * Host
     */
    Host<?> host;

    /**
     * RequestStream
     */
    HttpRequestStream requestStream;

    /**
     * OutputStream
     */
    OutputStream outputStream;

    /**
     * Request first line Map
     */
    Map<REQUEST_LINE, String> requestLines;

    /**
     * Request headers Map
     */
    Map<String, List<?>> requestHeaders;

    /**
     * Request cookies Map
     */
    Map<String, String> requestCookies;

    /**
     * Request url query parameters Map
     */
    Map<String, String> queryParameters;

    /**
     * Request
     */
    HttpRequest request;

    /**
     * Response
     */
    HttpResponse response;

    /**
     * Constructor
     * @param host
     * @param inputStream
     * @param outputStream
     * @throws IOException
     */
    public HttpParser(Host<?> host, InputStream inputStream, OutputStream outputStream) throws IOException {
        this.host = host;
        this.requestStream = new HttpRequestStream(inputStream);
        this.outputStream = outputStream;
    }

    /**
     * Check request method
     * @param requestType
     * @return
     */
    public static boolean isValidType(String requestType) {
        if( REQUEST.GET.name().equals(requestType) || REQUEST.POST.name().equals(requestType) || REQUEST.PUT.name().equals(requestType) || REQUEST.DELETE.name().equals(requestType)) {
            return true;
        }
        return false; 
    }

    /**
     * Parse request line
     * @return
     * @throws IOException
     */
    public final Map<REQUEST_LINE, String> parseRequestLine() throws IOException {        
        if(this.requestLines != null) {
            return this.requestLines;
        }
        this.requestLines = new HashMap<REQUEST_LINE, String>();        
        String readLine = this.requestStream.readLine(StandardCharsets.UTF_8);
        if(readLine.equals("") || readLine.charAt(0) == 63) {
            throw new LeapException(HTTP.LEAP900, readLine);
        }
        final String requestLine = URLDecoder.decode(readLine, StandardCharsets.UTF_8);
        //System.out.println(Arrays.toString(requestLine.getBytes()));
        this.host.getLogger().info("############################## [REQUEST] "+requestLine);
        final String method = requestLine.substring(0, requestLine.indexOf(" "));
        String contextPath = requestLine.substring(requestLine.indexOf(" ")+1, requestLine.lastIndexOf(" "));
        this.queryParameters = parseQueryParameters(contextPath);        
        final String protocolVersion = requestLine.substring(requestLine.lastIndexOf(" ")).trim();
        if(method == null || method == null || protocolVersion == null) {
            throw new LeapException(HTTP.RES417, new IllegalStateException("Requested line format is wrong: "+requestLine));
        }
        if(!Arrays.asList(REQUEST.values()).stream().anyMatch(R -> R.name().equals(method))) {
            throw new LeapException(HTTP.RES405, new NotSupportedException("Not suported request method: "+method));
        }
        String protocol = protocolVersion.substring(0, protocolVersion.indexOf("/"));
        this.requestLines.put(REQUEST_LINE.LINE, requestLine);
        this.requestLines.put(REQUEST_LINE.METHOD, method);
        this.requestLines.put(REQUEST_LINE.PATH, contextPath.indexOf("?") == -1 ? contextPath : contextPath.substring(0, contextPath.indexOf("?")));
        this.requestLines.put(REQUEST_LINE.VERSION, protocolVersion);
        this.requestLines.put(REQUEST_LINE.PROTOCOL, protocol);
        return this.requestLines;
    }

    /**
     * Parse request header
     * @return
     * @throws IOException
     */
    public final Map<String, List<?>> parseRequestHeaders() throws IOException {
        if(this.requestLines == null) {
            throw new LeapException(HTTP.RES500, "It must be parsed request first line before headers parsing.");
        }
        if(this.requestHeaders != null) {
            return this.requestHeaders;
        }
        this.requestHeaders = new HashMap<>();
        List<String> headerLines = this.requestStream.readLines(Charset.forName(this.host.charset()));        
        for(String header : headerLines) {
            this.host.getLogger().debug(header);
            if(header == null || header.length() == 0) {
                break;
            }                
            int idx = header.indexOf(":");
            if (idx == -1) {
                throw new LeapException(HTTP.RES417, new IllegalStateException("Header format is wrong(Not found delimeter): "+header));
            }
            String headerKey = header.substring(0, idx);
            String headerValue = header.substring(idx + 1, header.length());
            List<?> headerValues = headerValue.contains(";") ? Stream.of(headerValue.split(";")).map(v -> v.trim()).collect(Collectors.toList()) : Arrays.asList(headerValue.trim());
            this.requestHeaders.put(headerKey, headerValues);
        }
        return this.requestHeaders;
    }

    /**
     * Parse cookies of request header
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> parseRequestCookies() {
        if(this.requestHeaders == null) {
            throw new LeapException(HTTP.RES500, "It must be parsed request headers before cookies parsing.");
        }
        if(this.requestCookies != null) {
            return this.requestCookies;
        }
        this.requestCookies = new HashMap<>();
        if(requestHeaders.get("Cookie") != null) {
            List<String> cookieArr = requestHeaders.get("Cookie") instanceof List ? (List<String>) requestHeaders.get("Cookie") : List.of(requestHeaders.get("Cookie").toString());
            for(String cookie : cookieArr) {
                String key = cookie.substring(0, cookie.indexOf("=")).trim();
                String value = cookie.substring(cookie.indexOf("=")+1);
                this.requestCookies.putIfAbsent(key, value.equals("null") ? "" : value);
            }
        }
        return this.requestCookies;
    }

    /**
     * Parse query parameters
     * @return
     */
    public Map<String, String> parseQueryParameters(String contextPath) {
        if(this.requestLines == null) {
            throw new LeapException(HTTP.RES500, "It must be parsed request first line before query parameters parsing.");
        }
        if(this.queryParameters != null) {
            return this.queryParameters;
        }
        this.queryParameters = new HashMap<>();        
        int paramsIndex = contextPath.indexOf("?");
        String queryParamString = contextPath.substring(paramsIndex + 1);
        if(paramsIndex != -1) {
            contextPath = contextPath.substring(0, paramsIndex);
            String[] params = queryParamString.split("&", -1);
            for(String param : params) {
                String[] keyValue = param.split("=", -1);
                if(keyValue.length > 1) {
                    this.queryParameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return this.queryParameters;
    }

    /**
     * Parse body part
     * @param mimeType
     * @param boundary
     * @param contentLength
     * @param preLoadBody
     * @param charset
     * @return
     * @throws IOException
     */
    public BodyPart parseBodyParts(MIME mimeType, String boundary, long contentLength, boolean preLoadBody, Charset charset) throws IOException {
        if(this.requestLines == null || this.requestHeaders == null || this.requestCookies == null || this.queryParameters == null) {
            throw new LeapException(HTTP.RES500, "Can't parse body part because it was not parsed request first line / request header / request cookie / query parameter.");
        }
        this.host.getLogger().debug("Host ID: "+this.host.getId()+"  MIME: "+mimeType+"  BOUNDARY: "+boundary+"  PRE-LOAD-BODY: "+preLoadBody);
        if(contentLength > 0) {
            switch(mimeType) {
                case MULTIPART_FORM_DATA:
                    return new MultiPart(this.host, mimeType, boundary, contentLength, this.requestStream, preLoadBody, charset);
                case APPLICATION_X_WWW_FORM_URLENCODED:
                    return new KeyValuePart(this.host, mimeType, contentLength, this.requestStream, preLoadBody, charset);
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
                    return new BinaryPart(this.host, mimeType, contentLength, this.requestStream, false, charset);
                case TEXT_PLAIN:
                case TEXT_CSS:
                case TEXT_JAVASCRIPT:
                case APPLICATION_XHTML_XML:
                case APPLICATION_XML:
                case TEXT_HTML:
                case TEXT_XML:
                case TEXT_JSON:
                    return new TextPart(this.host, mimeType, contentLength, this.requestStream, false, charset);
                default:
                    return null;                        
            }
        }
        return null;
    }

    /**
     * Parse request
     * @throws IOException
     * @throws URISyntaxException
     */
    public HttpRequest parseRequest() throws IOException, URISyntaxException {
        if(this.request != null) {
            return this.request;
        }
        long requestMillis = 0L;
        //Parse request line
        Map<REQUEST_LINE, String> requestLines = parseRequestLine();
        //Parse request header 
        Map<String, List<?>> requestHeaders = parseRequestHeaders(); 
        //Parse cookies from header
        Map<String, String> cookies = parseRequestCookies();
        // Request host
        String requestedHost = requestHeaders.get("Host") != null && requestHeaders.get("Host").get(0) != null ? requestHeaders.get("Host").get(0).toString() : "unknown";
        String hostName = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;
        //Get host ID from request host name
        String hostId = Context.get().hosts().getId(hostName);
        if(!Context.get().hosts().isExistHostname(hostName)) {
            throw new LeapException(HTTP.RES417, new Exception("Requested host ID not exist in this server. ID: "+hostName));
        }
        REQUEST requestType = REQUEST.valueOf(requestLines.get(REQUEST_LINE.METHOD).toString());
        String contextPath = requestLines.get(REQUEST_LINE.PATH).toString();
        String protocol = requestLines.get(REQUEST_LINE.PROTOCOL).toString();
        Charset charset = Charset.forName(requestHeaders.get("Charset") != null && requestHeaders.get("Charset").get(0) != null ? requestHeaders.get("Charset").get(0).toString() : this.host.charset());
        URI requestURI = new URI(protocol+"://"+requestedHost + Base64.getEncoder().encodeToString(contextPath.getBytes()));
        //Get content type from requested header
        List<?> contentType = requestHeaders.get("Content-Type");
        //Get content length from requested header
        long contentLength = requestHeaders.get("Content-Length") != null && requestHeaders.get("Content-Length").get(0) != null ? Long.parseLong(requestHeaders.get("Content-Length").get(0).toString()) : 0L;
        //Get mime type
        BodyPart bodyPart = null;            
        MIME mimeType = null;
        if(contentType != null && contentLength > 0) {
            mimeType = MIME.mimeType(contentType.get(0).toString());
            if(contentType.get(1) != null) {
                String boundary = contentType.get(1).toString().substring(contentType.get(1).toString().indexOf("=")+1).trim();
                Object bodyInStream = null;
                List<?> list = requestHeaders.get("body-in-stream");
                if(list != null) {
                    bodyInStream = list.get(0);
                }
                boolean preLoadBody = bodyInStream == null ? false : !Boolean.valueOf(bodyInStream.toString());                
                if(requestType == REQUEST.POST) {
                    bodyPart = parseBodyParts(mimeType, boundary, contentLength, preLoadBody, charset);
                }
            }                        
        }
        this.request = new HttpRequest(this.host, requestMillis, PROTOCOL.valueOf(protocol.toUpperCase()), hostName, protocol, requestType, requestHeaders, mimeType, contextPath, requestURI, this.queryParameters, bodyPart, contentLength, charset, cookies, null);
        return this.request;
    }

    /**
     * Build response 
     * @param outputStream
     * @param statusCode
     * @param body
     * @param headers
     * @return
     */
    public HttpResponse buildResponse(final int statusCode, final Object body, final Map<String, List<String>> headers) {
        if(this.response == null) {
            this.response = new HttpResponse(this.host, this.outputStream, statusCode, body, headers);
        }        
        return this.response;
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
}