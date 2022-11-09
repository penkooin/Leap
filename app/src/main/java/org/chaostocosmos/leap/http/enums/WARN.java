package org.chaostocosmos.leap.http.enums;

import org.chaostocosmos.leap.http.context.Context;

public enum WARN {
    // 'ServletDescriptor annotation not found in class: {}'
    WARN001(1),
    // 'Some exception in service({}). System received Null value as exception value. Original error is : {}. Maybe service method must have HttpRequestDescriptor and HttpResponseDescriptor.'
    WARN002(2),
    // reserved
    WARN003(3),
    // reserved
    WARN004(4),
    // reserved
    WARN005(5),
    // reserved
    WARN006(6),
    // reserved
    WARN007(7),
    // reserved
    WARN008(8),
    // reserved
    WARN009(9),
    // reserved
    WARN010(10);

    /**
     * Warning code
     */
    int code;

    /**
     * Initializer
     * @param code
     */
    WARN(int code) {
        this.code = code;
    }

    /**
     * Get warning code
     * @return
     */
    public int code() {
        return this.code;
    }

    /**
     * Get warning message
     * @return
     */
    public String message() {
        return Context.messages().warn(this.code);
    }

    /**
     * Get warning message with parameters
     * @param parameters
     * @return
     */
    public String message(Object ... parameters) {
        return Context.messages().warn(this.code, parameters);
    }
}
