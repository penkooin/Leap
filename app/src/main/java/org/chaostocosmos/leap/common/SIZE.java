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
    PCT(SizeConstants.PCT),
    CNT(SizeConstants.CNT);

    long size;

    SIZE(long size) {
        this.size = size;
    }

    /**
     * Get amount by default fraction point
     * @param amount
     * @return
     */
    public double get(long amount) {
        return get(amount, Constants.DEFAULT_FRACTION_POINT);
    }

    /**
     * Get amount with specified fraction point
     * @param amount
     * @param decimalPoint
     * @return
     */
    public double get(long amount, int decimalPoint) {
        return get((double)amount, decimalPoint);
    }

    /**
     * Get amount with specified fraction point
     * @param amount
     * @param decimalPoint
     * @return
     */
    public double get(double amount, int decimalPoint) {
        return Math.round((amount / (double)this.size) * Math.pow(10, decimalPoint)) / Math.pow(10, decimalPoint);
    }

    /**
     * Get unit size
     * @return
     */
    public long getUnitSize() {
        return this.size;
    }

    /**
     * Percentage of a size of total 
     * @param total
     * @param amount
     * @param decimalPoint
     * @return
     */
    public double ratio(long total, long amount, int decimalPoint) {
        return Math.round(((amount / (double)total) * this.size) * Math.pow(10, decimalPoint)) / Math.pow(10, decimalPoint);
    }
}
