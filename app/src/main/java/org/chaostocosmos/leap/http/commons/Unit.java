package org.chaostocosmos.leap.http.commons;

import javax.transaction.NotSupportedException;

/**
 * Unit enum
 */
public enum UNIT {
    MS(1),
    SE(1000),
    MI(1000*60),
    HR(1000*60*60),
    DY(1000*60*60*24),
    WK(1000*60*60*24*7),
    MO(1000*60*60*24*30),
    YR(1000*60*60*24*30*365),
    BYTE(1),
    KB(1000),
    MB(1000*1000),
    GB(1000*1000*1000),
    TB(1000*1000*1000*1000),
    PB(1000*1000*1000*1000*1000),
    PCT(100),
    CNT(1);

    double unit;
    
    /**
     * Creation
     * @param unit
     */
    UNIT(long unit) {
        this.unit = (double)unit;
    }

    /**
     * Get unit size
     * @return
     */
    public double getUnit() {
        return this.unit;
    }

    /**
     * Get time by unit
     * @param time
     * @return
     * @throws NotSupportedException
     */
    public long get(long time) throws NotSupportedException {
        if(!name().equals("MS") 
            && !name().equals("SE") 
            && !name().equals("MI") 
            && !name().equals("HR") 
            && !name().equals("DY") 
            && !name().equals("WK") 
            && !name().equals("MO") 
            && !name().equals("YR")) {
            throw new NotSupportedException("This method is only support time unit.");
        }
        return (long)(this.unit * time);
    }

    /**
     * Get bytes with specified fraction point
     * @param bytes
     * @param fractionPoint
     * @return
     * @throws NotSupportedException
     */
    public double get(long bytes, int fractionPoint) throws NotSupportedException {        
        if(!name().equals("BYTE")
            && !name().equals("KB") 
            && !name().equals("MB") 
            && !name().equals("GB") 
            && !name().equals("TB") 
            && !name().equals("PB")) {
            throw new NotSupportedException("This method not supported to percent calculation.");
        }
        return Math.round((bytes / (double)this.unit) * Math.pow(10, fractionPoint)) / Math.pow(10, fractionPoint);
    }

    /**
     * Get 
     * @param total
     * @param bytes
     * @param franctionPoint
     * @return
     * @throws NotSupportedException
     */
    public double get(long total, long bytes, int franctionPoint) throws NotSupportedException {
        if(!name().equals("PER")) {
            throw new NotSupportedException("This method is only used for percent calculation");
        }
        return Math.round((total / (double)bytes) * unit * Math.pow(10, franctionPoint)) / Math.pow(10, franctionPoint);
    }

	/**
	 * Apply unit.
	 * @param value
	 * @param fractalPoint
	 * @return
	 */
	public double applyUnit(double value, int fractalPoint) {
		return (double)(Math.round(value / this.unit * Math.pow(10d, fractalPoint)) / Math.pow(10d, fractalPoint));
	}    

    public static void main(String[] args) throws Exception {
        System.out.println(Math.pow(10, 2));
        System.out.println(UNIT.YR.get(1));
    }
}

