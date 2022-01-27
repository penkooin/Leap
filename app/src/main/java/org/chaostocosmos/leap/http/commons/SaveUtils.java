package org.chaostocosmos.leap.http.commons;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Constants;
import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.MSG_TYPE;
import org.chaostocosmos.leap.http.WASException;

import ch.qos.logback.classic.Logger;

/**
 * FileUtils
 * 
 * @author 9ins
 */
public class SaveUtils {

    public static void saveBinary(String host, InputStream requestStream, Path savePath, int flushSize) {
        Logger logger = LoggerFactory.getLogger(host);
        String line = readLine(requestStream);
    }

    /**
     * Save multipart contents
     * @param host
     * @param inputStream
     * @param savePath
     * @param flushSize
     * @param boundary
     * @throws IOException
     */
    public static void saveMultipart(String host, InputStream inputStream, Path savePath, int flushSize, String boundary) throws IOException {
        Logger logger = LoggerFactory.getLogger(host);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
        String line = "";
        line = reader.readLine();
        boolean isLast = false;
        do {
            if(line.trim().startsWith("--"+boundary)) {
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
                    while(!(line=reader.readLine()).startsWith("--"+boundary)) {
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
                            if(line.startsWith("--"+boundary) || line.endsWith(boundary+"--")) {
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
                if(line.trim().endsWith((boundary+"--"))) {
                    isLast = true;
                }
                continue;
            }
            line = reader.readLine().trim();
        } while(!isLast);
    }    

    /**
     * Get boundary Map
     * @param contentDesposition
     * @return
     */
    public static Map<String, String> getBoundaryMap(String contentDesposition) {
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
     * Save multipart to local path
     * @param host
     * @param inputStream
     * @param savePath
     * @param bufferSize
     * @param boundary
     * @throws WASException
     */
    private void saveMultiPart1(String host, InputStream inputStream, Path savePath, int bufferSize, String boundary) throws WASException {            
        if(inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            boundary = "--"+boundary;
            String endCondition = boundary+"--";
            try {                    
                String line = readLine(reader);
                do {
                    if((boundary).endsWith(line)) {
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
                            c = inputStream.read();
                            if(c == 0x0A && n == 0x0D) {
                                if(boundary.equals(line.trim()) || line.trim().equals(endCondition)) {
                                    //System.out.println(line.equals(boundary));
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
    public static String readLine(Reader is) throws IOException {
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
        return new String(baos.toByteArray(), Context.charset()).trim();
    }
}
