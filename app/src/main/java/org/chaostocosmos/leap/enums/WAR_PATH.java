package org.chaostocosmos.leap.enums;

import java.nio.file.Path;

import org.chaostocosmos.leap.context.Context;

/**
 * WEB_PATH enum
 * 
 * @9ins
 */
public enum WAR_PATH {
    ROOT("/"),
    CONFIG("config"),
    WEBAPP("webapp"),
    DOCROOT("webapp/static"),
    STATIC("webapp/static"),
    CSS("webapp/static/css"),
    IMG("webapp/static/image"),
    SCRIPT("static/script"),
    VIDEOS("static/video"),
    TEMPLATES("templates"),
    WEBINF("WEB-INF"),
    CLASSES("WEB-INF/classes"),
    LIB("WEB-INF/lib"),
    VIEWS("WEB-INF/views");

    String res;

    /**
     * Initializer
     * @param res
     */
    WAR_PATH(String res) {
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
        return Context.get().host(hostId).getHomePath().resolve(path());
    }
}
