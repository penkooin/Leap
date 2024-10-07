package org.chaostocosmos.leap.http;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StreamUtils
 * 
 * @author 9ins
 */
public class HttpRequestStream {

    /**
     * InputStream
     */
    InputStream inputStream;

    /**
     * Network buffer size
     */
    int bufferSize;

    /**
     * Constructs with InputStream
     * @param inputStream
     */
    public HttpRequestStream(InputStream inputStream, int bufferSize, Charset charset) {
        this.inputStream = inputStream;
        this.bufferSize = bufferSize;
    }

    /**
     * Read request header lines
     * @param charset
     * @return
     * @throws IOException
     */
    public List<String> readHeaderLines(Charset charset) throws IOException {
        byte[] header = readHeader(charset);
        String headerString = new String(header, charset).trim();
        String[] headerLines = headerString.split(System.lineSeparator());
        return Stream.of(headerLines).map(l -> l.trim()).collect(Collectors.toList());
    }

    /**
     * Read request header bytes
     * @param charset
     * @return
     * @throws IOException
     */
    public byte[] readHeader(Charset charset) throws IOException {        
        try(ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int read;
            while((read = this.inputStream.read()) != -1) {
                output.write(read);
                if(findSequenceIndex(output.toByteArray(), "\r\n\r\n".getBytes()) != -1) {                    
                    break;
                }
            }    
            //System.out.println(new String(output.toByteArray()));
            return output.toByteArray();
        }
    }

    /**
     * Read Multipart request
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
     */
    public Map<String, byte[]> readPartData(String boundary, Charset charset, int bufferSize) throws IOException {
        String boundaryStart = "--"+boundary;
        String boundaryEnd = "--"+boundary+"--";
        Map<String, byte[]> partMap = new HashMap<>();        
        byte[] data;
        try( ByteArrayOutputStream byteStream = new ByteArrayOutputStream() ) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {            
                byteStream.write(buffer, 0, bytesRead);
                if(findSequenceIndex(byteStream.toByteArray(), boundaryEnd.getBytes()) != -1) {
                    break;
                }
            }    
            data = byteStream.toByteArray();
        }
        List<byte[]> parts = splitBySequence(data, boundaryStart.getBytes());
        for(byte[] part : parts) {
            int idx = findSequenceIndex(part, "\r\n\r\n".getBytes()) + "\r\n\r\n".getBytes().length;
            String head = new String(Arrays.copyOf(part, idx));
            byte[] partData = Arrays.copyOfRange(part, idx, part.length);
            Map<String, String> fieldMap = extractFields(head, List.of("name", "filename"));
            if(fieldMap.containsKey("name") && fieldMap.containsKey("filename")) {                
                partMap.put(fieldMap.get("filename"), partData);
            } else if(fieldMap.containsKey("name")) {
                partMap.put(fieldMap.get("name"), partData);
            }
        }
        return partMap;
    }

    /**
     * Find index of bytes sequence
     * @param fileBytes
     * @param sequence
     * @return
     */
    public List<byte[]> splitBySequence(byte[] fileBytes, byte[] sequence) {
        List<byte[]> parts = new ArrayList<>();
        for (int i = 0; i <= fileBytes.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(fileBytes, i, i + sequence.length), sequence)) {
                if(i != 0) {
                    byte[] partData = Arrays.copyOfRange(fileBytes, 0, i);
                    parts.add(partData);                
                    fileBytes = Arrays.copyOfRange(fileBytes, i, fileBytes.length);    
                }
            }
        }
        return parts;
    }

    /**
     * Find sequence index
     * @param fileBytes
     * @param sequence
     * @return
     */
    public int findSequenceIndex(byte[] fileBytes, byte[] sequence) {
        for (int i = 0; i <= fileBytes.length - sequence.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(fileBytes, i, i + sequence.length), sequence)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Extract value matching with keys
     * @param header
     * @param keys
     * @return
     */
    public Map<String, String> extractFields(String header, List<String> keys) {
        Pattern EXTRACT_PATTERN = Pattern.compile("("+keys.stream().collect(Collectors.joining("|"))+")=\"([^\"]+)\"");
        Matcher matcher = EXTRACT_PATTERN.matcher(header);    
        Map<String, String> map = new HashMap<>();
        while(matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    /**
     * Read stream amount of length
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] readStream(long length) throws IOException {
        try(ByteArrayOutputStream data = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[this.bufferSize];
            int total = 0;
            int len;
            while((len = this.inputStream.read(buffer)) != -1 || total < length) {
                data.write(buffer, 0, len);
                total += len;
            }
            return data.toByteArray();
        }
    }

    /**
     * Extract boundary from content type
     * @param contentType
     * @return
     */
    public String extractBoundary(String contentType) {
        String boundary = null;
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring("boundary=".length());
            }
        }
        return boundary;
    }

    /**
     * Get boundary Map
     * @param contentDesposition
     * @return
     */
    public Map<String, String> getBoundaryMap(String contentDesposition) {
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
     * Save byte data 
     * @param data
     * @param savePath
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveBinary(byte[] data, Path savePath) throws FileNotFoundException, IOException {
        try(FileOutputStream target = new FileOutputStream(savePath.toFile())) {
            target.write(data);
        }
    }

    /**
     * Save string data
     * @param str
     * @param savePath
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveString(String str, Path savePath) throws FileNotFoundException, IOException {
        try(FileOutputStream target = new FileOutputStream(savePath.toFile())) {
            target.write(str.getBytes());
        }
    }
    
    /**
     * Save binary body
     * @param length
     * @param savePath
     * @throws IOException
     */
    public void saveTo(int length, Path savePath) throws IOException {
        try(FileOutputStream target = new FileOutputStream(savePath.toFile())) {
            int read;
            while((read=inputStream.read()) != -1) {
                target.write(read);
            }        
        }
    }

    /**
     * Get Hex string
     * @param str
     * @return
     */
    public static String getHexString(String str) {
        byte[] bytes = str.getBytes();
        // Print each byte in hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            // %02X ensures leading zero if the byte is less than 16
            hexString.append(String.format("%02X ", b));
        }         
        return hexString.toString();
    }

    /**
     * Read length bytes
     * @param length
     * @return
     * @throws IOException
    public byte[] readLength(int length) throws IOException {
        if(length == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        while((read=inputStream.read()) != -1) {
            buffer.write(read);
            if(buffer.size() >= length) {
                break;
            }
        }    
        return buffer.toByteArray();
    }
     */

    /**
     * Read line from stream
     * @param charset
     * @return
     * @throws IOException
    public String readRequestLine(Charset charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c =  -1;
        while ((c = this.inputStream.read()) != -1 && c != '\r' && c != '\n') {
            char character = (char) c;
            sb.append(character);
        };        
        String line = sb.toString().trim();
        return line;
    }
     */

    /**
     * Read lines
     * @param charset
     * @return
     * @throws IOException
    public List<String> readLines(Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while((line=readLine(charset)) != null) {
            lines.add(line);
        }
        return lines;
    }
     */

    /**
     * Read line
     * @param charset
     * @return
     * @throws IOException
    public String readLine(Charset charset) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c = this.inputStream.read();
        do {
            out.write(c);
        } while((c=this.inputStream.read()) != -1 && c != '\n');
        byte[] data = out.toByteArray();
        return data.length < 2 ? null : new String(Arrays.copyOf(data, data.length-1), charset);
    }
     */

    /**
     * Read line from stream
     * @param is
     * @param charset
     * @return
     * @throws IOException
    public String readLine(Reader is, Charset charset) throws IOException {
        int c, n = 0x00;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        do {
            c = is.read();
            //CR
            if(c == 0x0A && n == 0x0D && baos.size() != 0) {
                break;
            }
            baos.write(c);
            n = c;
        } while(c != -1 || c == 0x1A);
        return new String(baos.toByteArray(), charset).trim();
    }
     */

    /**
     * Read request lines
     * @param charset
     * @return
     * @throws IOException
    public List<String> readHeaders(Charset charset) throws IOException {
        StringBuffer allLines = new StringBuffer();
        int c = -1;
        while((c = this.inputStream.read()) != -1) {
            allLines.append((char) c);
            if(allLines.indexOf("\r\n\r\n") != -1) {
                break;
            }
        }        
        return Arrays.asList(allLines.toString().split("\n")).stream().filter(l -> !l.trim().equals("")).collect(Collectors.toList());
    }
     */

    /**
     * Get multipart contents
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
    public Map<String, byte[]> getMultiPartContents(String boundary, Charset charset) throws IOException {                
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        //HttpParser.printRequest(reader);
        Map<String, byte[]> multiPartMap = new HashMap<>();
        String boundaryStart = "--"+boundary;
        String boundaryEnd = "--"+boundaryStart+"--";
        String line;
        while((line = reader.readLine()) != null) {
            System.out.println(line);
            if(line != null && line.trim().startsWith(boundaryStart)) {
                String contentDisposition = readLine(reader, charset); 
                if(contentDisposition.startsWith("content-Desposition")) {

                }
                String contentType = readLine(reader, charset);
                contentType = contentType.substring(contentType.indexOf(":")+1).trim();
                //Read empty line
                readLine(reader, StandardCharsets.UTF_8);
                Map<String, String> map = getBoundaryMap(contentDisposition);
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
                if(line.trim().startsWith(boundaryEnd)) {
                    break;
                }
                byte[] dataBytes = data.toByteArray();
                multiPartMap.put(map.get("name"), Arrays.copyOfRange(dataBytes, 0, dataBytes.length));
            }
        }
        return multiPartMap;
    }
     */

    /**
     * Save multipart contents
     * 
     * @param savePath
     * @param flushSize
     * @param boundary
     * @param charset
     * @throws IOException
    public List<Path> saveMultiPart(Path savePath, String boundary, Charset charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
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
                        if(baos.size() >= BUFFER_SIZE && BUFFER_SIZE > Constants.MULTIPART_FLUSH_MINIMAL_SIZE) {
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
     */

    /**
     * Save multipart to local path
     * @param savePath
     * @param bufferSize
     * @param boundary
     * @param charset
     * @throws IOException
     * @throws LeapException
    private void saveMultiPart1(Path savePath, String boundary, Charset charset) throws IOException {            
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
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
                    byte[] buffer = new byte[BUFFER_SIZE];
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
     */
}

