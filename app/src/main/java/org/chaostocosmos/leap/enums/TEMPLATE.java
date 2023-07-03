package org.chaostocosmos.leap.enums;

import java.nio.file.Path;

import org.chaostocosmos.leap.context.Context;

/**
 * TEMPLATE
 * 
 * @9ins
 */
public enum TEMPLATE {
    DEFAULT("/templates/default.html"),
    ERROR("/templates/error.html"),
    MONITOR("/templates/monitor.html"),
    RESOURCE("/templates/resource.html"),
    RESPONSE("/templates/response.html"),
    WELCOME("/templates/welcome.html");

    String res;
    
    /**
     * Initialize
     * @param res
     */
    TEMPLATE(String res) {
        this.res = res;
    }

    /**
     * Get resource path
     * @return
     */
    public String path() {
        return this.res;
    }

    /**
     * Template path
     * @param hostId
     * @return
     */
    public Path getPath(String hostId) {
        return Context.get().host(hostId).getTemplates().resolve(this.name().toLowerCase()+".html");
    }    
}
