package org.chaostocosmos.leap.http.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Utilities of Date / Time
 * 
 * @author 9ins
 */
public class DateUtils {
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
    public static String getCurrentDate(String zoneId) {
        return getDateString(System.currentTimeMillis(), zoneId);
    }

    /**
     * Get string of local zone date
     * @return
     */
    public static String getDateSystemZone() {
        return getDateString(System.currentTimeMillis(), ZoneId.systemDefault().getId());
    }
    
    /**
     * Get string of GMT date string by specified milliseconds and pattern
     * @param millis
     * @param zoneId
     * @return
     */
    public static String getDateString(long millis, String zoneId) {
        Date date = new Date(millis);
        Instant instant = date.toInstant();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(zoneId));
        return zdt.toString();
    }

    /**
     * Get GMT date string added with offset secondst 
     * @param offsetSeconds
     * @param zoneId
     * @return
     */
    public static String getDateAddedOffset(long offsetSeconds, String zoneId) {
        return getDateString(getMillis() + offsetSeconds * 1000, zoneId);
    }
}
