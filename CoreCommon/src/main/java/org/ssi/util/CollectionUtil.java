package org.ssi.util;

public class CollectionUtil {
	
	public static void validateLoadFactor(final float loadFactor) {
        if (loadFactor < 0.1f || loadFactor > 0.9f) {
            throw new IllegalArgumentException("Load factor must be in the range of 0.1 to 0.9: " + loadFactor);
        }
    }
}
