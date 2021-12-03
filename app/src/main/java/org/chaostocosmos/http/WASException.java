package org.chaostocosmos.http;

/**
 * WAS exception object
 */
public class WASException extends Exception {

    MSG_TYPE type;

    /**
     * Constructor with error code
     */
    public WASException(MSG_TYPE type, String code) {
        this(type, code, new Object[0]);
    }

    /**
     * Constructor with code and arguments
     * @param code
     * @param args
     */
    public WASException(MSG_TYPE type, String code, Object ... args) {
        System.out.println(type.name()+"   "+code+"   "+args);
        //this(type, code, args, new Exception(""));
    }

    /**
     * Constructor with code, arguments and caused exception
     * @param code
     * @param args
     */
    public WASException(MSG_TYPE type, String code, Object args, Throwable cause) {
        super(Context.getInstance().getMsg(type, code, args), cause);
        this.type = type;
    }  

    /**
     * Constructor with caused exception
     * @param cause
     */
    public WASException(Throwable cause) {
        super(cause);
    }
    
}
