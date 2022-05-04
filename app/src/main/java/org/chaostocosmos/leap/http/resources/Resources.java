package org.chaostocosmos.leap.http.resources;

import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.resources.WatchResources.ResourceInfo;

/**
 * Resource model
 * 
 * @author 9ins
 */
public interface Resources {

    /**
     * Filtering and get resource List by mime-type
     * @param mimeType
     * @return
     */
    public Map<String, ResourceInfo> filter(MIME_TYPE mimeType);

    /**
     * Get context mapping with local Path
     * @param resourcePath
     * @return
     */
    public String getContextPath(Path resourcePath);

    /**
     * Add resource 
     * @param resourcePath
     */
    public void addResource(Path resourcePath) throws Exception;

    /**
     * Get resource matching with path
     * @param resourcePath
     * @return
     */
    public ResourceInfo getResourceInfo(Path resourcePath) throws Exception;

    /**
     * Get resource content
     * @param contentName
     * @return
     * @throws Exception
     */
    public ResourceInfo getContextResourceInfo(String contextPath) throws Exception;

    /**
     * Whether resource exist in Resource
     * @param resourcePath
     * @return
     */
    public boolean exists(Path resourcePath) throws Exception;

    /**
     * Get welcome page
     * @param params
     * @return
     * @throws Exception
     */
    public String getWelcomePage(Map<String, Object> params) throws Exception;

    /**
     * Get response page
     * @param params
     * @return
     * @throws Exception
     */
    public String getResponsePage(Map<String, Object> params) throws Exception;

    /**
     * Get error page
     * @param params
     * @return
     * @throws Exception
     */
    public String getErrorPage(Map<String, Object> params) throws Exception;

    /**
     * Get static page
     * @param resourceName
     * @param params
     * @return
     * @throws Exception
     */
    public String getStaticPage(String resourceName, Map<String, Object> params) throws Exception;

    /**
     * Get template page
     * @param resourceName
     * @param params
     * @return
     * @throws Exception
     */
    public String getTemplatePage(String resourceName, Map<String, Object> params) throws Exception;

    /**
     * Get resource page
     * @param params
     * @return
     * @throws Exception
     */
    public String getResourcePage(Map<String, Object> params) throws Exception;
}
