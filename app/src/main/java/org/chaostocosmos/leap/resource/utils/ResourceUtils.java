package org.chaostocosmos.leap.resource.utils;

import java.util.regex.Matcher;

import org.chaostocosmos.leap.resource.config.SIZE;
import org.chaostocosmos.leap.resource.config.SizeConstants;

public class ResourceUtils {
 
    /**
     * Get byte size from number string
     * @param numericString
     * @return
     */
    public static long fromString(String numericString) {
        Matcher matcher = SizeConstants.NUMBER_PATTERN.matcher(numericString.trim());
        if (matcher.matches()) {
            String numberPart = matcher.group(1);  // Numeric part (can be integer or decimal)
            String unitPart = matcher.group(2);    // Unit part (e.g., MB, GB, kb)
            String[] numberUnit = new String[] {numberPart, unitPart};
            return  SIZE.valueOf(unitPart.toUpperCase()).byteSize();
        } else {
            throw new IllegalArgumentException("Invalid size format: " + numericString);
        }        
    }
    
}
