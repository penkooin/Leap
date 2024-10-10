package org.chaostocosmos.leap.resource.model;

import java.nio.file.Path;

import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.resource.Resource;

/**
 * Resource model
 * @author 9ins
 */
public interface ResourcesWatcherModel extends Runnable {    

    /**
     * Filtering and get resource List by mime-type
     * @param mimeType
     * @return
     * @throws Exception
     */
    public Resource filter(MIME mimeType) throws Exception;

    /**
     * Resolve real path
     * @param contextPath
     * @return
     * @throws Exception
     */
    public Path resolveRealPath(String contextPath) throws Exception;

    /**
     * Get context mapping with local Path
     * @param resourcePath
     * @return
     */
    public String getContextPath(Path resourcePath);

    /**
     * Add resource 
     * @param resourcePath
     * @throws Exception
     */
    public void addResource(Path resourcePath) throws Exception;

    /**
     * Add resource raw data with Path
     * @param resourcePath
     * @param resourceRawData
     * @throws Exception
     */
    public void addResource(Path resourcePath, byte[] resourceRawData, boolean inMemoryFlag) throws Exception;

    /**
     * Remove resource
     * @param resourcePath
     * @throws Exception
     */
    public void removeResource(Path resourcePath) throws Exception;    

    /**
     * Get resource matching with path
     * @param resourcePath
     * @return
     * @throws Exception
     */
    public Resource getResource(Path resourcePath) throws Exception;

    /**
     * Get resource content
     * @param contentName
     * @return
     * @throws Exception
     */
    public Resource getContextResource(String contextPath) throws Exception;

    /**
     * Get resurce partial data
     * @param resource
     * @param position
     * @param length
     * @return
     * @throws Exception
     */
    public byte[] getResourceData(Path resource, long position, int length) throws Exception;

    /**
     * Get partial bytes of file.
     * @param resource
     * @param position
     * @param length
     * @return
     * @throws Exception
     */
    public byte[] getFilePartial(Path resource, long position, int length) throws Exception;

    /**
     * Whether resource exist in Resource
     * @param resourcePath
     * @return
     * @throws Exception
     */
    public boolean exists(Path resourcePath) throws Exception;

    /**
     * Whether resource is In-Memory 
     * @param resourcePath
     * @return
     */
    public boolean isInMemory(Path resourcePath);

    /**
     * Terminate resource watcher
     */
    public void terminate() throws Exception;

    /**
     * Start resource watcher 
     * @throws Exception
     */
    public void start() throws Exception;
}
