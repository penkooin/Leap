package org.chaostocosmos.leap.common;

/**
 * SIZE 
 * 
 * @author 9ins
 */
public enum SIZE {

    BY(SizeConstants.BYTE),
    KB(SizeConstants.KiB),
    MB(SizeConstants.MiB),
    GB(SizeConstants.GiB),
    TB(SizeConstants.TiB),
    PB(SizeConstants.PiB),
    PERCENTAGE(SizeConstants.PERCENTAGE);

    long amount;

    SIZE(long amount) {
        this.amount = amount;
    }

    /**
     * Get amount by default fraction point
     * @param size
     * @return
     */
    public double get(long size) {
        return get(size, Constants.DEFAULT_FRACTION_POINT);
    }

    /**
     * Get amount with specified fraction point
     * @param size
     * @param decimalPoint
     * @return
     */
    public double get(long size, int decimalPoint) {
        return Math.round((size / (double)this.amount) * Math.pow(10, decimalPoint)) / Math.pow(10, decimalPoint);
    }

    /**
     * Percentage of a size of total 
     * @param total
     * @param size
     * @param decimalPoint
     * @return
     */
    public double ratio(long total, long size, int decimalPoint) {
        return Math.round(((size / (double)total) * this.amount) * Math.pow(10, decimalPoint)) / Math.pow(10, decimalPoint);
    }
}
