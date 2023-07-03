package org.chaostocosmos.leap.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.enums.MIME;

/**
 * Resource model
 * @author 9ins
 */
public interface ResourcesModel {    
    /**
     * Filtering and get resource List by mime-type
     * @param mimeType
     * @return
     * @throws IOException
     */
    public Resource filter(MIME mimeType) throws IOException;
    /**
     * Resolve real path
     * @param contextPath
     * @return
     * @throws IOException
     */
    public Path resolveRealPath(String contextPath) throws IOException;
    /**
     * Get context mapping with local Path
     * @param resourcePath
     * @return
     */
    public String getContextPath(Path resourcePath);
    /**
     * Add resource 
     * @param resourcePath
     * @throws IOException
     */
    public void addResource(Path resourcePath) throws IOException;
    /**
     * Add resource raw data with Path
     * @param resourcePath
     * @param resourceRawData
     * @throws IOException
     */
    public void addResource(Path resourcePath, byte[] resourceRawData, boolean inMemoryFlag) throws IOException;
    /**
     * Remove resource
     * @param resourcePath
     * @throws IOException
     */
    public void removeResource(Path resourcePath) throws IOException;    
    /**
     * Get resource matching with path
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public Resource getResource(Path resourcePath) throws IOException;
    /**
     * Get resource content
     * @param contentName
     * @return
     * @throws IOException
     */
    public Resource getContextResource(String contextPath) throws IOException;
    /**
     * Get resurce partial data
     * @param resource
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] getResourceData(Path resource, long position, int length) throws IOException;

    /**
     * Get partial bytes of file.
     * @param resource
     * @param position
     * @param length
     * @return
     * @throws IOException
     */
    public byte[] getFilePartial(Path resource, long position, int length) throws IOException;
    /**
     * Whether resource exist in Resource
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public boolean exists(Path resourcePath) throws IOException;
    /**
     * Whether resource is In-Memory 
     * @param resourcePath
     * @return
     */
    public boolean isInMemory(Path resourcePath);
    /**
     * Get welcome page
     * @param params
     * @return
     * @throws IOException
     */
    public String getWelcomePage(Map<String, Object> params) throws IOException;
    /**
     * Get response page
     * @param params
     * @return
     * @throws IOException
     */
    public String getResponsePage(Map<String, Object> params) throws IOException;
    /**
     * Get error page
     * @param params
     * @return
     * @throws IOException
     */
    public String getErrorPage(Map<String, Object> params) throws IOException;
    /**
     * Get resource page
     * @param params
     * @return
     * @throws IOException
     */
    public String getResourcePage(Map<String, Object> params) throws IOException;
    /**
     * Get template page
     * @param hostId
     * @parma templatePath
     * @param params
     * @return
     * @throws IOException
     */
    public String getTemplatePage(String templatePath, Map<String, Object> params) throws IOException;
    /**
     * Get static page
     * @param resourceName
     * @param params
     * @return
     * @throws IOException
     */
    public String getViewPage(String resourceName, Map<String, Object> params) throws IOException;
    /**
     * Resolve HTML page between comment replacement id and params
     * @param htmlPage
     * @param params
     * @return
     * @throws IOException
     */
    public String resolvePage(String htmlPage, Map<String, Object> params) throws IOException;
}
