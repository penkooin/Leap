package org.chaostocosmos.leap.enums;

import org.chaostocosmos.leap.context.Context;

/**
 * Information code and message
 * 
 * @author 9ins
 */
public enum INFO {

    INFO001(1),
    INFO002(2),
    INFO003(3),
    INFO004(4),
    INFO005(5),
    INFO006(6),
    INFO007(7),
    INFO008(8),
    INFO009(9),
    INFO010(10);       

    /**
     * Info message code;
     */
    int code ;

    INFO(int code) {
        this.code = code;
    }

    /**
     * Get info code
     * @return
     */
    public int code() {
        return this.code;
    }

    /**
     * Get info message
     * @return
     */
    public String message() {
        return Context.get().messages().info(this.code);
    }
 
    /**
     * Get info message with parameters
     * @param parameters
     * @return
     */
    public String message(Object ... parameters) {
        return Context.get().messages().info(this.code, parameters);
    }
}
