package org.chaostocosmos.leap.resource.config;

import java.util.List;

import org.chaostocosmos.leap.context.Metadata;
import org.chaostocosmos.leap.resource.utils.ResourceUtils;

/**
 * ResourceProviderConfig
 * 
 * @author 9ins
 */
public class ResourceConfig <T> extends Metadata <T> {

    private List<String> watchRoots = super.<List<String>>getValue("resource.watch-paths");
    private List<String> watchKind = super.<List<String>>getValue("resource.watch-kind");
    private List<String> accessFilters = super.<List<String>>getValue("resource.access-filters");
    private List<String> inMemoryFilters = super.<List<String>>getValue("resource.in-memory-filters");;
    private long totalMemorySizeLimit = ResourceUtils.fromString(super. getValue("resource.total-memory-size-limit"));
    private int splitUnitSize = (int) ResourceUtils.fromString(super. getValue("resource.split-unit-size"));
    private int fileSizeLimit = (int) ResourceUtils.fromString(super. getValue("resource.file-size-limit"));
    private int fileReadBufferSize = (int) ResourceUtils.fromString(super. getValue("resource.file-read-buffer-size"));
    private int fileWriteBufferSize = (int) ResourceUtils.fromString(super. getValue("resource.file-write-buffer-size"));

    /**
     * Constructor
     * @param configMap
     */
    public ResourceConfig(T configMap) {
        super(configMap);
    }

    public List<String> getWatchRoots() {
        return watchRoots;
    }

    public void setWatchRoots(List<String> watchRoots) {
        this.watchRoots = watchRoots;
    }

    public List<String> getWatchKind() {
        return watchKind;
    }

    public void setWatchKind(List<String> watchKind) {
        this.watchKind = watchKind;
    }

    public List<String> getAccessFilters() {
        return accessFilters;
    }

    public void setAccessFilters(List<String> accessFilters) {
        this.accessFilters = accessFilters;
    }

    public List<String> getInMemoryFilters() {
        return inMemoryFilters;
    }

    public void setInMemoryFilters(List<String> inMemoryFilters) {
        this.inMemoryFilters = inMemoryFilters;
    }

    public long getTotalMemorySizeLimit() {
        return totalMemorySizeLimit;
    }

    public void setTotalMemorySizeLimit(long totalMemorySizeLimit) {
        this.totalMemorySizeLimit = totalMemorySizeLimit;
    }

    public int getSplitUnitSize() {
        return splitUnitSize;
    }

    public void setSplitUnitSize(int splitUnitSize) {
        this.splitUnitSize = splitUnitSize;
    }

    public int getFileSizeLimit() {
        return fileSizeLimit;
    }

    public void setFileSizeLimit(int fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
    }

    public int getFileReadBufferSize() {
        return fileReadBufferSize;
    }

    public void setFileReadBufferSize(int fileReadBufferSize) {
        this.fileReadBufferSize = fileReadBufferSize;
    }

    public int getFileWriteBufferSize() {
        return fileWriteBufferSize;
    }

    public void setFileWriteBufferSize(int fileWriteBufferSize) {
        this.fileWriteBufferSize = fileWriteBufferSize;
    }

    @Override
    public String toString() {
        return "ResourceProviderConfig [watchRoots=" + watchRoots + ", watchKind=" + watchKind + ", accessFilters="
                + accessFilters + ", inMemoryFilters=" + inMemoryFilters + ", totalMemorySizeLimit="
                + totalMemorySizeLimit + ", splitUnitSize=" + splitUnitSize + ", fileSizeLimit=" + fileSizeLimit
                + ", fileReadBufferSize=" + fileReadBufferSize + ", fileWriteBufferSize=" + fileWriteBufferSize + "]";
    }    
}