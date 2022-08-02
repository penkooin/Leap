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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
     * Constructs with resource path & in-memory flag & file split size
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
            this.resourceData = new ArrayList<>();
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
     * Constructs with node flag, path, raw data, in-memory flag, splited file size
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
     * 
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
            final int startIdx = (int) (position / this.splitSize);
            final int endIdx = startIdx + (length % this.splitSize == 0 ? length / this.splitSize : length / this.splitSize + 1);
            final int pre = (int) (position % this.splitSize);
            final int post = (int) ((position + length) % this.splitSize);
            //System.out.println("resourceSize: "+this.resourceSize+"  splitCount: "+this.resourceData.size()+"  splitSize: "+this.splitSize+" startIdx: "+startIdx+"  endIdx: "+endIdx+"  pre: "+pre+"  post: "+post+"  position: "+position+"  length: "+length);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IntStream.range(startIdx, endIdx).mapToObj(i -> {
                byte[] data = this.resourceData.get(i);
                if(i == startIdx) {
                    // System.out.println("start i: " + i + "  len: " + (data.length - pre));
                    return Arrays.copyOfRange(data, pre, data.length);
                } else if(i == endIdx - 1 ) {
                    // System.out.println("end i: " + i + "  len: " + post);
                    return Arrays.copyOfRange(data, 0, post);
                } else {
                    // System.out.println("---------------------------------------");
                    return Arrays.copyOfRange(data, 0, data.length);
                }
            }).forEach(ba -> {
                try {
                    baos.write(ba);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            baos.close();
            return baos.toByteArray();
        } else {
            bytes = getFilePartial(position, length);
        }
        return bytes;
    } 

    /**
     * Get bytes of resource(file or memory) with position & length
     * 
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] getBytes2(final long position, final int length) throws IOException {
        if(this.inMemoryFlag) {
            if(position < 0) {
                throw new IllegalArgumentException("Offset position must be on positive side of the digit.");
            }
            int partIdx1 = (int) ( position / this.splitSize );
            int partIdx2 = (int) ((position + length) / this.splitSize);
            int pre = (int) ( position % this.splitSize );
            int post = (int) (( position + length) % this.splitSize );
            //System.out.println("filename: "+this.resourceFile.getName()+"  partIdx1: "+partIdx1+"  partIdx2: "+partIdx2+"  pre: "+pre+"  post: "+post+"  position: "+position+"  length: "+length+"  resourceCount: "+this.resourceData.size());
            if(partIdx1 == partIdx2) {
                byte[] part = this.resourceData.get(partIdx1);
                return Arrays.copyOfRange(part, pre, pre + length);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if(partIdx2 - partIdx1 == 1) {
                    byte[] part = this.resourceData.get( partIdx1 );
                    baos.write(Arrays.copyOfRange(part, pre, part.length));
                    part = this.resourceData.get( partIdx2 );
                    baos.write(Arrays.copyOfRange(part, 0, post));
                } else {
                    byte[] part = this.resourceData.get( partIdx1 );
                    baos.write(Arrays.copyOfRange(part, pre, part.length));
                    for(int i = partIdx1 + 1; i < partIdx2; i++) {
                        part = this.resourceData.get(i);
                        baos.write(part);
                        //System.out.println("i: "+i+"  pre: "+pre+"  post: "+post+"  part: "+part.length);
                    }
                    part = this.resourceData.get( partIdx2 );
                    baos.write(Arrays.copyOfRange(part, 0, post));
                }
                baos.close();
                return baos.toByteArray();
            }
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
