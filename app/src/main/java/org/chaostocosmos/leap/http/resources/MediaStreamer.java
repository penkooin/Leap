package org.chaostocosmos.leap.http.resources;

import java.io.IOException;
import java.nio.file.Path;

import org.chaostocosmos.leap.http.commons.FileUtils;

/**
 * MediaStream
 * 
 * @author 9ins
 */
public class MediaStreamer {

    Resource resourceInfo;
    Path mediaPath;
    int bytePerSeconds;

    /**
     * Constructs with ResourceInfo object
     * @param resourceInfo
     * @throws IOException
     */
    public MediaStreamer(Resource resourceInfo) throws IOException {
        this.resourceInfo = resourceInfo;
        double duration = FileUtils.getMp4DurationSeconds(this.resourceInfo.getPath());
        double totalSize = this.resourceInfo.getResourceSize() * 1.0d;        
        bytePerSeconds = (int)(totalSize / duration);
    }

    /**
     * Get forword bytes of given seconds
     * @param offset
     * @param seconds
     * @return
     * @throws IOException
     */
    public byte[] getForword(long offset, int seconds) throws Exception {        
        return this.resourceInfo.getBytes1(offset, this.bytePerSeconds * seconds);
    }

    /**
     * Get backword bytes of given seconds
     * @param offset
     * @param seconds
     * @return
     * @throws IOException
     */
    public byte[] getBackword(long offset, int seconds) throws Exception {
        offset = offset - (this.bytePerSeconds * seconds);
        offset = offset < 0 ? 0 : offset;
        return this.resourceInfo.getBytes1(offset, this.bytePerSeconds * seconds);
    }

    /**
     * Get forword progress bytes of amount
     * @param offset
     * @param amount
     * @return
     * @throws IOException
     */
    public byte[] getProgress(long offset, int amount) throws Exception {        
        return this.resourceInfo.getBytes1(offset, amount);    
    }
}

