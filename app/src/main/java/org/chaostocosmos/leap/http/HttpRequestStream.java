package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.common.file.FileUtils;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.exception.LeapException;

/**
 * StreamUtils
 * 
 * @author 9ins
 */
public class HttpRequestStream {

    /**
     * 2 * CRLF
     */
    private static final String CRLF2 = "\r\n\r\n";

    /**
     * CRLF
     */
    public static final String CRLF = "\r\n";    

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
     * Convert numeric list to bytes
     * @param numericList
     * @return
     */
    public byte[] convertToBytes(List<? extends Number> numericList) {
       byte[] byteArray = new byte[numericList.size()];
       for (int i = 0; i < numericList.size(); i++) {
           byteArray[i] = numericList.get(i).byteValue();
       }
       return byteArray;        
    }

    /**
     * Extract filename from part header
     * @param headers
     * @return
     */
    private String extractFileNameFromHeaders(String headers) {
        Pattern pattern = Pattern.compile("filename=\"(.*?)\"");
        Matcher matcher = pattern.matcher(headers);
        if (matcher.find()) {
            return matcher.group(1); // Return filename from Content-Disposition header
        }
        return null;
    }

    /**
     * Read part data by sing content length
     * @param boundary
     * @param contentLengh
     * @param charset
     * @return
     * @throws IOException
     */
    public Map<String, byte[]> readPartData(String boundary, long contentLengh, Charset charset) throws IOException {
        String boundaryStart = "--"+boundary;
        String boundaryEnd = "--"+boundary+"--";
        byte[] data;
        if(bufferSize < boundaryEnd.getBytes().length) {
            throw new LeapException(HTTP.RES500, "Socket buffer size cannot be less then: "+boundaryEnd.getBytes().length+" bytes");
        }
        try( ByteArrayOutputStream byteStream = new ByteArrayOutputStream() ) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while (byteStream.size() < contentLengh && (bytesRead = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }    
            data = byteStream.toByteArray();
        }
        return makePartdata(data, boundaryStart);
    }

    /**
     * Read Multipart request
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
     */
    public Map<String, byte[]> readPartData(String boundary, int contentLength, Charset charset) throws IOException {        
        String boundaryStart = "--"+boundary;        
        String boundaryEnd = "--"+boundary+"--";
        byte[] data;
        if(bufferSize < boundaryEnd.getBytes().length) {
            throw new LeapException(HTTP.RES500, "Socket buffer size cannot be less then: "+boundaryEnd.getBytes().length+" bytes");
        }
        //Read and load whole request data into byte stream object.
        try( ByteArrayOutputStream byteStream = new ByteArrayOutputStream() ) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);            
                //Escape loop when it reaches to content length.    
                if(byteStream.size() >= contentLength) {                    
                    break;
                }
            }    
            data = byteStream.toByteArray();
        }
        //Make the parts of Multipart reuqest.
        return makePartdata(data, boundaryStart);
    }

    /**
     * Make part data using boundary start string with bytes multipart data
     * @param data
     * @param boundaryStart
     * @return
     */
    private Map<String, byte[]> makePartdata(byte[] data, String boundaryStart) {
        Map<String, byte[]> partMap = new HashMap<>();        
        //Split the whole request data chunk to seprate parts. it's gonna be splited with boundary start string value.
        List<byte[]> parts = splitBySequence(data, boundaryStart);        
        for(byte[] part : parts) {
            //Finding index of part data starting point in the byes of part header and part data.
            int idx = findSequenceIndex(part, CRLF2.getBytes()) + CRLF2.getBytes().length;
            String head = new String(Arrays.copyOf(part, idx));
            byte[] partData = Arrays.copyOfRange(part, idx, part.length);
            Map<String, String> fieldMap = extractFields(head, List.of("name", "filename"));
            if(fieldMap.containsKey("filename")) {                
                partMap.put(fieldMap.get("filename"), partData);
            } else if (fieldMap.containsKey("name") && !fieldMap.containsKey("filename")) {
                partMap.put(fieldMap.get("name"), partData);
            }
        }        
        return partMap;
    }

    /**
     * Splie bytes by bytes sequence
     * @param fileBytes
     * @param sequence
     * @return
     */
    private List<byte[]> splitBySequence(byte[] fileBytes, String boundaryStart) {
        byte[] boundaryStartBytes = boundaryStart.getBytes();
        byte[] boundaryEndBytes = ("--"+boundaryStart+"--").getBytes();
        List<byte[]> parts = new ArrayList<>();
        int preIdx = 0;
        for (int i = 0; i <= fileBytes.length; i++) {
            final int idx = i;
            //the condition of matching bytes of both of start and end boundary with 'or' condition.
            if (Arrays.equals(Arrays.copyOfRange(fileBytes, idx, idx + boundaryStartBytes.length), boundaryStartBytes) 
                || Arrays.equals(Arrays.copyOfRange(fileBytes, idx, idx + boundaryEndBytes.length), boundaryEndBytes)) {
                if(i != 0) {                    
                    //Copy part data into part bytes. though i is start of boundary, to make i index minus CRLF length.
                    byte[] partData = Arrays.copyOfRange(fileBytes, preIdx, i - CRLF.getBytes().length);
                    parts.add(partData);
                    preIdx = i;                    
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
    private int findSequenceIndex(byte[] fileBytes, byte[] sequence) {
        for (int i = 0; i <= fileBytes.length - sequence.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(fileBytes, i, i + sequence.length), sequence)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Save multipart files to directory
     * @param saveDir
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
     */
    public List<Path> saveMultiPart(Path saveDir, String boundary, int contentLengh, Charset charset) throws IOException {        
        Map<String, byte[]> parts = readPartData(boundary, contentLengh, charset);
        return parts.entrySet().stream().map(e -> {
            try {
                return Files.write(saveDir.resolve(e.getKey()), e.getValue());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }).collect(Collectors.toList());
    }

     /**
     * Get multipart contents
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
    public Map<String, byte[]> getMultiPartContents(String boundary, Charset charset) throws IOException {                
        Map<String, byte[]> multiPartMap = new HashMap<>();
        String boundaryStart = "--"+boundary;
        String boundaryEnd = "--"+boundaryStart+"--";
        String line;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int len;
        while((len = inputStream.read(buffer)) != -1) {            
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
     * Read line from stream
     * @param is
     * @param charset
     * @return
     * @throws IOException
     */
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
     * Save multipart
     * @param saveDir
     * @param boundary
     * @return
     * @throws IOException
     */
    public List<Path> saveMultiPart1(Path saveDir, long contentLength, String boundary) throws IOException {
        String boundaryStart = "--"+boundary;        
        String boundaryEnd = boundary+"--";
        List<Path> paths = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.inputStream));
        String line;
        long total = 0;
        while((line = br.readLine()) != null && total < contentLength) {
            total += line.getBytes().length + 2;
            if(line.equals(boundaryStart) || line.startsWith("Content-Disposition")) {
                String header = line.equals(boundaryStart) ? "" : line;
                while(!(line = br.readLine()).equals("")) {
                    total += line.getBytes().length + 2;
                    header += line + System.lineSeparator();
                }
                Map<String, String> fields = extractFields(header, List.of("name", "filename"));
                String filename = fields.get("filename") != null ? fields.get("filename") : fields.get("name");
                Path dest = saveDir.resolve(filename);                
                FileOutputStream fos = new FileOutputStream(dest.toFile());
                ArrayList<Integer> queue = new ArrayList<>();
                int ch;
                while((ch = br.read()) != -1) {
                    queue.add(ch);
                    total++;        
                    if(queue.size() > 1) {
                        if(queue.get(queue.size()-2) == '\r' && queue.get(queue.size()-1) == '\n') {
                            if(queue.size() > boundaryStart.getBytes().length) {
                                String end = new String(convertToBytes(queue.subList(queue.size() - boundaryStart.getBytes().length -2, queue.size()-2)));
                                if(end.equals(boundaryStart) || end.equals(boundaryEnd)) {
                                    //System.out.println(end);
                                    int idx = end.equals(boundaryStart) ? boundaryStart.getBytes().length + 4 : boundaryEnd.getBytes().length + 8;
                                    byte[] sourceData = Files.readAllBytes(Paths.get("/home/kooin/Documents/Resume/시니어 풀스택 개발자 _ 엔지니어 - 신구인-20240919.docx"));
                                    byte[] partData = convertToBytes(queue.subList(0, queue.size() - idx)); 
                                    //System.out.println(new String(partData));                                    
                                    fos.write(partData);
                                    if(end.equals(boundaryEnd)) {
                                        line = boundaryEnd;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } 
                queue.clear();
                fos.close();                                                
                paths.add(dest);
            }
            if(line.equals(boundaryEnd)) {
                break;
            }
        }            
        return paths;
    }

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
                        if(baos.size() >= bufferSize && bufferSize > Constants.MULTIPART_FLUSH_MINIMAL_SIZE) {
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
}

