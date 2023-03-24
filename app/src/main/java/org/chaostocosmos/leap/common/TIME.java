package org.chaostocosmos.leap.common;

import java.util.concurrent.TimeUnit;

/**
 * UNIT enum
 * 
 * @author 9ins
 */
public enum TIME  {  
    MICRO(TimeUnit.MICROSECONDS),
    MILLIS(TimeUnit.MILLISECONDS),
    SECOND(TimeUnit.SECONDS),
    MINUTE(TimeUnit.MINUTES),
    HOUR(TimeUnit.HOURS),
    DAY(TimeUnit.DAYS);

    TimeUnit timeUnit;

    /**
     * Init with timeunit
     * @param unit
     */
    TIME(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Get unit size
     * @return
     */
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    /**
     * Get full size of TimeUnit
     * @return
     */
    public long period(TimeUnit unit) {
        return unit.convert(1, this.timeUnit);
    }

    /**
     * Get milliseconds of this TimeUnit
     * @return
     */
    public long millis() {
        return this.timeUnit.toMillis(period(this.timeUnit));
    }

    /**
     * Get time by unit
     * @param duration
     * @param unit
     * @return
     */
    public long duration(long duration, TimeUnit unit) {
        if(!unit.name().equals("MICROSECONDS") 
            && !unit.name().equals("MILLISECONDS") 
            && !unit.name().equals("SECONDS") 
            && !unit.name().equals("MINUTES") 
            && !unit.name().equals("HOURS") 
            && !unit.name().equals("DAYS") 
        ) {
            throw new RuntimeException("This method is only support time unit.");
        }
        return unit.convert(duration, this.timeUnit);
    }
}

