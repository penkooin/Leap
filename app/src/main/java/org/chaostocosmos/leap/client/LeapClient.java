package org.chaostocosmos.leap.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LeapClient
 * 
 * @author 9ins
 */
public class LeapClient {

    public static final String PROTOCOL = "HTTP/1.1";
    private Socket socket = null;
    private String host = null;
    private int port = -1;
    private boolean keepAlive = true;
    private boolean tcpNoDelay = true;
    private int receiveBufferSize = 2048;
    private int sendBufferSize = 2048;
    private int timeout = 30 * 1000;
    private int responseCode = -1;
    private byte[] responseBody = null;
    private Charset charset = Charset.forName("UTF-8");
    private Map<String, Object> requestHeaders = null;
    private Map<String, List<String>> responseHeaders = null;
    private String responseMsg = null;
    private File saveResponseFile = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private static LeapClient client;

    /**
     * Default constructor
     */
    private LeapClient() {
        this.requestHeaders = new HashMap<>();
        this.requestHeaders.put("User-Agent", "LeapClient/1.0");
        this.requestHeaders.put("Accept", "*/*");
        if(this.keepAlive) {
            this.requestHeaders.put("Connection", "keep-alive");
        }
    }
    /**
     * Constructs with host, port
     * @param host
     * @param port
     * @throws UnknownHostException
     * @throws IOException
     */
    private LeapClient(String host, int port) throws UnknownHostException, IOException {
        this();
        this.host = host;
        this.port = port;
        this.requestHeaders.put("Host", host+":"+port);
    }
    /**
     * Build client
     * @return
     */
    public static LeapClient build() {
        if(client == null) {
            client = new LeapClient();
        }
        return client;
    }    
    /**
     * Build client with host, port
     * @param host
     * @param port
     * @return
     * @throws UnknownHostException
     * @throws IOException
     */
    public static LeapClient build(String host, int port) throws UnknownHostException, IOException {
        if(client == null) {
            client = new LeapClient(host, port);
        }
        return client;
    }
    /**
     * Add request header key / value
     * @param key
     * @param value
     * @return
     */
    public LeapClient addHeader(String key, Object value) {
        this.requestHeaders.put(key, value);
        return client;
    }
    /**
     * Set charset
     * @param charset
     * @return
     */
    public LeapClient charset(Charset charset) {
        this.charset = charset;
        return client;
    }
    /**
     * Set keep alive
     * @param keepAlive
     * @return
     */
    public LeapClient keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return client;
    }
    /**
     * Set connection timeout
     * @param timeout
     * @return
     */
    public LeapClient timeout(int timeout) {
        this.timeout = timeout;
        return client;
    }
    /**
     * Set tcp no delay
     * @param tcpNoDelay
     * @return
     */
    public LeapClient tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return client;
    }    
    /**
     * Set receive buffer size
     * @param receiveBufferSize
     * @return
     */
    public LeapClient receiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return client;
    }
    /**
     * Set send buffer size
     * @param sendBufferSize
     * @return
     */
    public LeapClient sendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return client;
    }
    /**
     * Set saving response File
     */
    public LeapClient save(File saveResponseFile) {
        this.saveResponseFile = saveResponseFile;
        return client;
    }
    /**
     * Get response code
     * @return
     */
    public int getResponseCode() {
        return this.responseCode;
    }
    /**
     * Get response message
     * @return
     */
    public String getResponseMsg(){
        return this.responseMsg;
    }
    /**
     * Connection to server
     * @return
     * @throws IOException
     */
    private Socket connect() throws IOException {
        if(this.host == null) {
            throw new IllegalArgumentException("Host name is not set yet!!!");
        }
        this.socket = new Socket(this.host, this.port); 
        this.socket.setKeepAlive(this.keepAlive);
        this.socket.setSoTimeout(this.timeout);
        this.socket.setTcpNoDelay(this.tcpNoDelay);
        this.socket.setReceiveBufferSize(this.receiveBufferSize);
        this.socket.setSendBufferSize(this.sendBufferSize);                
        this.outputStream = socket.getOutputStream(); 
        this.inputStream = socket.getInputStream();
        return this.socket;
    }
    /**
     * Write context with params
     * @param contextPath
     * @param contextParams
     * @throws IOException
     */
    private void writeContextParams(REQUEST method, String contextPath, Map<String, String> contextParams) throws IOException {
        String requestLine = method.name()+" "+contextPath+""+(contextParams == null ? "" : "?"+contextParams.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), charset)+"="+URLEncoder.encode(e.getValue(), charset)).collect(Collectors.joining("&")))+" HTTP/1.1\r\n";        
        this.outputStream.write(requestLine.getBytes(StandardCharsets.ISO_8859_1));        
        this.outputStream.flush();
    }
    /**
     * Write request headers
     * @param requestHeaders
     * @throws IOException
     */
    private void writeHeaders(Map<String, Object> requestHeaders) throws IOException {
        for(Map.Entry<String, Object> e : requestHeaders.entrySet()) {
            String header = e.getKey()+": "+e.getValue()+"\r\n";
            this.outputStream.write(header.getBytes(StandardCharsets.ISO_8859_1));
        }
        this.outputStream.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
        this.outputStream.flush();
    }
    /**
     * Write Multi part form data
     * @param formData
     * @throws IOException
     */
    private void writeFormData(Map<String, FormData<?>> formData) throws IOException {
        for(Map.Entry<String, FormData<?>> entry : formData.entrySet()) {
            this.outputStream.write("----------------------------LeapClient\r\n".getBytes(StandardCharsets.ISO_8859_1));
            String line = "Content-Disposition: form-data; name=\""+entry.getKey()+"\"";
            if(entry.getValue().getContent() instanceof File) {
                line += "; filename=\""+((File)entry.getValue().getContent()).getName()+"\"";
            } else if(entry.getValue().getContent() instanceof Path) {
                line += "; filename=\""+((Path)entry.getValue().getContent()).toFile().getName()+"\"";
            }
            this.outputStream.write((line+"\r\n").getBytes(StandardCharsets.UTF_8));
            line = "Content-Type: "+entry.getValue().getContentType().mimeType()+"\r\n\r\n";
            this.outputStream.write(line.getBytes(StandardCharsets.ISO_8859_1));
            this.outputStream.write(entry.getValue().getContentBytes());
            this.outputStream.write("\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        }
        this.outputStream.write("----------------------------LeapClient--\r\n".getBytes(StandardCharsets.ISO_8859_1));
        this.outputStream.flush();
    }
    /**
     * Read line
     * @param is
     * @return
     * @throws IOException
     */
    private byte[] readLine(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte c = 0;
        while((c = (byte) is.read()) != -1) {
            if(c == 0x0A) {
                break;
            }
            out.write(c);
        }
        if( c == -1 ) {
            return null;
        }
        byte[] bytes = out.toByteArray();
        return Arrays.copyOfRange(bytes, 0, bytes.length-1);
    }
    /**
     * Process response
     * @param inputStream
     * @return
     * @throws IOException
     */
    private byte[] processResponse(InputStream is) throws IOException {
        byte[] bytes = readLine(is);
        if(bytes == null) {
            return null;
        }
        String line = new String(bytes, StandardCharsets.ISO_8859_1);
        List<String> list = Arrays.asList(line.split(" "));
        this.responseCode = Integer.parseInt(list.get(1).trim());
        this.responseMsg = list.get(2);        
        this.responseHeaders = new HashMap<>();
        int contentLength = 0;
        while(!(line = new String(readLine(is)).trim()).equals("")) {
            String key = line.substring(0, line.indexOf(":")).trim();
            List<String> values = Arrays.asList(line.substring(line.indexOf(":")+1).trim().split(";")).stream().map(t -> t.trim()).collect(Collectors.toList());
            this.responseHeaders.put(key, values); 
            if(key.equals("Content-Length")) {
                contentLength = Integer.parseInt(values.get(0));
            }
        }        
        if(contentLength > 0) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[this.receiveBufferSize];
            int len;
            while((len = is.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            this.responseBody = out.toByteArray();
            List<String> ctxTypeList = (List<String>)this.responseHeaders.get("Content-Type");
            if(ctxTypeList != null) {
                String charset = null;                
                if(ctxTypeList.size() > 1) {
                    charset = ctxTypeList.size() > 0 ? ctxTypeList.get(1).substring(ctxTypeList.get(1).indexOf("=")+1) : this.responseHeaders.get("Charset") != null ? this.responseHeaders.get("Charset").get(0) : "utf-8";
                }
                if(charset != null) {
                    this.responseMsg = new String(this.responseBody, Charset.forName(charset.trim()));
                } else {
                    this.responseMsg = null;
                }    
                if(this.saveResponseFile != null) {
                    if(!this.saveResponseFile.getParentFile().exists()) {
                        this.saveResponseFile.mkdirs();
                    }
                    if(this.responseMsg != null) {
                        Files.writeString(this.saveResponseFile.toPath(), this.responseMsg, Charset.forName(charset), StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        Files.write(this.saveResponseFile.toPath(), this.responseBody, StandardOpenOption.TRUNCATE_EXISTING);
                    }                
                }
            }
        }
        return responseBody;    
    }
    /**
     * Request GET 
     * @param contextPath
     * @param contextParams
     * @param responseContentPath
     * @return
     * @throws IOException
     */
    public synchronized LeapClient get(String contextPath, Map<String, String> contextParams) throws IOException {
        this.requestHeaders.put("Host", this.host+":"+this.port);
        connect();
        writeContextParams(REQUEST.GET, contextPath, contextParams);
        writeHeaders(this.requestHeaders);
        processResponse(this.inputStream);
        this.outputStream.close();
        this.inputStream.close();
        this.socket.close();
        return client;
    }
    /**
     * Request POST
     * @param contextPath
     * @param contextParams
     * @param formDataMap
     * @param responseContentPath
     * @return
     * @throws IOException
     */
    public synchronized LeapClient post(String contextPath, Map<String, String> contextParams, Map<String, FormData<?>> formDataMap) throws IOException {
        connect();
        writeContextParams(REQUEST.POST, contextPath, contextParams);

        long contentLength = formDataMap.values().stream().mapToInt(f -> f.getContentLength()).sum();
        this.requestHeaders.put("Content-Type", "multipart/form-data; boundary=--------------------------LeapClient");
        this.requestHeaders.put("Content-Length", contentLength);

        writeHeaders(this.requestHeaders);
        writeFormData(formDataMap);
        processResponse(this.inputStream);
        this.outputStream.close();
        this.inputStream.close();
        this.socket.close();
        return client;
    }    

    public static void main(String[] args) throws UnknownHostException, IOException {
        Map<String, FormData<?>> map = Map.of("code", new FormData<File>(MIME.APPLICATION_ZIP, Paths.get("./LICENSE").toFile()));
        LeapClient client = LeapClient.build("localhost", 8080).addHeader("charset", "utf-8").post("/monitor/chart/image", null, map);       
        //LeapClient client = LeapClient.build("localhost", 8080).addHeader("charset", "utf-8").get("/", null);
        System.out.println(new String(client.getResponseMsg()));        
    }
}
