package org.chaostocosmos.leap.enums;

import java.util.List;

import org.chaostocosmos.leap.LeapServer;

/**
 * Grant for user
 */
public enum ROLES {
    ADMIN(List.of(REQUEST.GET, REQUEST.PUT, REQUEST.DELETE, REQUEST.HEAD, REQUEST.OPTIONS, REQUEST.PATCH, REQUEST.POST), LeapServer.getHost().getValue("global.roles")),
    USER(List.of(REQUEST.GET, REQUEST.PUT, REQUEST.POST), LeapServer.getHost().getValue("global.roles")),
    GUEST(List.of(REQUEST.GET), LeapServer.getHost().getValue("global.roles"));

    /**
     * Request types
     */
    List<REQUEST> requestTypes;

    /**
     * Role paths
     */
    List<String> paths;

    /**
     * Initiate
     * @param requestTypes
     * @param paths
     */
    ROLES(List<REQUEST> requestTypes, List<String> paths) {
        this.requestTypes = requestTypes;
        this.paths = paths;
    }
}
