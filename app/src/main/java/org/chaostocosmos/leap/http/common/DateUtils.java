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
     * Local zone date pattern
     */
    public static final String ZONE_DATE_PATTERN = "dd-MM-yyyy HH:mm:ss Z";

    /**
     * GMT date pattern
     */
    public static final String GMT_DATE_PATTERN = "dd-MM-yyyy HH:mm:ss 'GMT'Z '('z')'";

    /**
     * Default date pattern
     */
    public static final String DEFAULT_DATE_PATTERN = ZONE_DATE_PATTERN;

    /**
     * Get current milliseconds
     * @return
     */
    public static long getMillis() {
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
    public static String getDefaultDate() {
        return getDateString(System.currentTimeMillis(), DEFAULT_DATE_PATTERN);
    }

    /**
     * Get string of GMT date string
     * @param pattern
     * @return
     */
    public static String getDateGMT() {
        return getDateString(System.currentTimeMillis(), GMT_DATE_PATTERN);
    }

    /**
     * Get string of local zone date
     * @return
     */
    public static String getDateLocalZone() {
        return getDateString(System.currentTimeMillis(), ZONE_DATE_PATTERN);
    }
    
    /**
     * Get string of GMT date string by specified milliseconds and pattern
     * @param millis
     * @param pattern
     * @return
     */
    public static String getDateString(long millis, String pattern) {
        Date date = new Date(millis);
        Instant instant = date.toInstant();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());        
        return zdt.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Get GMT date string added with offset secondst 
     * @param offsetSeconds
     * @return
     */
    public static String getDateGMTAddedOffset(long offsetSeconds) {
        return getDateString(getMillis() + offsetSeconds * 1000, GMT_DATE_PATTERN);
    }

    /**
     * Get local date string added with offset seconds
     * @param offsetSeconds
     * @return
     */
    public static String getDateLocalAddedOffset(long offsetSeconds) {
        return getDateString(getMillis() + offsetSeconds * 1000, DEFAULT_DATE_PATTERN);
    }

}
