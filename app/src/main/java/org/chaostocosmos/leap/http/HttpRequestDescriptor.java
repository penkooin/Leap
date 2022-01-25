package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Http request descriptor
 * 
 * @author 9ins
 * @since 2021.09.18
 */
public class HttpRequestDescriptor {
    /**
     * Multi part descriptor
     */
    public static class MultipartDescriptor {

        Logger logger;
        String host;
        List<Path> filePaths;
        MIME_TYPE contentType;
        String boundary;
        long contentLength;
        InputStream requestStream;

        /**
         * Constructor of multipart
         * @param host
         * @param contentType
         * @param boundary
         * @param contentLength
         * @param requestStream
         */
        public MultipartDescriptor(String host, MIME_TYPE contentType, String boundary, long contentLength, InputStream requestStream) {
            this.host = host;
            this.contentType = contentType;
            this.boundary = boundary;
            this.contentLength = contentLength;
            this.requestStream = requestStream;
            this.filePaths = new ArrayList<>();
            this.logger = LoggerFactory.getLogger(this.host);
        }

        public String getHost() {
            return this.host;
        }

        public List<Path> getFilePaths() {
            return this.filePaths;
        }

        public MIME_TYPE contentType() {
            return this.contentType;
        }

        public String getBoundary() {
            return this.boundary;
        }

        public long getContentLength() {
            return this.contentLength;
        }

        public InputStream getRequestInputStream() {
            return this.requestStream;
        }

        public void save(Path savePath, int flushSize) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.requestStream, StandardCharsets.ISO_8859_1));
            String line = "";
            line = reader.readLine();
            boolean isLast = false;
            do {
                if(line.trim().startsWith("--"+this.boundary)) {
                    String contentDesposition = readLine(reader);
                    String contentType =  reader.readLine();
                    contentType = contentType.substring(contentType.indexOf(":")+1).trim();
                    String emptyLine = reader.readLine();
                    Map<String, String> map = getBoundaryMap(contentDesposition);
                    logger.debug("============================== MULTIPART CONTENT: {} ==============================", contentType);
                    map.entrySet().stream().forEach(e -> logger.debug(e.getKey()+" = "+e.getValue()));
                    File file = new File(map.get("filename"));
                    long startMillis = System.currentTimeMillis();                    
                    if(contentType.startsWith("text")) {
                        FileWriter writer = new FileWriter(file);
                        while(!(line=reader.readLine()).startsWith("--"+this.boundary)) {
                            writer.write(line+System.lineSeparator());
                        }
                        writer.close();
                    } else {
                        FileOutputStream out = new FileOutputStream(file);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int c, d = 0x00;
                        int len = 0;
                        while((c = reader.read()) != -1) {
                            baos.write(c);
                            if(c == 0x0A && d == 0x0D) {
                                byte[] lineData = baos.toByteArray();
                                line = new String(lineData);
                                if(line.startsWith("--"+this.boundary) || line.endsWith(this.boundary+"--")) {
                                    break;
                                }
                                len += lineData.length;
                                out.write(lineData);
                                baos.reset();
                            }
                            if(baos.size() >= flushSize && flushSize > Constants.MULTIPART_FLUSH_MINIMAL_SIZE) {
                                byte[] data = baos.toByteArray();
                                if(data[data.length-1] != 0x0A && data[data.length-2] != 0x0D) {
                                    out.write(data);
                                    baos.reset();
                                }
                            }
                            d = c;
                        };
                        out.close();
                    }
                    float elapseSec = Math.round(((System.currentTimeMillis() - startMillis) / 1000f) * 100f) / 100f ;
                    logger.debug("Save uploaded file - filename: {}, size: {}, content-type: {}, upload elpase seconds: {}", file.getName(), file.length(), contentType, elapseSec);
                    if(line.trim().endsWith((this.boundary+"--"))) {
                        isLast = true;
                    }
                    continue;
                }
                line = reader.readLine().trim();
            } while(!isLast);
        }

        private Map<String, String> getBoundaryMap(String contentDesposition) {
            contentDesposition = contentDesposition.substring(contentDesposition.indexOf(":")+1).trim();
            //System.out.println(contentDesposition);
            String[] splited = contentDesposition.split(";");
            Map<String, String> map = Arrays.asList(splited)
                                            .stream()
                                            .map(t -> t.trim())
                                            .map(t -> t.indexOf("=") == -1 ? new String[]{t, ""} : new String[]{t.split("=")[0], t.split("=")[1].replace("\"", "")})
                                            .collect(Collectors.toMap(k -> k[0].toString(), v -> v[1].toString()));
            return map;
        }

        /**
         * Save to local path
         * @param savePath
         * @param bufferSize
         * @throws WASException
         */
        public void saveTo(Path savePath, int bufferSize) throws WASException {            
            if(this.requestStream != null) {
                InputStreamReader reader = new InputStreamReader(this.requestStream);
                this.boundary = "--"+this.boundary;
                String endCondition = this.boundary+"--";
                int endConditionLen = endCondition.getBytes().length;
                try {                    
                    String line = readLine(reader);
                    do {
                        if((this.boundary).endsWith(line)) {
                            String contentDesposition = readLine(reader);
                            String contentType =  readLine(reader);
                            String empty = readLine(reader);
                            int idx = contentDesposition.indexOf("filename");
                            String filename = null;
                            if(idx != -1) {
                                filename = contentDesposition.substring(idx);
                                filename = filename.substring(filename.indexOf("\"")+1);
                                filename = filename.substring(0, filename.indexOf("\""));
                            }
                            FileOutputStream fos = new FileOutputStream(savePath.resolve(filename).toFile());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
                            byte[] buffer = new byte[bufferSize];
                            int c, n = 0x00;
                            line = "";
                            do {
                                c = this.requestStream.read();
                                if(c == 0x0A && n == 0x0D) {
                                    if(this.boundary.equals(line.trim()) || line.trim().equals(endCondition)) {
                                        //System.out.println(line.equals(this.boundary));
                                        line = line.trim();
                                        break;
                                    }
                                    fos.write(baos.toByteArray());
                                    baos.reset();
                                    line = "";
                                } else {
                                    line += (char)c;
                                }
                                baos.write(c);
                                n = c;      
                                /*
                                if(baos.size() == bufferSize)  {
                                    byte[] bytes = baos.toByteArray();
                                    System.arraycopy(bytes, 0, buffer, 0, bufferSize-endConditionLen);
                                    fos.write(bytes);
                                    byte[] lastBytes = new byte[endConditionLen];
                                    System.arraycopy(bytes, bufferSize-endConditionLen, lastBytes, 0, lastBytes.length);
                                    baos.reset();
                                    baos.write(lastBytes);
                                }
                                */
                            } while(c != -1);
                            fos.close();
                            //System.out.println("############ saving to file");
                            if(line.trim().equals(endCondition)) {
                                //System.out.println(line+"     "+endCondition);
                                break;
                            }
                        }                        
                        System.out.println("0."+line.trim());
                    } while(true);
                } catch(IOException e) {
                    throw new WASException(MSG_TYPE.ERROR, 43, savePath.toString());
                }
            }
        }
        
        /**
         * Read line from stream
         * @param is
         * @return
         * @throws IOException
         */
        private String readLine(Reader is) throws IOException {
            int c, n = 0x00;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            do {
                c = is.read();
                //CR
                if(c == 0x0A && n == 0x0D) {
                    break;
                }
                baos.write(c);
                n = c;
            } while(c != -1);
            return new String(baos.toByteArray(), "utf-8").trim();
        }
    }

    private HttpRequest httpRequest;
    private String httpVersion;
    private REQUEST_TYPE requestType;
    private String requestedHost;
    private Map<String, String> reqHeader;
    private String contentType;
    private byte[] reqBody; 
    private String contextPath;
    private Map<String, String> contextParam;
    private MultipartDescriptor multipart;

    /**
     * Constructor
     * @param httpVersion
     * @param requestType
     * @param requestHost
     * @param reqHeader
     * @param reqBody
     * @param contextPath
     * @param url
     * @param contextParam
     * @param multipartList
     */
    public HttpRequestDescriptor(
                                String httpVersion, 
                                REQUEST_TYPE requestType, 
                                String requestHost,
                                Map<String,String> reqHeader, 
                                String contentType,
                                byte[] reqBody, 
                                String contextPath, 
                                Map<String,String> contextParam,
                                MultipartDescriptor multipart) {
        this.httpVersion = httpVersion;
        this.requestType = requestType;
        this.requestedHost = requestHost;
        this.reqHeader = reqHeader;
        this.contentType = contentType;
        this.reqBody = reqBody;
        this.contextPath = contextPath;
        this.contextParam = contextParam;
        this.multipart = multipart;
    }

    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public REQUEST_TYPE getRequestType() {
        return this.requestType;
    }

    public String getRequestedHost() {
        return this.requestedHost;
    }

    public void setRequestedHost(String requestedHost) {
        this.requestedHost = requestedHost;
    }

    public Map<String,String> getReqHeader() {
        return this.reqHeader;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getReqBody() {
        return this.reqBody;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public Map<String, String> getContextParam() {
        return this.contextParam;
    }

    public MultipartDescriptor getMultipartDescriptor() {
        return this.multipart;
    }

    public void printURLInfo() throws URISyntaxException, MalformedURLException {
        Logger logger = (Logger)LoggerFactory.getLogger(this.requestedHost);
        String url = !this.requestedHost.startsWith("http") ? "http://"+this.requestedHost : requestedHost;
        URI uri = new URL(url).toURI();
        logger.debug("getHost :"+uri.getHost());
        logger.debug("getPort :"+uri.getPort());
        logger.debug("getQuery :"+uri.getQuery());
        logger.debug("getPath :"+uri.getPath());
        logger.debug("getRawPath :"+uri.getRawPath());
        logger.debug("getRawQuery :"+uri.getRawQuery());
        logger.debug("getFragment :"+uri.getFragment());
        logger.debug("getScheme :"+uri.getScheme());
        logger.debug("getRawAuthority :"+uri.getRawAuthority());
        logger.debug("getRawUserInfo :"+uri.getRawUserInfo());
        logger.debug("getAuthority :"+uri.getAuthority());
    }

    @Override
    public String toString() {
        return "{" +
            " httpRequest='" + getHttpRequest() + "'" +
            ", httpVersion='" + getHttpVersion() + "'" +
            ", requestType='" + getRequestType() + "'" +
            ", requestedHost='" + getRequestedHost() + "'" +
            ", reqHeader='" + getReqHeader() + "'" +
            ", contentType='" + getContentType() + "'" +
            ", reqBody='" + getReqBody() + "'" +
            ", contextPath='" + getContextPath() + "'" +
            ", contextParam='" + getContextParam() + "'" +
            ", multipartList='" + getMultipartDescriptor() + "'" +
            "}";
    }
}
