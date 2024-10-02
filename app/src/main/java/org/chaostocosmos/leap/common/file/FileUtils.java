package org.chaostocosmos.leap.common.file;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.context.Context;

import com.coremedia.iso.IsoFile;

/**
 * FileUtils
 * 
 * @author 9ins
 */
public class FileUtils {

    /**
     * Save binary data
     * @param data
     * @param target
     * @throws IOException
     */
    public static void saveBinary(byte[] data, Path target) throws IOException {
        saveBinary(data, target, Context.get().server().getFileBufferSize());
    }

    /**
     * Save binary data
     * @param data
     * @param target
     * @param bufferSize
     * @throws IOException
     */
    public static void saveBinary(byte[] data, Path target, int bufferSize) throws IOException {
        FileOutputStream out = new FileOutputStream(target.toFile());
        bufferSize = bufferSize < data.length ? data.length : bufferSize;
        int pos = 0;
        int rest = data.length % bufferSize;
        int times = data.length / bufferSize;
        byte[] part;
        for(int i=0; i<times; i++) {
            part = Arrays.copyOfRange(data, pos, bufferSize);
            out.write(part);
            pos += bufferSize;            
        }
        if(rest > 0) {
            part = Arrays.copyOfRange(data, pos, rest);
            out.write(part);
        }
        out.close();                                
    }

    /**
     * Read file using FileInputStream
     * @param file
     * @param bufferSize
     * @return
     */
    public static byte[] readFile(File file, int bufferSize) {
        try(FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int)file.length()];
            byte[] buffer = new byte[bufferSize];
            int len;
            int idx = 0;
            while((len = fis.read(buffer)) > 0) {
                System.arraycopy(buffer, 0, data, idx, len);
                idx += len;
            }
            return data;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read File part using RandomAccessFile
     * @param file
     * @param idx
     * @param len
     * @return
     */
    public static byte[] readFilePart(File file, int idx, int len) {
        byte[] data = new byte[len];
        try(RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(idx);
            int readLen = raf.read(data);
            return data;            
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save text
     * @param data
     * @param target
     * @param charset
     * @throws IOException
     */
    public static void saveText(String data, Path target) throws IOException {
        FileWriter out = new FileWriter(target.toFile());
        out.write(data);
        out.close();        
    }    

    /**
     * Search files for given wildcard keywords
     * @param rootPath
     * @param wildcardKeywords
     * @return
     * @throws IOException
     */
    public static List<File> searchFiles(Path rootPath, List<String> wildcardKeywords) throws IOException {
        List<File> searchFiles = new ArrayList<>();
        for(File file : Files.walk(rootPath).sorted().map(Path::toFile).collect(Collectors.toList())) {
            for(String keyword : wildcardKeywords) {
                String regex = Arrays.asList(keyword.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*";
                if(file.getAbsolutePath().matches(regex)) {
                    searchFiles.add(file);
                }   
            }
        }
        return searchFiles;
    }    

    /**
     * Get MP4 media file duration in seconds
     * @param mp4Path
     * @return
     * @throws IOException
     */
    public static double getMp4DurationSeconds(Path mp4Path) throws IOException {
        IsoFile isoFile = new IsoFile(mp4Path.toAbsolutePath().toString());
        return (double)isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
    }
}
