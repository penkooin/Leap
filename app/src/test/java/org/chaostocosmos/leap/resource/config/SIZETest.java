package org.chaostocosmos.leap.resource.config;

import org.junit.Test;

public class SIZETest {
    @Test
    public void testByteSize() {
        System.out.println(SIZE.GB.fromByte(102400000000L));
        System.out.println(SIZE.GB.fromByte(500));
        System.out.println(SIZE.GB.fromByte(1024 * 1024 * 1024));        
    }

    @Test
    public void testFromByte() {

    }

    @Test
    public void testFromByte2() {

    }

    @Test
    public void testFromByte3() {

    }

    @Test
    public void convertTest() {
        double val = SIZE.GB.convert(2.0, SIZE.TB);
        System.out.println(val);
        val = SIZE.MB.convert(100, SIZE.KB);
        System.out.println(val);
    }

    @Test
    public void testFromString() {
        System.out.println("byte: "+SIZE.GB.toByte(1.0));
        String numString = "1.0";
    }

    @Test
    public void testRatio() {

    }

    @Test
    public void testToByte() {

    }

    @Test
    public void testValueOf() {

    }

    @Test
    public void testValues() {

    }
}
