package org.chaostocosmos.leap.http;

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
     * Get resource matching with context path
     * @param resourcePath
     * @return
     */
    public Object getResource(Path resourcePath);

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
     * Get static page
     * @param resourceName
     * @param params
     * @return
     * @throws IOException
     */
    public String getStaticPage(String resourceName, Map<String, Object> params) throws IOException;

    /**
     * Get resource content
     * @param contentName
     * @return
     * @throws IOException
     */
    public Object getStaticContent(String contextPath) throws IOException;
    
}
