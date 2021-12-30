package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http parsing factory object
 * @author 9ins 
 */
public class HttpParser {
    /**
     * logger
     */
    public static Logger logger = LoggerFactory.getLogger(Context.getDefaultHost()); 

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
         * 
         * @param inputStream
         * @return
         * @throws IOException
         */
        public Map<String, Object> readInputStream(InputStream inputStream) throws IOException {
            char previous = '\0';
            char current = '\0';
            ByteArrayOutputStream headPartStream = new ByteArrayOutputStream();
            ByteArrayOutputStream bodyPartStream = new ByteArrayOutputStream();
            int read;
            while((read = inputStream.read()) != -1) {
                current = (char)read;
                if(previous == '\n' && current == '\n') {
                    bodyPartStream.write(current);
                } else {
                    headPartStream.write(current);
                }
                if(current != '\r') {
                    previous = current;
                }
            }    
            return Map.of("HEAD", headPartStream, "BODY", bodyPartStream, "STREAM", inputStream);
        }

        public Map<String, Object> readReader(Reader reader) {
            return null;
        }

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
         * @throws URISyntaxException
         */
        public HttpRequestDescriptor parseRequest(InputStream in) throws WASException {
            REQUEST_TYPE requestType = null;
            List<String> requestLines = new ArrayList<>();
            try {
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
                for(String line; (line=buffReader.readLine()) != null; ) {
                        if(line.isEmpty()) break;
                        requestLines.add(line);
                }    
            } catch (IOException ios) {
                throw new WASException(MSG_TYPE.ERROR, 41);
            }
            // Map<String, Object> readMap = readInputStream(in);
            // ByteArrayOutputStream headPart = (ByteArrayOutputStream)readMap.get("HEAD");
            // String requestAll = new String(headPart.toByteArray());
            // List<String> requestLines = Arrays.asList(requestAll.split("\n"));

            Map<String, String> reqHeader = new HashMap<>();
            if(requestLines.size() < 1) {
                return null;
            }
            String head = requestLines.remove(0);
            logger.info(head);
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
                reqHeader.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
            }
            String str = reqHeader.get("Host").toString().trim();            
            String host = !str.startsWith("http://") ? "http://"+str : str;            
            URL url;
            try {
                url = new URL(host+contextPath);
            } catch (MalformedURLException e1) {
                throw new WASException(MSG_TYPE.ERROR, 42);
            }
            String contentType = reqHeader.get("Content-Type ");
            byte[] reqBody = null;
            // List<String> bodyLines = new ArrayList<>();
            // if(requestType == REQUEST_TYPE.POST) {
            //     String line = buffReader.readLine();
            //     while(line != null && line.length() > 0){
            //         bodyLines.add(line);
            //         line = buffReader.readLine();
            //     }    
            //     String body = bodyLines.stream().collect(Collectors.joining(System.lineSeparator()));
            //     System.out.println(body);
            //     reqBody = body.getBytes();
            // }
            //ByteArrayOutputStream bodyPart = (ByteArrayOutputStream)readMap.get("BODY");
            reqHeader.entrySet().stream().forEach(e -> logger.info(e.getKey()+"="+e.getValue())); 
            HttpRequestDescriptor desc = new HttpRequestDescriptor(httpVersion, 
                                                                    requestLines.stream().collect(Collectors.joining(System.lineSeparator())), 
                                                                    requestType, 
                                                                    url.getHost(), 
                                                                    reqHeader, 
                                                                    contentType, 
                                                                    reqBody, 
                                                                    contextPath, 
                                                                    url, 
                                                                    contextParam);;
            HttpRequest request = HttpBuilder.buildHttpRequest(desc);   
            desc.setHttpRequest(request);
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
