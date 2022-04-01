package org.chaostocosmos.leap.http.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Resource model
 * 
 * @author 9ins
 */
public interface Resource {

    /**
     * Add resource 
     * @param resourcePath
     */
    public void addResource(Path resourcePath) throws IOException;

    /**
     * Get resource matching with path
     * @param resourcePath
     * @return
     */
    public Object getResource(Path resourcePath) throws IOException;

    /**
     * Get resource content
     * @param contentName
     * @return
     * @throws IOException
     */
    public Object getContextResource(String contextPath) throws IOException;

    /**
     * Whether resource exist in Resource
     * @param resourcePath
     * @return
     */
    public boolean exists(Path resourcePath) throws IOException;

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
     * Get static page
     * @param resourceName
     * @param params
     * @return
     * @throws IOException
     */
    public String getStaticPage(String resourceName, Map<String, Object> params) throws IOException;

    /**
     * Get template page
     * @param resourceName
     * @param params
     * @return
     * @throws IOException
     */
    public String getTemplatePage(String resourceName, Map<String, Object> params) throws IOException;

    /**
     * Get resource page
     * @param params
     * @return
     * @throws IOException
     */
    public String getResourcePage(Map<String, Object> params) throws IOException;
}
