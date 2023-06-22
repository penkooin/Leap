package org.chaostocosmos.leap.enums;

import org.chaostocosmos.leap.context.Context;

/**
 * HTTP status and code
 * 
 * @author 9ins
 */
public enum ERROR {    
    // 'Invalid request information. Wrong request'
    ERROR001(1),
    // 'Request or response build process failed. It could be HTTP protocol or SSL/TLS issue: {}'
    ERROR002(2),
    // 'Invalid header parameter: {}'
    ERROR003(3),
    // 'Specified type of response body object is not supported: {}'
    ERROR004(4),    
    // 'Can not represent trademark logo'
    ERROR005(5),
    // 'Service object mapping path must be exist: {}, Service object: {}'
    ERROR006(6),
    // 'Service operation method mapping path must be exist: {}, Service method: {}'
    ERROR007(7),
    // 'Service operation method already exist!!! Check service object mapping. Path: {} Operation method: {}'
    ERROR008(8),
    // 'Service mapper path expression is wrong: {}'
    ERROR009(9),
    // 'Method mapper path expression is wrong: {}'
    ERROR010(10),
    // 'Error in server setup process!!!'
    ERROR011(11),
    // 'Error is occurred on rquested file writing process. Path: {}' 
    ERROR012(12),
    // 'Cannot save configuration ({})'
    ERROR013(13),
    // 'Cannot read binary resource: {}'
    ERROR0(14),
    // 'Requested method is not matching with service: {}'
    ERROR015(15),
    // 'Invalid Password'
    ERROR016(16),
    // 'Password validation failed: (Password must have digit, lower case, upper case, special case at least once and no whilte space and more 8 places more)'
    ERROR017(17),
    // 'Not found mapping service exist with requested path: {}'
    ERROR018(18),
    // 'Deploy service process in error: {}'
    ERROR019(19),
    // 'Resource not found: {}'
    ERROR020(20),
    // 'Service method must have two parameter which is Request and Response. Called method: {}'
    ERROR021(21),
    // 'Can not read template file: {}'
    ERROR022(22),
    // 'Multi Part is in fault operation: {}'
    ERROR023(23),
    // 'Requested host not found in this server: {}'
    ERROR024(24),
    // 'User not found in this server. {}'
    ERROR025(25),
    // 'Requested method({}) not allowed. Supported GET / POST / PUT / DELETE'
    ERROR026(26),
    // 'Method not supported on MultiPart operation: readBody()'
    ERROR027(27),
    // 'Auth information not collect. {}'
    ERROR028(28),
    ERROR029(29),
    ERROR030(30);        
    
    /**
     * Error code
     */
    int code;

    /**
     * Initializer
     * @param code
     */
    ERROR(int code) {
        this.code = code;
    }

    /**
     * Get error code
     * @return
     */
    public int code() {
        return this.code;
    }

    /**
     * Get error message
     * @return
     */
    public String message() {
        return Context.get().messages().error(this.code);
    }

    /**
     * Get error message with parameters
     * @param parameters
     * @return
     */
    public String message(Object ... parameters) {
        return Context.get().messages().error(this.code, parameters);
    }
}

