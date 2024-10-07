package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.chaostocosmos.leap.http.part.KeyValuePart;
import org.chaostocosmos.leap.http.part.MultiPart;
import org.chaostocosmos.leap.http.part.Part;
import org.chaostocosmos.leap.http.part.TextPart;

/**
 * Http parsing factory object
 * 
 * @author 9ins 
 */
public class HttpParser<T, R> {

    /**
     * Host
     */
    Host<?> host;

    /**
     * InputStream
     */
    InputStream inputStream;

    /**
     * OutputStream
     */
    OutputStream outputStream;

    /**
     * RequestStream
     */
    HttpRequestStream requestStream;

    /**
     * First line of request
     */
    String requestFirstLine;

    /**
     * Request header lines
     */
    List<String> requestHeaderLines;

    /**
     * Request first line Map
     */
    Map<REQUEST_LINE, String> firstLineMap;

    /**
     * Request headers Map
     */
    Map<String, String> requestHeaders;

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
    HttpRequest<T> request;

    /**
     * Response
     */
    HttpResponse<R> response;

    /**
     * Constructor
     * 
     * @param host
     * @param inputStream
     * @param outputStream
     * @throws IOException 
     */
    public HttpParser(Host<?> host, InputStream inputStream, OutputStream outputStream) throws IOException {
        this.host = host;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.requestStream = new HttpRequestStream(this.inputStream, this.host.<Integer> getValue("network.receive-buffer-size"), host.charset());
        this.requestHeaderLines = this.requestStream.readHeaderLines(host.charset());       
        //remove first line from header string list 
        this.requestFirstLine = this.requestHeaderLines.remove(0);
        this.requestFirstLine = URLDecoder.decode(this.requestFirstLine, StandardCharsets.UTF_8);
        //Parse request first line
        this.firstLineMap = parseRequestLine(this.requestFirstLine);
        //Parse query parameters
        this.queryParameters = parseQueryParameters(this.requestFirstLine);
        //Parse request header 
        this.requestHeaders = parseRequestHeaders(this.requestHeaderLines); 
        //Parse cookies from header
        this.requestCookies = parseRequestCookies(this.requestHeaderLines.stream().filter(l -> l.startsWith("Cookie:")).findAny().orElse(null));
    }

    /**
     * Get request header lines
     * @return
     */
    public List<String> getRequestHeaderLines() {
        return this.requestHeaderLines;
    }

    /**
     * Get first line Map
     * @return
     */
    public Map<REQUEST_LINE, String> getFirstLines() {
        return this.firstLineMap;
    }

    /**
     * Get query parameters
     * @return
     */
    public Map<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /**
     * Get request headers 
     * @return
     */
    public Map<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }

    /**
     * Get cookies
     * @return
     */
    public Map<String, String> getRequestCookies() {
        return this.requestCookies;
    }

    /**
     * Parse request
     * @return
     */
    @SuppressWarnings("unchecked")
    public HttpRequest<T> parseRequest() throws LeapException {        
        if(this.request != null) {
            return this.request;
        }
        long requestMillis = 0L;
        try {
            // Request host
            String requestedHost = requestHeaders.get("Host") != null && requestHeaders.get("Host") != null ? requestHeaders.get("Host") : "unknown";
            String hostName = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;

            //Get host ID from request host name
            //String hostId = Context.get().hosts().getId(hostName);
            if(!Context.get().hosts().isExistHostname(hostName)) {
                throw new LeapException(HTTP.RES417, new Exception("Requested host ID not exist in this server. ID: "+hostName));
            }

            REQUEST requestType = REQUEST.valueOf(this.firstLineMap.get(REQUEST_LINE.METHOD).toString());
            String contextPath = this.firstLineMap.get(REQUEST_LINE.CONTEXT).toString();
            String protocol = this.firstLineMap.get(REQUEST_LINE.PROTOCOL).toString();
            Charset charset = requestHeaders.get("Charset") != null && requestHeaders.get("Charset") != null ? Charset.forName(requestHeaders.get("Charset")) : this.host.charset();
            URI requestURI = new URI(protocol+"://"+requestedHost + Base64.getEncoder().encodeToString(contextPath.getBytes()));

            //Get content type from requested header
            String contentType = requestHeaders.get("Content-Type");

            //Get content length from requested header
            long contentLength = requestHeaders.get("Content-Length") != null ? Long.valueOf(requestHeaders.get("Content-Length")) : 0L;

            //Get mime type
            Part<T> bodyPart = null;
            MIME mimeType = null;
            if(contentType != null) {
                int idx = contentType.indexOf(";");
                if(idx != -1) {
                    contentType = contentType.substring(0, idx).trim();
                }
                mimeType = MIME.mimeType(contentType);                
                // Object bodyInStream = null;
                // List<?> list = requestHeaders.get("body-in-stream");
                // if(list != null) {
                //     bodyInStream = list.get(0);
                // }
                // boolean preLoadBody = bodyInStream == null ? false : !Boolean.valueOf(bodyInStream.toString());                
                if(requestType == REQUEST.POST) {
                    bodyPart = (Part<T>) parseBodyParts(mimeType, contentLength, charset);
                }
            }
            this.request = new HttpRequest<T> (this.host, 
                                               requestMillis, 
                                               PROTOCOL.valueOf(protocol.toUpperCase()), 
                                               hostName, 
                                               protocol, 
                                               requestType, 
                                               this.requestHeaders, 
                                               mimeType, 
                                               contextPath, 
                                               requestURI, 
                                               this.queryParameters, 
                                               bodyPart, 
                                               contentLength, 
                                               charset, 
                                               this.requestCookies, 
                                               null);
        } catch(Exception e) {
            e.printStackTrace();
            if(e instanceof LeapException) {
                throw (LeapException) e;
            } else {
                throw new LeapException(HTTP.RES500, e);
            }
        }
        return this.request;
    }

    /**
     * Parse request line
     * @return
     * @throws IOException
     */
    private final Map<REQUEST_LINE, String> parseRequestLine(String firstLine) {        
        if(firstLine == null) {
            throw new LeapException(HTTP.RES400, "Request context line of header is wrong!!!");
        }
        Map<REQUEST_LINE, String> firstLineMap = new HashMap<REQUEST_LINE, String>();
        try {
            if(firstLine.equals("") || firstLine.charAt(0) == 63) {
                throw new LeapException(HTTP.LEAP900, firstLine);
            }                
            this.host.getLogger().info("############################## [REQUEST] "+firstLine);
            final String method = firstLine.substring(0, firstLine.indexOf(" "));
            String contextPath = firstLine.substring(firstLine.indexOf(" ")+1, firstLine.lastIndexOf(" "));
            final String protocolVersion = firstLine.substring(firstLine.lastIndexOf(" ")).trim();
            if(method == null || method == null || protocolVersion == null) {
                throw new LeapException(HTTP.RES417, new IllegalStateException("Requested line format is wrong: "+firstLine));
            }
            if(!Arrays.asList(REQUEST.values()).stream().anyMatch(R -> R.name().equals(method))) {
                throw new LeapException(HTTP.RES405, new NotSupportedException("Not suported request method: "+method));
            }            
            String context = contextPath.indexOf("?") == -1 ? contextPath : contextPath.substring(0, contextPath.indexOf("?"));
            String contextParam = contextPath.indexOf("?") == -1 ? null : contextPath.substring(contextPath.indexOf("?"));
            String protocol = protocolVersion.substring(0, protocolVersion.indexOf("/"));
            firstLineMap.put(REQUEST_LINE.LINE, firstLine);
            firstLineMap.put(REQUEST_LINE.METHOD, method);
            firstLineMap.put(REQUEST_LINE.CONTEXT, context);
            firstLineMap.put(REQUEST_LINE.PARAMS, contextParam);
            firstLineMap.put(REQUEST_LINE.VERSION, protocolVersion);
            firstLineMap.put(REQUEST_LINE.PROTOCOL, protocol);
        } catch(Exception e) {
            if(e instanceof LeapException) {
                throw (LeapException) e;
            } else {
                this.host.getLogger().throwable(e);
                throw new LeapException(HTTP.RES500, e);
            }
        }            
        return firstLineMap;
    }

    /**
     * Parse request header
     * @return
     * @throws IOException
     */
    private final Map<String, String> parseRequestHeaders(List<String> headerLines) throws LeapException {
        Map<String, String> requestHeaders = new HashMap<>();
        try {
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
                requestHeaders.put(headerKey, headerValue.trim());
            }    
        } catch(Exception e) {
            e.printStackTrace();
            if(e instanceof LeapException) {
                throw (LeapException) e;
            } else {
                throw new LeapException(HTTP.RES500, e);
            }
        }
        return requestHeaders;
    }

    /**
     * Parse request cookies
     * @param cookieLine
     * @return
     */
    private Map<String, String> parseRequestCookies(String cookieLine) {
        Map<String, String> requestCookies = new HashMap<>();
        if(cookieLine == null) {
            return requestCookies;
        }
        try {
            String[] cookieArr = cookieLine.split(";");
            for(String cookie : cookieArr) {
                int idx = cookie.indexOf("=");
                if(idx != -1) {
                    String key = cookie.substring(0, idx).trim();
                    String value = cookie.substring(idx+1).trim();
                    requestCookies.putIfAbsent(key, value.equals("null") ? "" : value);    
                }
            }
        } catch(Exception e) {
            if(e instanceof LeapException) {
                throw (LeapException) e;
            } else {
                throw new LeapException(HTTP.RES500, e);
            }
        }
        return requestCookies;
    }

    /**
     * Parse query parameters
     * @param firstLine
     * @return
     */
    private Map<String, String> parseQueryParameters(String firstLine) {
        Map<String, String> queryParameters = new HashMap<>();        
        try {
            int paramsIndex = firstLine.indexOf("?");
            String queryParamString = firstLine.substring(paramsIndex + 1, firstLine.lastIndexOf(" "));
            if(paramsIndex != -1) {
                firstLine = firstLine.substring(0, paramsIndex);
                String[] params = queryParamString.split("&", -1);
                for(String param : params) {
                    String[] keyValue = param.split("=", -1);
                    if(keyValue.length > 1) {
                        queryParameters.put(keyValue[0], keyValue[1]);
                    }
                }
            }    
        } catch(Exception e) {
            if(e instanceof LeapException) {
                throw (LeapException) e;
            } else {
                throw new LeapException(HTTP.RES500, e);
            }
        }
        return queryParameters;
    }

    /**
     * Parse body part
     * @param mimeType
     * @param contentLength
     * @param charset
     * @return
     * @throws LeapException
     */
    private Part<?> parseBodyParts(MIME mimeType, long contentLength, Charset charset) throws LeapException {
        if(this.firstLineMap == null || this.requestHeaders == null || this.requestCookies == null || this.queryParameters == null) {
            throw new LeapException(HTTP.RES500, "Can't parse body part because it was not parsed request first line / request header / request cookie / query parameter.");
        }
        this.host.getLogger().debug("Host ID: "+this.host.getId()+"  MIME: "+mimeType+"  CHARSET: "+charset.toString());
        try {
            switch(mimeType) {
                case APPLICATION_X_WWW_FORM_URLENCODED:
                    return new KeyValuePart(this.host, mimeType, contentLength, this.requestStream, charset);
                case MULTIPART_FORM_DATA:
                    String contentType = this.requestHeaders.get("Content-Type");
                    int idx = contentType.indexOf("boundary=")+"boundary=".length();
                    String boundary = contentType.substring(idx);
                    return new MultiPart(this.host, mimeType, boundary, contentLength, this.requestStream, charset);
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
                    return new BinaryPart(this.host, mimeType, contentLength, this.requestStream, charset);
                case TEXT_PLAIN:
                case TEXT_CSS:
                case TEXT_JAVASCRIPT:
                case APPLICATION_XHTML_XML:
                case APPLICATION_XML:
                case APPLICATION_SPARQL_QUERY:
                case TEXT_HTML:
                case TEXT_XML:
                case TEXT_JSON:
                case TEXT_YAML:
                    return new TextPart(this.host, mimeType, contentLength, this.requestStream, charset);
                default:
                    return null;                        
            }
        } catch(Exception e) {
            if(e instanceof LeapException) {
                throw (LeapException) e;
            } else {
                throw new LeapException(HTTP.RES500, e);
            }
        }
    }

    /**
     * Get response 
     * @param outputStream
     * @param statusCode
     * @param body
     * @param headers
     * @return
     * @throws IOException 
     */
    public HttpResponse<R> getResponse(final int statusCode, final R body, final Map<String, List<String>> headers) throws IOException {
        if(this.response == null) {
            this.response = new HttpResponse<R> (this.host, this.outputStream, statusCode, body, headers);
        }        
        return this.response;
    }

    /**
     * Check request method
     * @param requestType
     * @return
     */
    public static boolean isValidType(String requestType) {
        if( REQUEST.GET.name().equals(requestType) 
            || REQUEST.POST.name().equals(requestType) 
            || REQUEST.PUT.name().equals(requestType) 
            || REQUEST.DELETE.name().equals(requestType) ) {
            return true;
        }
        return false; 
    }

    /**
     * Print http request
     * @param reader
     * @throws IOException
     */
    public static void printRequest(BufferedReader reader) {
        String line; 
        try {
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    } 
}