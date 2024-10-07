package org.chaostocosmos.leap.resource.utils;

import java.util.regex.Matcher;

import org.chaostocosmos.leap.resource.config.SIZE;
import org.chaostocosmos.leap.resource.config.SizeConstants;

/**
 * ResourceUtils
 * 
 * @author 9ins
 */
public class ResourceUtils {
 
    /**
     * Get byte size from number string
     * @param numericString
     * @return
     */
    public static long fromString(String numericString) {
        Matcher matcher = SizeConstants.NUMBER_PATTERN.matcher(numericString.trim());
        if (matcher.matches()) {
            //String numberPart = matcher.group(1);  // Numeric part (can be integer or decimal)
            //String[] numberUnit = new String[] {numberPart, unitPart};
            String unitPart = matcher.group(2);    // Unit part (e.g., MB, GB, kb)
            return  SIZE.valueOf(unitPart.toUpperCase()).byteSize();
        } else {
            throw new IllegalArgumentException("Invalid size format: " + numericString);
        }        
    }
    
}
