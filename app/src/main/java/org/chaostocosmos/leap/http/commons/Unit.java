package org.chaostocosmos.leap.http.commons;

public enum Unit {

    BYTE(0),
    KB(1024),
    MB(1024*1000),
    GB(1024*1000*1000),
    TB(1024*1000*1000*1000),
    PB(1024*1000*1000*1000*1000);

    long bytes;
    
    Unit(long bytes) {
        this.bytes = bytes;        
    }

    public long getBytes() {
        return bytes;
    }

    public double KB() {
        return Math.round((this.bytes / 1000d) * 100d) / 100d;
    }    

    public double MB() {
        return Math.round((this.bytes / (1000d * 1000d)) * 100d) / 100d;
    }    

    public static double MB(long bytes, int pointDigit) {
        return Math.round((bytes / (1000d * 1000d)) * Math.pow(10, pointDigit)) / Math.pow(10, pointDigit);
    }
}

