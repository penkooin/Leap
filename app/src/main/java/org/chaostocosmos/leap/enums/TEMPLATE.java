package org.chaostocosmos.leap.enums;

import java.nio.file.Path;
import java.util.Map;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.http.Html;

import java.io.IOException;
import java.nio.file.Files;

/**
 * TEMPLATE
 * 
 * @9ins
 */
public enum TEMPLATE {
    DEFAULT( WAR_PATH.TEMPLATES.path()+"/default.html"),
    ERROR(WAR_PATH.TEMPLATES.path()+"/error.html"),
    MONITOR(WAR_PATH.TEMPLATES.path()+"/monitor.html"),
    DIRECTORY(WAR_PATH.TEMPLATES.path()+"/directory.html"),
    RESPONSE(WAR_PATH.TEMPLATES.path()+"/response.html"),
    INDEX(WAR_PATH.TEMPLATES.path()+"/index.html");

    String resourceName;
    
    /**
     * Initialize
     * @param res
     */
    TEMPLATE(String res) {
        this.resourceName = res;
    }

    /**
     * Get resource path
     * @return
     */
    public String path() {
        return this.resourceName;
    }

    /**
     * Template path
     * @param hostId
     * @return
     */
    public Path getTemplatePath(String hostId) {
        return Context.get().host(hostId).getTemplates().resolve(this.name().toLowerCase()+".html");
    }    

    /**
     * Load template page
     * @param hostId
     * @return
     * @throws IOException
     */
    public String loadTemplatePage(String hostId) throws IOException {
        return Files.readString(getTemplatePath(hostId));
    }

    /**
     * Load template page with placehoder Map
     * @param hostId
     * @param placeHolderValueMap
     * @return
     * @throws IOException
     */
    public String loadTemplatePage(String hostId, Map<String, Object> placeHolderValueMap) throws IOException {
        String html = loadTemplatePage(hostId);
        return Html.resolvePlaceHolder(html, placeHolderValueMap);
    }
}
