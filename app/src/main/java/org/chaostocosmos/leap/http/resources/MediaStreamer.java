package org.chaostocosmos.leap.http.resources;

import java.io.IOException;
import java.nio.file.Path;

import com.drew.imaging.ImageProcessingException;

import org.chaostocosmos.leap.http.resources.WatchResources.ResourceInfo;

public class MediaStreamer {

    ResourceInfo resourceInfo;
    Path mediaPath;
    int bytePerSeconds;

    public MediaStreamer(ResourceInfo resourceInfo) throws ImageProcessingException, IOException {
        this.resourceInfo = resourceInfo;
        int duration = Integer.parseInt(this.resourceInfo.getMetadataValue("MP4", "Duration"));
        long totalSize = this.resourceInfo.getResourceSize();        
        bytePerSeconds = (int)(totalSize / duration);
    }

    public byte[] getForword(long offset, int seconds) throws IOException {        
        return this.resourceInfo.getBytes(offset, this.bytePerSeconds * seconds);
    }

    public byte[] getBackword(long offset, int seconds) throws IOException {
        offset = offset - (this.bytePerSeconds * seconds);
        offset = offset < 0 ? 0 : offset;
        return this.resourceInfo.getBytes(offset, this.bytePerSeconds * seconds);
    }

    public byte[] getProgress(long offset, int amount) throws IOException {        
        return this.resourceInfo.getBytes(offset, amount);    
    }
}

