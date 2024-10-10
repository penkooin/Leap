package org.chaostocosmos.leap.common.enums;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.constant.SizeConstants;

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
     * @param byteSize
     * @return
     */
    public double get(long byteSize) {
        return get(byteSize, Constants.DEFAULT_FRACTION_POINT);
    }

    /**
     * Get amount with specified fraction point
     * @param byteSize
     * @param decimalPoint
     * @return
     */
    public double get(long byteSize, int decimalPoint) {
        return get((double)byteSize, decimalPoint);
    }

    /**
     * Get amount with specified fraction point
     * @param byteSize
     * @param decimalPoint
     * @return
     */
    public double get(double byteSize, int decimalPoint) {
        return Math.round((byteSize / (double)this.size) * Math.pow(10, decimalPoint)) / Math.pow(10, decimalPoint);
    }

    /**
     * Get amount with unit string 
     * @param byteSize
     * @return
     */
    public String getWithUnit(long byteSize) {
        return get(byteSize) + " " + this.name();
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
