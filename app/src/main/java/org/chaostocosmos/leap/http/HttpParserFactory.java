package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
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
 */
public class HttpParserFactory {
    /**
     * logger
     */
    public static Logger logger = LoggerFactory.getLogger(HttpParserFactory.class);

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
    public static RequestParser getRequestParser() throws IOException {
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
     * Request parser inner class
     */
    public static class RequestParser {
        /**
         * Parse request
         * @throws IOException
         * @throws WASException
         * @throws URISyntaxException
         */
        public HttpRequestDescriptor parseRequest(Reader reader) throws IOException, WASException, URISyntaxException {
            BufferedReader buffReader = new BufferedReader(reader);
            Map<String, String> reqHeader = new HashMap<>();
            REQUEST_TYPE requestType = null;
            List<String> requestLines = new ArrayList<>();
            for(String line; (line=buffReader.readLine()) != null; ) {
                if(line.isEmpty()) break;
                requestLines.add(line);
            }
            String requestAll = requestLines.stream().collect(Collectors.joining(System.lineSeparator()));

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
                throw new WASException(MSG_TYPE.ERROR, "error009", token[0]);
            }
            String httpVersion = token[2];
            String contextPath = token[1];

            Map<String, String> contextParam = new HashMap<>();

            for(String header : requestLines) {
                if(header == null || header.length() == 0)
                    break;
                int idx = header.indexOf(":");
                if (idx == -1) {
                    throw new WASException(MSG_TYPE.ERROR, "error007", header);
                }
                reqHeader.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
            }
            String str = reqHeader.get("Host").toString().trim();            
            String host = !str.startsWith("http://") ? "http://"+str : str;            
            URL url = new URL(host+contextPath);
            String contentType = reqHeader.get("Content-Type ");
            String reqBody = null;
            List<String> bodyLines = new ArrayList<>();
            if(requestType == REQUEST_TYPE.POST) {
                String line = buffReader.readLine();
                while(line != null && line.length() > 0){
                    bodyLines.add(line);
                    line = buffReader.readLine();
                }    
                reqBody = bodyLines.stream().collect(Collectors.joining(System.lineSeparator()));
            }
            //reqHeader.entrySet().stream().forEach(e -> logger.info(e.getKey()+"="+e.getValue())); 
            HttpRequestDescriptor desc = new HttpRequestDescriptor(httpVersion, 
                                                                    requestAll, 
                                                                    requestType, 
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
        public HttpResponseDescriptor createDummyHttpResponseDescriptor() {
            return new HttpResponseDescriptor();
        }
    }    
}
