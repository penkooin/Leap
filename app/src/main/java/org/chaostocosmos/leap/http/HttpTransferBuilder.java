package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.commons.ResourceHelper;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * HttpResponseTransfer
 * 
 * @author 9ins
 */
public class HttpTransferBuilder {
    /**
     * HttpTransfer object
     * @author 9ins
     */
    public static class HttpTransfer {
        /**
         * Requested host
         */
        String vHost;
        /**
         * Client Socket
         */
        Socket client;
        /**
         * InputStream
         */
        InputStream clientInputStream;
        /**
         * OutputStream
         */
        OutputStream clientOutputStream;
        /**
         * Http request
         */
        HttpRequestDescriptor httpRequestDescriptor;
        /**
         * Http response
         */
        HttpResponseDescriptor httpResponseDescriptor;

        /**
         * Construct with request host and client socket
         * @param vHost
         * @param client
         * @throws IOException
         * @throws WASException
         */
        public HttpTransfer(String vHost, Socket client) {
            try {
                this.vHost = vHost;
                this.client = client;
                this.clientInputStream = this.client.getInputStream();
                this.clientOutputStream = this.client.getOutputStream();
                this.httpRequestDescriptor = parseRequest();
                this.httpResponseDescriptor = HttpResponseBuilder.getBuilder()
                                                                 .build(this.httpRequestDescriptor)
                                                                 .setStatusCode(-1)
                                                                 .setBody(null)
                                                                 .setHeaders(new HashMap<String, List<Object>>())
                                                                 .get();        
            } catch(Exception e) {
                LoggerFactory.getLogger(vHost).error(e.getMessage());
            }
        }

        /**
         * Get client InputStream
         * @return
         */
        public InputStream getClientInputStream() {
            return this.clientInputStream;
        }

        /**
         * Get client OutputStream
         * @return
         */
        public OutputStream getClientOutputStream() {
            return this.clientOutputStream;
        }

        /**
         * Get HttpRequestDescriptor
         * @return
         */
        public HttpRequestDescriptor getRequest() {
            return this.httpRequestDescriptor;
        }

        /**
         * Get HttpResponseDescriptor
         * @return
         */
        public HttpResponseDescriptor getResponse() {
            return this.httpResponseDescriptor;
        }

        /**
         * Get HttpRequestDescriptor object
         * @return
         * @throws WASException
         * @throws IOException
         * @throws Exception
         */
        private HttpRequestDescriptor parseRequest() throws IOException, WASException {
            return HttpParser.buildRequestParser().parseRequest(this.clientInputStream);
        }

        /**
         * Send response to client
         * @param out
         * @param response
         * @throws IOException
         */
        public void sendResponse(HttpResponseDescriptor response) throws IOException {
            System.out.println(Context.getHttpMsg(response.getStatusCode()));
            String res = Context.getProtocol(response.getRequestedHost()).name()+" "+response.getStatusCode()+" "+RES_CODE.valueOf("RES"+response.getStatusCode()).getMessage()+"\r\n"; 
            this.clientOutputStream.write(res.getBytes());
            Object body = response.getBody();
            if(body == null) {
                throw new IllegalArgumentException("Response body not set. Something wrong in Respose process!!!");
            }
            if(response.getHeaders().get("Content-Length") == null) {
                int contentLength = body instanceof byte[] ? ((byte[])body).length : (int)((Path)body).toFile().length();
                response.addHeader("Content-Length", contentLength+"");
            }
            //LoggerFactory.getLogger(response.getRequestedHost()).debug(response.toString());
            StringBuffer resStr = new StringBuffer();
            resStr.append("============================== RES : "+res.trim()+" - "+this.client.getRemoteSocketAddress().toString()+" =============================="+System.lineSeparator());
            resStr.append("RES CODE: "+response.getStatusCode()+System.lineSeparator());
            for(Map.Entry<String, List<Object>> e : response.getHeaders().entrySet()) {
                this.clientOutputStream.write((e.getKey()+": "+e.getValue().stream().map(v -> v.toString()).collect(Collectors.joining("; "))+"\r\n").getBytes());
                resStr.append(e.getKey()+": "+e.getValue()+System.lineSeparator());
            }
            LoggerFactory.getLogger(response.getRequestedHost()).debug(resStr.substring(0, resStr.length()-1));
            this.clientOutputStream.write("\r\n".getBytes());
            this.clientOutputStream.flush(); 
            if(body instanceof byte[]) {
                this.clientOutputStream.write((byte[]) body);
            } else {
                if(body instanceof String) {
                    this.clientOutputStream.write(body.toString().getBytes(Context.charset()));
                } else if(body instanceof File) {
                    writeToStream((File)body, this.clientOutputStream, Context.getFileBufferSize());
                } else if(body instanceof Path) {
                    writeToStream(((Path)body).toFile(), this.clientOutputStream, Context.getFileBufferSize());
                } else {
                    throw new IllegalArgumentException("Not supported response body type: "+body.getClass().getName());
                }
            }
            this.clientOutputStream.flush();
        }

        /**
         * Write resource to OutputStream for client
         * @param resource
         * @param out
         * @param bufferSize
         * @throws IOException
         */
        private void writeToStream(File resource, OutputStream out, int bufferSize) throws IOException {
            byte[] buffer = new byte[bufferSize];
            FileInputStream in = new FileInputStream(resource);
            int len;
            while((len=in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
        }

        /**
         * Close client connection
         * @throws IOException
         */
        public void close() {
            try {
                if(this.clientInputStream != null) {
                    this.clientInputStream.close();
                }
                if(this.clientOutputStream != null) {
                    this.clientOutputStream.close();
                }
                if(this.client != null) {
                    this.client.close();
                }    
            } catch(Exception e) {
                LoggerFactory.getLogger(this.vHost).error(e.getMessage(), e);
            }
            LoggerFactory.getLogger(this.vHost).info("Client closing......"+this.client.getInetAddress().toString());
        }
    }

    /**
     * Build error response messasge
     * @param requestedHost
     * @param e
     * @return
     * @throws WASException
     */
    public static String buildErrorResponse(String requestedHost, Throwable e) throws WASException {
        Throwable throwable = e;
        do {
            if(throwable instanceof WASException || throwable == null) {
                break;
            }
            throwable = e.getCause();
        } while(true);
        if(throwable instanceof WASException) {
            WASException we = (WASException)throwable;
            //LoggerFactory.getLogger(requestedHost).warn("Type: "+we.getMessageType()+" Code: "+we.getCode()+" Exception: "+we.toString());
            LoggerFactory.getLogger(requestedHost).error(we.getMessage(), e);
            return buildHttpResponsePage(requestedHost, we.getMessageType(), we.getCode(), we.toString());
            // String remote = (connection != null) ? connection.getRemoteSocketAddress().toString() : "Unknown";
            // String server = (connection != null) ? connection.getLocalSocketAddress().toString() : requestedHost;
            // LoggerFactory.getLogger(requestedHost).info("Error client request "+remote+" --- "+server);
            // LoggerFactory.getLogger(requestedHost).info("Close client request on error "+remote+" --- "+server);
        } else {
            if(throwable == null)
                throwable = e;
            if(throwable instanceof SocketTimeoutException) {
                LoggerFactory.getLogger(requestedHost).error("SocketTimeoutException occurred: "+throwable.getMessage());
            } else {
                LoggerFactory.getLogger(requestedHost).error(e.getMessage(), e);
            }
            return buildHttpResponsePage(requestedHost, MSG_TYPE.HTTP, 503, e.toString());
        }
    }

    /**
     * Create http response page
     * @param requestedHost
     * @param type
     * @param code
     * @param message
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws WASException
     */
    public static String buildHttpResponsePage(String requestedHost, MSG_TYPE type, int code, String message) throws WASException {
        Map<String, Object> map;
        if(type != null) {
            map = Map.of("@code", code, "@type", type.name(), "@message", message);
        } else {
            map = Map.of("@code", 0, "@type", "EXCEPTION", "@message", message);
        }
        return ResourceHelper.getResponsePage(requestedHost, map);
    }        

    /**
     * Build HttpTransfer object
     * @param vHost
     * @param client
     * @throws IOException
     * @throws WASException
     * @throws Exception
     */
    public static HttpTransfer buildHttpTransfer(String vHost, Socket client) throws IOException, WASException {
        return new HttpTransfer(vHost, client);
    } 
}
