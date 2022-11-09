package org.chaostocosmos.leap.http;

public class Bits {

    public static String getBits(Integer a) { 
        String result = "";
        for(int i=Integer.BYTES-1; i>=0; i--) {
            result += getBits((byte)(a >>> (i * 8)) );
        }
        return result;
    }

    public static String getBits(byte i) {
        return (String.format("%"+Byte.SIZE+"s", Integer.toBinaryString(i))).replace(' ', '0');
    }

    public static byte calcXOR(byte a, byte b) {
        return (byte) (a ^ b);
    }

    public static byte calcBitwiseNOT(byte a) {
        return (byte) ~a;
    }

    public static int calcXOR(int a, int b) {
        return a ^ b;
    }

    public static int calcBitwiseNOT(int a) {
        return ~a;
    } 
    
    public static void main(String[] args) {
        for(byte i=0; i<10; i++) {
            System.out.println(i+"  "+getBits(i)+"   "+calcBitwiseNOT(i)+" ("+getBits(calcBitwiseNOT(i))+")");
        }

        for(byte i=0; i<10; i++) {
            System.out.println((i & 0xFF)+"  "+getBits(i & 10));
        }
        
        int left = 10;
        for(int i=1; i<=10; i++) {
            int right = i;
            String leftBits = getBits(left);
            String rightBits = getBits(right);
            int intVal = calcXOR(left, right);
            String result = getBits(intVal);
            System.out.println(left+"("+leftBits+") ^ "+rightBits+"("+rightBits +") = " + (left ^ right)+"("+result+")");
        }

        String nonillion  = "1000000000000000000000000000000";
        int charNum = nonillion.toCharArray().length;

    }
}
