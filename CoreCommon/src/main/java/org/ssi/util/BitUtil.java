package org.ssi.util;

public class BitUtil {
	private static final int MASK_16_BIT = (1 << 16) - 1;
	
	public static int findNextPositivePowerOfTwo(final int value)
    {
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(value - 1));
    }
	
	public static int getLow(final int value) {
		return value & MASK_16_BIT;
	}
	
	public static int getHigh(final int value) {
		return (value >> 16) & MASK_16_BIT;
	}
	
	public static int pack(int high, int low) {
		high = getLow(high);
		low = getLow(low);
		
		return (high << 16) | low;
	}
}
