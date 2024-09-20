package org.chaostocosmos.leap.enums;

import java.nio.file.Path;

import org.chaostocosmos.leap.context.Context;

/**
 * WEB_PATH enum
 * 
 * @9ins
 */
public enum WEB_PATH {
    HOME("/"),
    CONFIG("/config"),
    LOGS("/logs"),
    WEBAPP("/webapp"),
    WEBINF("/WEB-INF"),
    CLASSES("/WEB-INF/classes"),
    DOCROOT("/static"),
    STATIC("/static"),
    CSS("/static/css"),
    IMG("/static/image"),
    SCRIPT("/static/script"),
    TEMPLATES("/templates"),
    VIDEOS("/static/videos"),
    VIEWS("/WEB-INF/views");

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
