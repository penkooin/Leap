package org.chaostocosmos.leap.http.commons;

import java.util.concurrent.TimeUnit;

import org.chaostocosmos.leap.common.TIME;

public class TimeTest {


    public static void main(String[] args) {
        System.out.println(TIME.DAY.duration(1, TimeUnit.SECONDS));
        System.out.println(TIME.SECOND.duration(10, TimeUnit.MILLISECONDS));
        long days = TimeUnit.DAYS.toSeconds(1);
        System.out.println(days);
    }
}
