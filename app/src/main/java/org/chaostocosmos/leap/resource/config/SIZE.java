package org.chaostocosmos.leap.resource.config;

/**
 * SIZE 
 * 
 * @author 9ins
 */
public enum SIZE {

    BYTE(SizeConstants.BYTE),
    KB(SizeConstants.KiB),
    MB(SizeConstants.MiB), 
    GB(SizeConstants.GiB),
    TB(SizeConstants.TiB),
    PB(SizeConstants.PiB),
    PCT(SizeConstants.PCT),
    CNT(SizeConstants.CNT);

    long byteSize;
    String unitString;

    SIZE(long byteSize) {
        this.byteSize = byteSize;
        this.unitString = this.name();
    }

    /**
     * Get amount by default fraction point
     * @param byteSize
     * @return
     */
    public double fromByte(long byteSize) {
        return fromByte(byteSize, SizeConstants.DEFAULT_FRACTION_POINT);
    }

    /**
     * Get amount with specified fraction point
     * @param byteSize
     * @param decimalPoint
     * @return
     */
    public double fromByte(long byteSize, int decimalPoint) {
        return fromByte((double) byteSize, decimalPoint);
    }

    /**
     * Get amount with specified fraction point
     * @param decimal
     * @param decimalPoint
     * @return
     */
    public double fromByte(double decimal, int decimalPoint) {
        return Math.round( (decimal / (double) this.byteSize) * Math.pow(10, decimalPoint) ) / Math.pow(10, decimalPoint);
    }

    /**
     * Get byte amount of parameters
     * @param decimal
     * @param sizeUnit
     * @return
     */
    public long toByte(double decimal) {
        return Math.round(this.byteSize * decimal);
    }

    /**
     * Convert number value to SIZE unit
     * @param decimal
     * @param toSizeUnit
     * @return
     */
    public double convert(double decimal, SIZE toSizeUnit) {
        return toSizeUnit.fromByte(toByte(decimal));
    }

    /**
     * Get unit size
     * @return
     */
    public long byteSize() {
        return this.byteSize;
    }

    /**
     * Percentage of a size of total 
     * @param total
     * @param byteSize
     * @param decimalPoint
     * @return
     */
    public double ratio(long total, long byteSize, int decimalPoint) {
        return Math.round(((byteSize / (double)total) * this.byteSize) * Math.pow(10, decimalPoint)) / Math.pow(10, decimalPoint);
    }
}
