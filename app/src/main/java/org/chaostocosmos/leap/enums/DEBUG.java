package org.chaostocosmos.leap.enums;

import org.chaostocosmos.leap.context.Context;

/**
 * Debug code and message
 * 
 * @author 9ins
 */
public enum DEBUG {

    DEBUG001(1),
    DEBUG002(2),
    DEBUG003(3),
    DEBUG004(4),
    DEBUG005(5),
    DEBUG006(6),
    DEBUG007(7),
    DEBUG008(8),
    DEBUG009(9),
    DEBUG010(10);    

    int code;

    DEBUG(int code) {
        this.code = code;
    }
    /**
     * Get debug code
     * @return
     */
    public int code() {
        return this.code;
    }

    /**
     * Get debug message
     * @return
     */
    public String message() {
        return Context.messages().debug(this.code);
    }

    /**
     * Get debug message with parameters
     * @param parameters
     * @return
     */
    public String message(Object ... parameters) {
        return Context.messages().debug(this.code, parameters);
    }
}
