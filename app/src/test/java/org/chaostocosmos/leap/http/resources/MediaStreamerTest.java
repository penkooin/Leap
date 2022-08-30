package org.chaostocosmos.leap.http.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;    
    
public class MediaStreamerTest {

    @Before
    public void setup(){

    }
        
    @Test
    public void test() throws IOException {
    }

    MediaStreamerTest(Path resourcePath) throws IOException {
        this.resourcePath = resourcePath;
        this.resourceData = new ArrayList<>();        
        FileInputStream fis = new FileInputStream(resourcePath.toFile());
        long cnt = resourcePath.toFile().length() / Integer.MAX_VALUE;
        for(int i=0; i<cnt; i++) {
            byte[] bytes = new byte[Integer.MAX_VALUE-2];
            fis.read(bytes);
            this.resourceData.add(bytes);
        }
        byte[] bytes = new byte[Integer.MAX_VALUE-2];
        fis.read(bytes);
        this.resourceData.add(bytes);
    }

    Path resourcePath;

    boolean inMemoryFlag = true;

    List<byte[]> resourceData;

    public byte[] getBytes(long offset, int length) throws IOException {
        byte[] bytes = new byte[length];
        if(this.inMemoryFlag) {
            int len = 0;
            int start = (int)offset;
            int end  = (int)(offset + length);
            for(int i=0; i<this.resourceData.size(); i++) {
                byte[] data = this.resourceData.get(i);
                System.out.println("start: "+start+" end: "+end+"  range: "+(i*data.length)+" - "+(i + 1)*data.length);
                if(start >= i * data.length && start < (i + 1)*data.length) {
                    if(end < (i + 1)*data.length) {
                        System.out.println("being exit "+start+"  "+length);
                        System.arraycopy(data, start - i*data.length, bytes, 0, length);
                        break;
                    } else {
                        len += (i + 1)*data.length - start;
                        System.out.println("begin "+start+"  "+len);
                        System.arraycopy(data, start, bytes, 0, len);
                    }   
                } else if(end >= i * data.length  && end < (i + 1)*data.length) {
                    System.out.println("end  len: "+len);
                    System.arraycopy(data, 0, bytes, len, bytes.length - len);
                    break;
                } else if(start < i * data.length  && end > (i + 1)*data.length) {
                    System.out.println("middle ");
                    System.arraycopy(data, 0, bytes, i * data.length , data.length);
                    len += data.length;
                }
            }
        } else {
            FileInputStream fis = new FileInputStream(this.resourcePath.toFile());
            fis.skip(offset);
            fis.read(bytes);
        }
        return bytes;
    }

    public static void main(String[] args) throws IOException {
        Path resourcePath = Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static\\video\\video.mp4");
        // System.out.println(resourcePath.toFile().length());
        MediaStreamerTest test = new MediaStreamerTest(resourcePath);
        test.getBytes(1000L, 100);
        
        // test.inMemoryFlag = false;
        // byte[] bytes = test.getBytes(7445, 20);
        // System.out.println(Arrays.toString(bytes));
        // test.inMemoryFlag = true;
        // bytes = test.getBytes(7445, 20);
        // System.out.println(Arrays.toString(bytes));
    }
}
    