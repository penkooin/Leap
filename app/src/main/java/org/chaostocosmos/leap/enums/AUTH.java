package org.chaostocosmos.leap.enums;

import org.chaostocosmos.leap.context.Context;

/**
 * Authentication methods
 * 
 * @author 9ins
 */
public enum AUTH {
    NONE,
    BASIC,
    OAUTH,
    JWT;

    /**
     * Get authentication method by host id
     * @param hostId
     * @return
     */
    public AUTH getAuth(String hostId) {
        return valueOf(Context.get().host(hostId).<String> getValue("authentication"));
    }
}
