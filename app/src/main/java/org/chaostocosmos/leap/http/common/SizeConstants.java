package org.chaostocosmos.leap.http.common;

/**
 * Size constants
 * 
 * @author 9ins
 */
public interface SizeConstants {
    /**
     * Byte
     */
    public static final long BYTE = 1L;

    /**
     * Kilo byte
     */
    public static final long KiB = BYTE << 10;

    /**
     * Mega byte
     */
    public static final long MiB = KiB << 10;

    /**
     * Giga byte
     */
    public static final long GiB = MiB << 10;

    /**
     * Tera byte
     */
    public static final long TiB = GiB << 10;

    /**
     * Peta byte
     */
    public static final long PiB = TiB << 10;

    /**
     * Exa byte
     */
    public static final long EiB = PiB << 10;        

    /**
     * Percentage
     */
    public static final long PERCENTAGE = 100L;
}
