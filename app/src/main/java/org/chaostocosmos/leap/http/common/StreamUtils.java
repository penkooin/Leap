package org.chaostocosmos.leap.http.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.common.LoggerFactory;

/**
 * StreamUtils
 * 
 * @author 9ins
 */
public class StreamUtils {
    /**
     * Save byte data 
     * @param host
     * @param data
     * @param savePath
     * @param flushSize
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveBinary(byte[] data, Path savePath, int flushSize) throws FileNotFoundException, IOException {
        try(FileOutputStream target = new FileOutputStream(savePath.toFile())) {
            target.write(data);
        }
    }

    /**
     * Save binary body
     * @param requestStream
     * @param contentLength
     * @param savePath
     * @param flushSize
     * @throws IOException
     */
    public static void saveBinary(InputStream requestStream, long contentLength, Path savePath, int flushSize) throws IOException {
        //String line = readLine(requestStream, StandardCharsets.ISO_8859_1);
        try(FileOutputStream target = new FileOutputStream(savePath.toFile())) {
            byte[] buffer = new byte[flushSize];
            int len;
            long total = 0;
            while((len=requestStream.read(buffer)) > 0) {
                target.write(buffer, 0, len);
                total += len;
                if(total >= contentLength) 
                    break;
            }    
        }
    }

    /**
     * Save text
     * @param host
     * @param requestStream
     * @param contentLength
     * @param savePath
     * @param charset
     * @throws IOException
     */
    public static void saveStream(InputStream requestStream, long contentLength, Path savePath, Charset charset) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(savePath.toFile())) {
            int total =0;
            int read;
            while((read = requestStream.read()) != -1) {
                fos.write(read);
                total++;
                if(total >= contentLength) {
                    break;
                }                
            }    
        }
    }

    /**
     * Get multipart contents
     * @param host
     * @param is
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
     */
    public static Map<String, byte[]> getMultiPartContents(InputStream inputStream, String boundary, Charset charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line = reader.readLine();
        Map<String, byte[]> multiPartMap = new HashMap<>();
        String boundaryStart = "--"+boundary;
        String boundaryEnd = boundaryStart+"--";
        boolean isLast = false;
        do {
            if(line.trim().startsWith(boundaryStart)) {
                String contentDesposition = readLine(reader, charset);
                String contentType = readLine(reader, charset);
                contentType = contentType.substring(contentType.indexOf(":")+1).trim();
                //Read empty line
                readLine(reader, StandardCharsets.UTF_8);
                Map<String, String> map = getBoundaryMap(contentDesposition);
                LoggerFactory.getLogger().debug("============================== MULTIPART CONTENT: {} ==============================", contentType);
                map.entrySet().stream().forEach(e -> LoggerFactory.getLogger().debug(e.getKey()+" = "+e.getValue()));
                long startMillis = System.currentTimeMillis();
                ByteArrayOutputStream lineStream = new ByteArrayOutputStream();
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                int c, d = 0x00;
                int len = 0;
                while((c = reader.read()) != -1) {
                    lineStream.write(c);
                    if(c == 0x0A && d == 0x0D) {
                        byte[] lineData = lineStream.toByteArray();
                        line = new String(lineData);
                        if(line.trim().equals(boundaryStart) || line.trim().equals(boundaryEnd)) {
                            //System.out.println(line.trim());
                            len -= 2;
                            break;
                        }
                        len += lineData.length;
                        data.writeBytes(lineData);
                        lineStream.reset();
                    }
                    d = c;
                }
                float elapseSec = Math.round(((System.currentTimeMillis() - startMillis) / 1000f) * 100f) / 100f;
                LoggerFactory.getLogger().debug("Uploaded - name: {}, filename: {}, size: {}, content-type: {}, upload elpase seconds: {}", map.get("name"), map.get("filename"), len, contentType, elapseSec);
                if(line.trim().endsWith(boundaryEnd)) {
                    isLast = true;
                }
                byte[] dataBytes = data.toByteArray();
                multiPartMap.put(map.get("name"), Arrays.copyOfRange(dataBytes, 0, dataBytes.length-2));
            }
        } while(!isLast);
        return multiPartMap;
    }

    /**
     * Save multipart contents
     * 
     * @param inputStream
     * @param savePath
     * @param flushSize
     * @param boundary
     * @param charset
     * @throws IOException
     */
    public static List<Path> saveMultiPart(InputStream inputStream, Path savePath, int flushSize, String boundary, Charset charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line = reader.readLine();
        boolean isLast = false;
        List<Path> savedFiles = new ArrayList<>();
        String boundaryStart = "--"+boundary;
        String boundaryEnd = boundaryStart+"--";
        if(!savePath.toFile().exists()) {
            Files.createDirectories(savePath);
        }
        do {
            //System.out.println(line);
            if(line.trim().equals(boundaryStart)) {
                String contentDesposition = readLine(reader, charset);
                String contentType =  readLine(reader, charset);
                contentType = contentType.substring(contentType.indexOf(":")+1).trim();
                //Read empty line
                reader.readLine();
                Map<String, String> map = getBoundaryMap(contentDesposition);
                LoggerFactory.getLogger().debug("============================== MULTIPART CONTENT: {} ==============================", contentType);
                map.entrySet().stream().forEach(e -> LoggerFactory.getLogger().debug(e.getKey()+" = "+e.getValue()));
                File file = savePath.resolve(map.get("filename")).toFile();
                long startMillis = System.currentTimeMillis();
                if(contentType.indexOf("text") != -1) {
                    FileWriter writer = new FileWriter(file);
                    while((line=reader.readLine()) != null) {
                        if(line.equals(boundaryStart) || line.equals(boundaryEnd)) {
                            break;
                        }
                        writer.write(line+System.lineSeparator());
                    }
                    writer.close();
                } else {
                    FileOutputStream out = new FileOutputStream(file);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int c, d = 0x00;
                    //int len = 0;
                    while((c = reader.read()) != -1) {
                        baos.write(c);
                        if(c == 0x0A && d == 0x0D) {
                            byte[] lineData = baos.toByteArray();
                            line = new String(lineData);
                            if(line.trim().equals(boundaryStart) || line.trim().equals(boundaryEnd)) {
                                //System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&"+line);
                                break;
                            }
                            //len += lineData.length;
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
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(file.length()-2);
                raf.close();
                savedFiles.add(file.toPath());
                float elapseSec = Math.round(((System.currentTimeMillis() - startMillis) / 1000f) * 100f) / 100f ;
                LoggerFactory.getLogger().debug("Save uploaded file - filename: {}, size: {}, content-type: {}, upload elpase seconds: {}", file.getName(), file.length(), contentType, elapseSec);
                if(line.trim().equals(boundaryEnd)) {
                    //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^ last");
                    isLast = true;
                    break;
                }
                //System.out.println("$$$$$$$$$$$$$$$$$"+line);
            }
        } while(!isLast);
        return savedFiles;
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
     * @throws IOException
     * @throws LeapException
     */
    private void saveMultiPart1(String host, InputStream inputStream, Path savePath, int bufferSize, String boundary, Charset charset) throws IOException {            
        if(inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            boundary = "--"+boundary;
            String endCondition = boundary+"--";
            String line = readLine(reader, StandardCharsets.UTF_8);
            do {
                if((boundary).endsWith(line)) {
                    String contentDesposition = readLine(reader, charset);
                    String contentType =  readLine(reader, charset);
                    String empty = readLine(reader, charset);
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
                //System.out.println("0."+line.trim());
            } while(true);
        }
    }

    /**
     * Read line from stream
     * @param is
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readLine(Reader is, Charset charset) throws IOException {
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
        } while(c != -1 || c == 0x1A);
        return new String(baos.toByteArray(), charset).trim();
    }

    /**
     * Read line from stream
     * @param is
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readLine(InputStream is, Charset charset) throws IOException {
        int c;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while((c = is.read()) > 0 || baos.size() < 1) {
            //LF : \n
            if(c == 0x0A) {
                break;
            }
            baos.write(c);
        };
        byte[] data = baos.toByteArray();
        String line = new String(Arrays.copyOfRange(data, 0, data.length - 1), charset);
        return line.equals("") ? null : line;
    }

    /**
     * Read request lines
     * @param is
     * @return
     * @throws IOException
     */
    public static List<String> readHeaders(InputStream is) throws IOException {
        String allLines = "";
        String line;
        while((line=readLine(is, StandardCharsets.UTF_8)) != null) {            
            allLines += line + System.lineSeparator();            
        }
        return Arrays.asList(allLines.split(System.lineSeparator())).stream().collect(Collectors.toList());
    }

    /**
     * Read data from InputStream as much as the length
     * @param is
     * @param length
     * @return
     * @throws IOException
     */
    public static byte[] readLength(InputStream is, int length) throws IOException {
        byte[] data = new byte[length];
        is.read(data, 0, length);
        return data;
    }
}
