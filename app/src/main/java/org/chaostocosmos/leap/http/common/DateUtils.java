package org.chaostocosmos.leap.http.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utilities of Date / Time
 * 
 * @author 9ins
 */
public class DateUtils {

    /**
     * Default date pattern
     */
    public static final String DEFAULT_DATE_PATTERN = "dd-MM-yyyy HH:mm:ss z";

    /**
     * Get current milliseconds
     * @return
     */
    public static long getCurrentMillis() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Get milliseconds from date string
     * @param dateString
     * @return
     */
    public static long getMillis(String dateString) {
        Instant instant = Instant.parse(dateString);
        return instant.toEpochMilli();
    }

    /**
     * Get string of GMT date current with DEFAULT_DATE_PATTERN
     * @return
     */
    public static String getDefaultCurrentDate() {
        return getCurrentDate(DEFAULT_DATE_PATTERN);
    }

    /**
     * Get string of GMT date string
     * @param pattern
     * @return
     */
    public static String getCurrentDate(String pattern) {
        return getDateGMT(System.currentTimeMillis(), pattern);
    }
    
    /**
     * Get string of GMT date string by specified milliseconds and pattern
     * @param millis
     * @param pattern
     * @return
     */
    public static String getDateGMT(long millis, String pattern) {
        Date date = new Date(millis);
        Instant instant = date.toInstant();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());        
        return zdt.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Get expire date added with offset seconds
     * @param offsetSeconds
     * @return
     */
    public static String getExpireOffset(long offsetSeconds) {
        return getDateGMT(getCurrentMillis() + offsetSeconds * 1000, DEFAULT_DATE_PATTERN);
    }

}
