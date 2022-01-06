package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.ArrayList;
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
        int c = 0x00;
        String line = "";
        do {
            System.out.println(c);                
            if(c == 0x0D || c == -1) {
                //int lf = is.read();
                break;
            } else {
                line += (char)c;
            }
        } while((c = is.read()) != -1);
        System.out.println(line+" $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        return line.trim();
    }

    /**
     * Read all requested lines
     * @param is
     * @return
     * @throws IOException
     */
    private static List<String> readRequestLines(InputStream is) throws IOException {
        String line;
        List<String> lines = new ArrayList<>();
        while(!(line = readLine(is)).equals("")) {
            System.out.println(line);
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
            HttpRequestDescriptor desc;
            try {
                List<String> requestLines = readRequestLines(in);
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
