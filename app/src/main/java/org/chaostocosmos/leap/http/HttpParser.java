package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.commons.LoggerFactory;

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
    * Read line from stream
     * @param is
     * @return
     * @throws IOException
     */
    private static String readLine(InputStream is) throws IOException {
        char r = '\r', n = '\n';
        String line = "";
        do {
            r = (char)is.read();
            line += r;
            if(r == '\n' && n == '\r') {
                break;
            }
            n = r;
        } while(n != -1);
        return line.trim();
    }

    /**
     * Read all requested lines 
     * @param is
     * @return
     * @throws IOException
     */
    private static List<String> readRequestLines(InputStream is) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = "";
        do {
            char r = '\r', n = '\n';
            do {
                r = (char)is.read();
                line += r;
                if(r == '\n' && n == '\r') {
                    break;
                }
                n = r;
            } while(r != -1);
            if(line.equals("\r\n"))
                break;
            lines.add(line.substring(0, line.indexOf("\r\n")));  
            line = "";
        } while(true);
        return lines;
    }

    /**
     * Read lines from request
     * @param is
     * @return
     * @throws IOException
     */
    private static List<String> readLines(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line;
        List<String> lines = new ArrayList<>();
        while((line=br.readLine()) != null) {
            if(line.trim().equals(""))
                break;
            lines.add(line);
        }
        return lines;
    }

    /**
     * Request parser inner class
     * @author 9ins
     */
    public static class RequestParser {

        /**
         * Read by Reader
         * @param reader
         * @return
         */
        public Map<String, Object> readReader(Reader reader) {
            return null;
        }

        /**
         * Read by InpuStream
         */
        public Map<String, Object> readInputStream(InputStream inputStream, int bufferSize) {
            return null;            
        }

        /**
         * Parse request
         * @throws IOException
         */
        public HttpRequestDescriptor parseRequest0(InputStream in) throws IOException {
            return null;
        }

        /**
         * Parse request
         * @throws IOException
         * @throws WASException
         */
        public HttpRequestDescriptor parseRequest(InputStream in) throws WASException {
            HttpRequestDescriptor desc = null;
            try {
                List<String> requestLines = readRequestLines(in);
                requestLines.stream().forEach(System.out::println);
                Map<String, String> reqHeader = new HashMap<>();
                if(requestLines.size() < 1) {
                    throw new WASException(MSG_TYPE.ERROR, 9);
                }
                String head = requestLines.remove(0);
                String[] token = head.split("\\s+");
                REQUEST_TYPE requestType = REQUEST_TYPE.valueOf(token[0]);
                String contextPath = token[1];
                String httpVersion = token[2];
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
                Map<String, String> contextParam = new HashMap<>();
                int paramsIndex = contextPath.indexOf("?");
                if(paramsIndex != -1) {
                    String[] params = contextPath.substring(paramsIndex+1).split("&", -1);
                    for(String param : params) {
                        String[] keyValue = param.split("=", -1);
                        contextParam.put(keyValue[0], keyValue[1]);
                    }
                }
                String host = requestedHost.indexOf(":") != -1 ? requestedHost.substring(0, requestedHost.indexOf(":")) : requestedHost;
                String contentType = reqHeader.get("Content-Type");
                MIME_TYPE mimeType = contentType.indexOf(";") != -1 ? MIME_TYPE.getMimeType(contentType.substring(0, contentType.indexOf(";"))) : MIME_TYPE.getMimeType(contentType);
                int contentLength = Integer.parseInt(reqHeader.get("Content-Length"));
                String boundary = contentType != null ? contentType.substring(contentType.indexOf(";")+1) : null;
                LoggerFactory.getLogger(requestedHost).debug("Context params: "+contextParam.toString());
                System.out.println(contextPath+"  "+contentType+"   "+contentLength+"--------------------------------------");
                if(contentLength > 0) {
                    switch(mimeType) {
                        case MULTIPART_FORM_DATA:
                            long length = Long.parseLong(reqHeader.get("Content-Length"));
                            String[] splited = contentType.split("\\;");
                            contentType = splited[0].trim();
                            boundary = splited[1].substring(splited[1].indexOf("=") + 1).trim();
                            Multipart multipart = new Multipart(host, mimeType, boundary, length, in);
                            desc = new HttpRequestDescriptor(httpVersion, requestType, host, reqHeader, contentType, null, contextPath, contextParam, multipart);
                            break;
                        case APPLICATION_X_WWW_FORM_URLENCODED:
                            
                            break;
                        case APPLICATION_OCTET_STREAM:
                            break;
                        case TEXT_PLAIN:
                        case TEXT_CSS:
                        case TEXT_JAVASCRIPT:
                        case APPLICATION_XHTML_XML:
                        case APPLICATION_XML:
                        default:
                    }
                }
                HttpRequest request = HttpBuilder.buildHttpRequest(desc);
                desc.setHttpRequest(request);
            } catch (Exception e) {
                throw new WASException(MSG_TYPE.ERROR, 45, e.toString());
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
