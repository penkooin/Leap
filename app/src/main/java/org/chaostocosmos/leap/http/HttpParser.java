package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.HttpRequestDescriptor.MultipartDescriptor;

/**
 * Http parsing factory object
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
        int c;
        String line = "";
        do {
            c = is.read();
            if(c == 0x0D) {
                int lf = is.read();
                break;
            }
            line += c;
        } while(c != -1);
        return line;
    }

    /**
     * Read all requested lines
     * @param is
     * @return
     * @throws IOException
     */
    private static List<String> readRequestLines(InputStream is) throws IOException {
        String line;
        String all = "";
        while(!(line = readLine(is)).equals("")) {
            all += line + System.lineSeparator();
        }
        return Arrays.asList(all.split(System.lineSeparator()));
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
            HttpRequestDescriptor desc;
            try {
                REQUEST_TYPE requestType = null;
                List<String> requestLines = readRequestLines(in);
                Map<String, String> reqHeader = new HashMap<>();
                if(requestLines.size() < 1) {
                    return null;
                }
                String head = requestLines.remove(0);
                String[] token = head.split("\\s+");
                if(token[0].equals(REQUEST_TYPE.GET.name())) {
                    requestType = REQUEST_TYPE.GET;
                } else if(token[0].equals(REQUEST_TYPE.POST.name())) {
                    requestType = REQUEST_TYPE.POST;
                } else {
                    throw new WASException(MSG_TYPE.ERROR, 9, token[0]);
                }
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
                    System.out.println(header.substring(0, idx)+"   "+header.substring(idx + 1, header.length()).trim());
                    reqHeader.put(header.substring(0, idx), header.substring(idx + 1, header.length()).trim());
                }
                System.out.println(reqHeader.get("Host"));
                String str = reqHeader.get("Host").toString().trim();
                String host = !str.startsWith("http://") ? "http://"+str : str;
                URL url;
                url = new URL(host+contextPath);
                String contentType = reqHeader.get("Content-Type");
                String boundary = null;
                MultipartDescriptor multipart = null;
                if(contentType != null) {
                    String s = contentType.substring(contentType.indexOf(":")+1, contentType.indexOf(";")).trim().toUpperCase();
                    s = s.replace("/", "_").replace("-", "_");
                    System.out.println(s);
                    MIME_TYPE mimeType = MIME_TYPE.valueOf(s);
                    long length = Long.parseLong(reqHeader.get("Content-Length"));
                    String[] splited = contentType.split("\\;");
                    contentType = splited[0].trim();
                    boundary = splited[1].substring(splited[1].indexOf("=") + 1).trim();
                    multipart = new MultipartDescriptor(mimeType, boundary, length, in);
                }
                desc = new HttpRequestDescriptor(httpVersion, requestType, url.getHost(), reqHeader, contentType, null, contextPath, url, contextParam, multipart);
                HttpRequest request = HttpBuilder.buildHttpRequest(desc);
                desc.setHttpRequest(request);
            } catch (Exception e1) {
                throw new WASException(MSG_TYPE.ERROR, 42);
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
