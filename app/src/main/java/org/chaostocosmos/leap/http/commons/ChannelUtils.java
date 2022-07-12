package org.chaostocosmos.leap.http.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Logger;

/**
 * ChannelUtils
 * 
 * @author 9ins
 */
public class ChannelUtils {

    /**
     * Save binary body
     * @param host
     * @param socketChannel
     * @param contentLength
     * @param savePath
     * @param flushSize
     * @throws IOException
     */
    public static void save(String host, SocketChannel socketChannel, long contentLength, Path savePath, int flushSize) throws IOException {
        Logger logger = LoggerFactory.getLogger(host);
        //String line = readLine(requestStream, StandardCharsets.ISO_8859_1);
        ByteBuffer buffer = ByteBuffer.allocate(flushSize);
        FileChannel fileChannel = FileChannel.open(savePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
        int read = 0;
        while((read=socketChannel.read(buffer)) > 0) {
            buffer.flip();
            fileChannel.write(buffer);
        }
        socketChannel.close();
        fileChannel.close();
        logger.debug("Save Binary Data To: "+savePath.toString()+"   Size: "+savePath.toFile().length());
    }

    /**
     * Get Multi-part data
     * @param host
     * @param socketChannel
     * @param boundary
     * @param charset
     * @return
     * @throws IOException
     */
    public static Map<String, byte[]> getMultiPartContents(String host, SocketChannel socketChannel, ByteBuffer buffer, String boundary, String charset, long contentLength, int bufferSize) throws IOException {
        Map<String, byte[]> multiPartMap = new HashMap<>();
        String boundaryStart = "--"+boundary;
        String boundaryEnd = boundaryStart+"--";
        int len = 0;
        int ch = 0;        
        ByteArrayOutputStream line = new ByteArrayOutputStream();                
        do {
            socketChannel.read(buffer);
            buffer.flip();
            while(buffer.hasRemaining()) {
                ch = buffer.get();
                line.write(ch);
                len++;
                if((char)ch == '\n') 
                    break;
            }
            if(line.equals("\r\n")) {
                
            }
            if(line.toString().endsWith(boundaryStart+"\r\n")) {
                // String contentDesposition = readLine(reader, charset);
                // String contentType =  readLine(reader, charset);
                // contentType = contentType.substring(contentType.indexOf(":")+1).trim();
                // String emptyLine = reader.readLine();
                // Map<String, String> map = getBoundaryMap(contentDesposition);
                // multiPartMap.put(map.get("filename"), Arrays.copyOfRange(dataBytes, 0, dataBytes.length));                
            } else if(line.toString().endsWith(boundaryEnd+"\r\n")) {
                
            }
            buffer.compact();
            //System.out.println(len+"  "+contentLength+"  "+new String(buffer.array(), charset));
        } while(len < contentLength);
        return multiPartMap;
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
     * Read line
     * @param socketChannel
     * @param buffer
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readLine(SocketChannel socketChannel, ByteBuffer buffer, String charset) throws IOException {
        int read = 0;
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        while((read=socketChannel.read(buffer)) > 0) {
            buffer.flip();
            while(buffer.hasRemaining()) {
                int ch = buffer.get();
                line.write(ch);
                if((char)ch == '\n') {
                    buffer.compact();
                    return line.toString(charset);
                }
            }
            buffer.compact();
        }        
        return null;
    }

    /**
     * Read header part
     * @param channel
     * @param buffer
     * @return
     * @throws IOException
     */
    public static Map<String, List<String>> readHeaders(SocketChannel channel, ByteBuffer buffer) throws IOException {        
        //cr : 0x0A
        //lf : 0x0D
        int cnt = 0;
        String lines = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        char ch = 0, pre = 0;
        while((cnt=channel.read(buffer)) > 0) {
            buffer.flip();
            while(buffer.hasRemaining()) {
                ch = (char)buffer.get();                
                out.write(ch);                
                if(pre == '\r' && ch == '\n') {
                    break;
                }
                pre = ch;
            }
            buffer.compact();
            lines = out.toString("utf-8");
            if(lines.toString().endsWith("\r\n\r\n")) {                
                break;
            }                
        }
        if(lines == null) {
            return null;
        }                
        List<String> list = new ArrayList<>(Arrays.asList(lines.split("\r\n")));
        list.removeIf(e -> e.trim().equals(""));
        return list.stream()
                   .map(l -> {          
                       String key = l.substring(0, l.indexOf(":")).trim();
                       List<String> value = Arrays.asList(l.substring(l.indexOf(":") + 1).split(";")).stream().map(t -> t.trim()).collect(Collectors.toList());
                       return new Object[]{key, value};
                   })
                   .collect(Collectors.toMap(k -> (String)k[0], v -> (List<String>)v[1]));
    }
}
