package org.chaostocosmos.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import org.chaostocosmos.http.HttpParserFactory.RequestParser;
import org.chaostocosmos.http.HttpParserFactory.ResponseParser;
import org.chaostocosmos.http.servlet.ServletInvoker;
import org.chaostocosmos.http.servlet.SimpleServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request processor object
 * 
 * @author 9ins modified from cybaek
 * @since 2021.09.16
 */
public class HttpRequestHandler implements Runnable {
    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    /**
     * context
     */
    private final Context context = Context.getInstance();

    /**
     * vhost manager
     */
    private final VirtualHostManager virtualHostManager = VirtualHostManager.getInstance();

    /**
     * HttpServer
     */
    private final HttpServer httpServer;

    /**
     * doc root
     */
    private File rootDirectory;

    /**
     * welcome index html
     */
    private String indexFileName = context.getWelcomFilename();

    /**
     * Client socket
     */
    private Socket connection;

    /**
     * Constructor with root direcotry, index.html file, client socket
     * 
     * @param httpServer
     * @param rootDirectory
     * @param indexFileName
     * @param connection
     */
    public HttpRequestHandler(HttpServer httpServer, File rootDirectory, String indexFileName, Socket connection) {
        this.httpServer = httpServer;
        if (rootDirectory.isFile()) {
            throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
        }
        try {
            rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException ex) {
        }
        this.rootDirectory = rootDirectory;
        if (indexFileName != null)
            this.indexFileName = indexFileName;
        this.connection = connection;
    }

    @Override //127.0.0.1:8080
    public void run() {
        try {
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), context.getServerCharset());

            ////////////////////////////// Parse from Reader to descriptor object
            RequestParser requestParser = HttpParserFactory.getRequestParser();
            HttpRequestDescriptor request = requestParser.parseRequest(in);
            if(request == null) {
                return;
            }
            // log request information
            logger.info(request.getRequestLines());
            //request.printURLInfo();
            // Put client address to request header Map for ip filter
            request.getReqHeader().put("@Client", connection.getInetAddress().getHostName());

            ////////////////////////////// Create dummy response descriptor
            ResponseParser responseParser = HttpParserFactory.getResponseParser();
            HttpResponseDescriptor response = responseParser.createDummyHttpResponseDescriptor();
            response.setHttpResponse(HttpBuilder.buildDummyHttpResponse());
            Path resourcePath = ResourceHelper.getResourcePath(request);

            ////////////////////////////// Servlet process
            String contextPath = request.getContextPath();            
            SimpleServlet servlet = (SimpleServlet) this.httpServer.getServletLoader().getMatchServlet(request.getContextPath());
            int resCode = -1;
            String mimeType = "text/html";
            String body = null;

            // if client request servlet path
            if (servlet != null) {
                // request method validation
                if (this.httpServer.getServletLoader().vaildateRequestMethod(request.getRequestType(), request.getContextPath())) {
                    Method invokingMethod = this.httpServer.getServletLoader().getMatchMethod(request.getContextPath());
                    ServletInvoker.invokeService(servlet, invokingMethod, request, response);
                    resCode = 200;
                    body = response.getBody();                    
                } else {
                    resCode = 405;
                    body = ResourceHelper.getResourceContents(context.getResponseResource(resCode));
                }
            } else { // When client request static resources
                if(request.getContextPath().equals("/")) {
                    resourcePath = ResourceHelper.getResourcePath(request.getUrl().getHost(), request.getContextPath()+context.getWelcomFilename());
                    body = ResourceHelper.getResourceContents(resourcePath);
                    resCode = 200;
                } else {
                    if (resourcePath.toFile().exists()) {
                        resCode = 200;
                        mimeType = Files.probeContentType(resourcePath);

                        //Implementation spec #4
                        if(resourcePath.toFile().getName().endsWith(".exe")) {
                            resCode = 403;
                            body = ResourceHelper.getResourceContents(context.getResponseResource(resCode));
                        } else if(mimeType == null || mimeType.equals("application/x-msdownload")) {
                            byte[] rawData = ResourceHelper.getBinaryResource(resourcePath);
                            sendRaw(raw, resCode, rawData, mimeType);
                            return;
                        } else {
                            body = ResourceHelper.getResourceContents(resourcePath);
                        }
                        response.setContentType(mimeType);
                    } else {
                        resCode = 404;
                        body = ResourceHelper.getResourceContents(context.getResponseResource(resCode));
                    }
                }
                System.out.println(resourcePath.toString()+"  &&&&&&&&&&&&&&&&&&&&&&&&&&&");                    
            }
            //System.out.println("//////////////////////////////////////////////////////////////");
            response.setResponseCode(resCode);
            response.setBody(body);

            ////////////////////////////// Send response to client
            sendResponse(out, response);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Send response to client
     * @param out
     * @param desc
     * @throws IOException
     */
    public void sendResponse(Writer out, HttpResponseDescriptor desc) throws IOException {
         String res = context.getHttpVersion()+" "+desc.getResponseCode()+" "+context.getHttpMsg(desc.getResponseCode())+"\r\n"; 
         logger.info("RESPONSE: "+res);
         //logger.debug(desc.toString());
         out.write(res); 
         Map<String, Object> header = desc.getHeader(); 
         header.put("Date: ", new Date()); 
         header.put("Server: ", "WAS 0.9"); 
         header.put("Content-Type: ", desc.getContentType());
         header.put("Content-length: ", desc.getBody().length());
         for(Map.Entry<String, Object> e : header.entrySet()) {
            out.write(e.getKey()+": "+e.getValue()+"\r\n"); 
        } 
        out.write("\r\n");
        out.flush(); 
        out.write(desc.getBody()); 
        out.flush();
    }

    /**
     * Send raw data to client
     * @param out
     * @param resCode
     * @param rawData
     * @param contentType
     * @throws IOException
     */
    private void sendRaw(OutputStream out, int resCode, byte[] rawData, String contentType) throws IOException {
        out.write((context.getHttpVersion()+" "+resCode+"\r\n").getBytes()); 
        out.write(("Content-Type: "+contentType+"\r\n").getBytes());
        out.write(("Content-length: "+rawData.length+"\r\n").getBytes());
        out.write("\r\n".getBytes());
        out.flush(); 
        out.write(rawData); 
        out.flush();
   }    
}