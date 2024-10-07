package org.chaostocosmos.leap;

import java.util.Locale;
import java.util.TimeZone;

public class LocaleTest {


    public static void main(String[] args) {
        // Get time zone from its ID
        String timeZoneID = "Asia/Seoul";
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
        
        // Extract the region (second part of "Asia/Seoul")
        String[] parts = timeZoneID.split("/");
        String region = parts.length > 1 ? parts[1] : parts[0];  // Seoul in this case
        
        // Construct a Locale from the region
        Locale locale = new Locale("", region);  // "" for language, region for country
        
        System.out.println("Locale: " + locale);
        System.out.println("Country: " + locale.getDisplayCountry());  // Korea
    }
    
}
