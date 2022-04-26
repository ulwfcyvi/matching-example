package org.ssi.util;

public class Hashing {
	
	public static final float DEFAULT_LOAD_FACTOR = 0.65f;
	
//	public static int hash(final long value, int mask) {
//        long hash = value * 31;
//        hash = (int)hash ^ (int)(hash >>> 32);
//
//        return (int)(hash & mask);
//    }
//
//	public static int hash(final int value, int mask)
//    {
//		final int hash = value * 31;
//
//        return hash & mask;
//    }
    /**
     * Generate a hash for an int value.
     *
     * @param value to be hashed.
     * @return the hashed value.
     */
    public static int hash(final int value)
    {
        int x = value;

        x = ((x >>> 16) ^ x) * 0x119de1f3;
        x = ((x >>> 16) ^ x) * 0x119de1f3;
        x = (x >>> 16) ^ x;

        return x;
    }

    /**
     * Generate a hash for a long value.
     *
     * @param value to be hashed.
     * @return the hashed value.
     */
    public static int hash(final long value)
    {
        long x = value;

        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        x = x ^ (x >>> 31);

        return (int)x ^ (int)(x >>> 32);
    }

    /**
     * Generate a hash for an int value and apply mask to get remainder.
     *
     * @param value to be hashed.
     * @param mask  mask to be applied that must be a power of 2 - 1.
     * @return the hash of the value.
     */
    public static int hash(final int value, final int mask)
    {
        return hash(value) & mask;
    }

    /**
     * Generate a hash for an object and apply mask to get a remainder.
     *
     * @param value to be hashed.
     * @param mask  mask to be applied that must be a power of 2 - 1.
     * @return the hash of the value.
     */
    public static int hash(final Object value, final int mask)
    {
        return hash(value.hashCode()) & mask;
    }

    /**
     * Generate a hash for a long value and apply mask to get a remainder.
     *
     * @param value to be hashed.
     * @param mask  mask to be applied that must be a power of 2 - 1.
     * @return the hash of the value.
     */
    public static int hash(final long value, final int mask)
    {
        return hash(value) & mask;
    }

    /**
     * Generate an even hash for an int value and apply mask to get a remainder that will be even.
     *
     * @param value to be hashed.
     * @param mask  mask to be applied that must be a power of 2 - 1.
     * @return the hash of the value which is always even.
     */
    public static int evenHash(final int value, final int mask)
    {
        final int hash = hash(value);
        final int evenHash = (hash << 1) - (hash << 8);

        return evenHash & mask;
    }

    /**
     * Generate an even hash for a long value and apply mask to get a remainder that will be even.
     *
     * @param value to be hashed.
     * @param mask  mask to be applied that must be a power of 2 - 1.
     * @return the hash of the value which is always even.
     */
    public static int evenHash(final long value, final int mask)
    {
        final int hash = hash(value);
        final int evenHash = (hash << 1) - (hash << 8);

        return evenHash & mask;
    }

    /**
     * Combined two 32-bit keys into a 64-bit compound.
     *
     * @param keyPartA to make the upper bits
     * @param keyPartB to make the lower bits.
     * @return the compound key
     */
    public static long compoundKey(final int keyPartA, final int keyPartB)
    {
        return ((long)keyPartA << 32) | (keyPartB & 0xFFFF_FFFFL);
    }
}
