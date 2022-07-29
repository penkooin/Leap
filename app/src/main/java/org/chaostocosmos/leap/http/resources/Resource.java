package org.chaostocosmos.leap.http.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.chaostocosmos.leap.http.enums.MIME_TYPE;

/**
 * ResourceInfo
 * 
 * @author 9ins
 */
public class Resource extends HashMap<String, Resource> {
    /**
     * Whether node resource
     */
    boolean isNode;

    /**
     * Mapping context path
     */
    String contextPath;

    /**
     * Resource path
     */
    Path resourcePath;

    /**
     * In-Memory flag 
     */
    boolean inMemoryFlag;

    /**
     * Resource data List
     */
    List<byte[]> resourceData;

    /**
     * Resource mime-type  
     */        
    MIME_TYPE mimeType;

    /**
     * Resource last modified time
     */
    long lastModified;

    /**
     * Resource File
     */
    File resourceFile;

    /**
     * Resource size
     */
    long resourceSize;

    /**
     * Split size
     */
    int splitSize;

    /**
     * Constructs with resource path & in-memory flag
     * @param isNode
     * @param resourcePath
     * @param inMemoryFlag
     * @param splitSize
     * @throws IOException
     */
    public Resource(boolean isNode, Path resourcePath, boolean inMemoryFlag, int splitSize) throws IOException {
        this.isNode = isNode;
        this.resourcePath = resourcePath;
        this.resourceFile = resourcePath.toFile();
        this.resourceSize = resourcePath.toFile().length();
        this.inMemoryFlag = inMemoryFlag;
        this.splitSize = splitSize;
        this.mimeType = MIME_TYPE.mimeType(Files.probeContentType(this.resourcePath));
        if(this.inMemoryFlag) {
            this.resourceData = Collections.synchronizedList(new ArrayList<>());
            try(FileInputStream fis = new FileInputStream(resourcePath.toFile())) {
                long fileSize = this.resourcePath.toFile().length();
                int cnt = (int) (fileSize / splitSize);
                int rest = (int) (fileSize % splitSize);
                for(int i=0; i<cnt; i++) {
                    byte[] part = new byte[splitSize];
                    fis.read(part);
                    this.resourceData.add(part);
                }
                byte[] part = new byte[rest];
                fis.read(part);
                this.resourceData.add(part);  
            };
        }
        this.lastModified = resourcePath.toFile().lastModified();
    }  

    /**
     * Constructs with node flag, path, raw data
     * @param isNode
     * @param resourcePath
     * @param resourceRawData
     * @param splitSize
     * @throws IOException
     */
    public Resource(boolean isNode, Path resourcePath, byte[] resourceRawData, boolean inMemoryFlag, int splitSize) throws IOException {
        this.isNode = isNode;
        this.resourcePath = resourcePath;
        this.resourceFile = resourcePath.toFile();
        this.resourceSize = resourcePath.toFile().length();
        this.inMemoryFlag = inMemoryFlag;
        this.splitSize = splitSize;
        this.mimeType = MIME_TYPE.mimeType(Files.probeContentType(this.resourcePath));
        if(this.inMemoryFlag) {
            this.resourceData = Collections.synchronizedList(new ArrayList<>());
            long fileSize = resourceRawData.length;
            int cnt = (int)(fileSize / splitSize);
            int rest = (int)(fileSize % splitSize);
            byte[] part = null;
            int i = 0;
            for(i=0; i<cnt; i++) {
                part = new byte[splitSize];
                System.arraycopy(resourceRawData, i * part.length, part, 0, part.length);
                this.resourceData.add(part);
            }
            part = new byte[rest];
            System.arraycopy(resourceRawData, cnt * part.length, part, 0, part.length);
            this.resourceData.add(part);
        } else {
            try(FileOutputStream out = new FileOutputStream(resourcePath.toFile())) {
                out.write(resourceRawData);
            }
        }
        this.lastModified = System.currentTimeMillis();
    }     

    /**
     * Get all bytes on resource
     * @return
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        if(this.inMemoryFlag) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for(byte[] part : this.resourceData) {
                out.write(part);
            }
            return out.toByteArray();
        } else {
            return Files.readAllBytes(this.resourcePath);
        }
    }

    /**
     * Get bytes of resource(file or memory) with position & length
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] getBytes1(long position, int length) throws IOException {
        byte[] bytes = new byte[length];
        if(this.inMemoryFlag) {
            if(position < 0) {
                throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
            }
            int start = (int) position;
            int end  = (int) (position + length);
            int posStart = 0, posEnd = 0;
            int idx = 0;
            for(int i=0; i<this.resourceData.size(); i++) {
                byte[] data = this.resourceData.get(i);
                posEnd += data.length;
                posStart = posEnd - data.length;
                //System.out.println("start: "+start+" end: "+end+"  range: "+posStart+" - "+posEnd+"  i: "+i);
                if(start >= posStart && start < posEnd) {
                    if(end <= posEnd) {
                        System.arraycopy(data, start - posStart, bytes, 0, length);
                        break;
                    } else {
                        idx = posEnd - start;
                        System.arraycopy(data, start - posStart, bytes, 0, idx);
                        continue;
                    }
                } else if(start <= posStart) {
                    if(end < posEnd) {
                        int len = (posEnd - end);
                        //System.out.println("begin "+start+"  end: "+end+" data: "+data.length+"  posStart: "+posStart+"  posEnd: "+posEnd+"  len: "+len);
                        if(idx + len < posEnd) {
                            System.arraycopy(data, 0, bytes, idx, data.length - len);
                        } else {
                            System.arraycopy(data, 0, bytes, idx, posEnd - len);
                        }
                        break;    
                    } else {
                        System.arraycopy(data, 0, bytes, idx, data.length);
                        //System.out.println("idx: "+idx);
                        idx += data.length;
                    }
                }
            }
        } else {
            bytes = getFilePartial(position, length);
        }
        return bytes;
    }    

    /**
     * Get bytes of resource(file or memory) with position & length
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] getBytes3(long position, int length) throws Exception {
        byte[] bytes = new byte[length];
        if(this.inMemoryFlag) {                
            if(position < 0) {
                throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
            }
            int start = (int) position;
            int end  = (int) (position + length);
            int posStart = 0, posEnd = 0;
            int pos = 0;
            for(int i=0; i<this.resourceData.size(); i++) {
                byte[] data = this.resourceData.get(i);
                posEnd += data.length;
                posStart = posEnd - data.length;
                //System.out.println("start: "+start+" end: "+end+"  range: "+posStart+" - "+posEnd+"  i: "+i);
                if(start >= posStart && start < posEnd) {
                    if(end <= posEnd) {
                        System.arraycopy(data, start - posStart, bytes, 0, length);
                        break;
                    } else {
                        pos = posEnd - start;
                        System.arraycopy(data, start - posStart, bytes, 0, pos);
                        continue;
                    }
                } else if(start < posStart) {
                    if(end < posEnd) {
                        int len = posStart +(posEnd - end);
                        System.out.println("begin "+start+"  end: "+end+" data: "+data.length+"  posStart: "+posStart+"  posEnd: "+posEnd+"  len: "+len+"  data: "+data.length+"  pos: "+pos);
                        if(pos + len <= end) {
                            System.arraycopy(data, 0, bytes, pos, len);
                        } else {
                            System.arraycopy(data, 0, bytes, pos, posEnd - len);
                        }
                        break;
                    } else {
                        System.arraycopy(data, 0, bytes, pos, data.length);
                        //System.out.println("idx: "+idx);
                        pos += data.length;
                    }
                }
            } 
        } else {
            bytes = getFilePartial(position, length);
        }
        return bytes;
    }

    /**
     * Get bytes of resource(file or memory) with position & length
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public synchronized byte[] getBytes2(long position, int length) throws IOException {
        if(this.inMemoryFlag) {
            if(position < 0) {
                throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
            }
            byte[] bytes = new byte[length];
            long totalLen = this.resourcePath.toFile().length();
            int totalCnt = this.resourceData.size();
            int partIdx1 = (int) ( position / (totalCnt * this.splitSize) );
            int partIdx2 = (int) ( (position + length) / (totalCnt * this.splitSize) );
            System.out.println("filename: "+this.resourceFile.getName()+"  partIdx1: "+partIdx1+"  partIdx2: "+partIdx2+"  total count: "+totalCnt+"  position: "+position+"  buffer: "+length+"  part size: "+this.resourceData.get(0).length);
            if(partIdx1 == partIdx2) {
                byte[] part = this.resourceData.get(partIdx1);
                System.arraycopy(part, (int)(position % part.length), bytes, 0, length);
            } else {
                if(partIdx2 - partIdx1 == 1) {
                    byte[] part = this.resourceData.get(partIdx1); 
                    int idx = (int) ( position - (partIdx1 * part.length) );
                    System.arraycopy(part, idx, bytes, 0, part.length - idx);
                    part = this.resourceData.get( partIdx2 );
                    System.arraycopy(part, 0, bytes, part.length - idx, length - (part.length - idx));
                } else {
                    byte[] part = this.resourceData.get(partIdx1); 
                    int idx = (int) ( position - (partIdx1 * part.length) );
                    System.arraycopy(part, idx, bytes, 0, part.length - idx);
                    for(int i=0; i < (partIdx2 - partIdx1); i++) {
                        part = this.resourceData.get(i);
                        System.arraycopy(part, 0, bytes, idx += (i * part.length), part.length);
                    }
                    part = this.resourceData.get(partIdx2);
                    System.arraycopy(part, 0, bytes, idx, (int)(totalLen % idx));
                }
            }
            return bytes;
        } else {
            return getFilePartial(position, length);
        }
    }

    /**
     * Get partitial bytes of File resource by position & length
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] getFilePartial(long position, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        try(FileInputStream fis = new FileInputStream(this.resourcePath.toFile())) {
            FileChannel channel = fis.getChannel();
            channel.position(position);
            channel.read(buffer);
            channel.close();
            return buffer.array();    
        }
    }

    /**
     * Get whether node resource 
     * @return
     */
    public boolean isNode() {
        return this.isNode;
    }

    /**
     * Get resource Path object
     * @return
     */
    public Path getPath() {
        return this.resourcePath;
    }

    /**
     * Get context path of resource
     * @return
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Get resource name
     * @return
     */
    public String getResourceName() {
        return this.resourcePath.getFileName().toString();
    }

    /**
     * Get resource File
     * @return
     */
    public File getFile() {
        return this.resourceFile;
    }

    /**
     * Get resurce size
     * @return
     */
    public long getResourceSize() {
        return this.resourceSize;
    }

    /**
     * Get mime-type of resource
     * @return
     */
    public MIME_TYPE getMimeType() {
        return this.mimeType;
    }

    /**
     * Get last modified time of specfied unit
     * @param timeUnit
     * @return
     */
    public long getTime(TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(this.lastModified, timeUnit);
    }

    /**
     * Whether In-Memory resource
     * @return
     */
    public boolean isInMemory() {
        return this.inMemoryFlag;
    }
}
