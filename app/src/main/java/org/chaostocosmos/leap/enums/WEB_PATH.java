package org.chaostocosmos.leap.enums;

import java.nio.file.Path;

import org.chaostocosmos.leap.context.Context;

/**
 * WEB_PATH enum
 * 
 * @9ins
 */
public enum WEB_PATH {
    DOCROOT("/"),
    CONFIG("/config"),
    LOGS("/logs"),
    WEBAPP("/webapp"),
    WEBINF("/webapp/WEB-INF"),
    CLASSES("/webapp/WEB-INF/classes"),
    CSS("/webapp/WEB-INF/css"),
    IMG("/webapp/WEB-INF/img"),
    SCRIPT("/webapp/WEB-INF/script"),
    TEMPLATES("/webapp/WEB-INF/templates"),
    VIDEO("/webapp/WEB-INF/video"),
    VIEWS("/webapp/WEB-INF/views");

    String res;

    /**
     * Initializer
     * @param res
     */
    WEB_PATH(String res) {
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
     * Get 
     * @param hostId
     * @return
     */
    public Path getPath(String hostId) {
        return Context.get().host(hostId).getDocroot().resolve(path());
    }
}
