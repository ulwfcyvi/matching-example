package org.ssi.collection;

import static org.ssi.util.BitUtil.findNextPositivePowerOfTwo;
import static org.ssi.util.CollectionUtil.validateLoadFactor;

import java.util.Arrays;

import org.ssi.util.Hashing;

public class Long2LongMap {	
	private static final int MIN_CAPACITY = 32;
	public static final long MISSING_VALUE = Long.MIN_VALUE;
	
	private final float loadFactor;
	private int resizeThreshold;
	private int size;	
	
	private long[] keys;
	private long[] values;
	
	public Long2LongMap()
    {
        this(MIN_CAPACITY, Hashing.DEFAULT_LOAD_FACTOR);
    }
	
	public Long2LongMap(final int initialCapacit) {
		this(initialCapacit, Hashing.DEFAULT_LOAD_FACTOR);
	}
	
	public Long2LongMap(
	        final int initialCapacity,
	        final float loadFactor) {
        validateLoadFactor(loadFactor);

        this.loadFactor = loadFactor;

        final int capacity = findNextPositivePowerOfTwo(Math.max(MIN_CAPACITY, initialCapacity));
        resizeThreshold = (int)(capacity * loadFactor);

        keys = new long[capacity];
        values = new long[capacity];
        
        Arrays.fill(values, MISSING_VALUE);
    }
	
	public float loadFactor() {
        return loadFactor;
    }
	
	public int capacity() {
        return values.length;
    }
	
	public int size() {
		return size;
	}
	
	public int resizeThreshold() {
        return resizeThreshold;
    }
	
	public boolean isEmpty() {
        return 0 == size;
    }
	
	 public boolean containsKey(final long key) {
        final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        boolean found = false;
        while (MISSING_VALUE != values[index]) {
            if (key == keys[index]) {
                found = true;
                break;
            }

            index = ++index & mask;
        }

        return found;
	}
	
	public long get(final long key) {
		final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        long value;
        while (MISSING_VALUE != (value = values[index])) {
            if (key == keys[index]) {
                break;
            }

            index = ++index & mask;
        }
        
        return value;
    }
	
	public long put(final long key, final long value) {
		long oldValue = MISSING_VALUE;
        final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        while (MISSING_VALUE != values[index]) {
            if (key == keys[index]) {
                oldValue = values[index];
                break;
            }

            index = ++index & mask;
        }
        //co key cu roi thi ko phai tang size len nua, chi can gan value
        if (MISSING_VALUE == oldValue)
        {
            ++size;
            keys[index] = key;
        }

        values[index] = value;

        if (size > resizeThreshold)
        {
            increaseCapacity();
        }
        
        return oldValue;
	}
	
	private void increaseCapacity()
    {
        final int newCapacity = values.length << 1;
        if (newCapacity < 0) {
            throw new IllegalStateException("Max capacity reached at size = " + size);
        }

        rehash(newCapacity);
    }
	
	private void rehash(final int newCapacity) {
        final int mask = newCapacity - 1;
        resizeThreshold = (int)(newCapacity * loadFactor);

        final long[] tempKeys = new long[newCapacity];
        final long[] tempValues = new long[newCapacity];
        
        Arrays.fill(tempValues, MISSING_VALUE);

        for (int i = 0, size = values.length; i < size; i++) {
            final long value = values[i];
            if (MISSING_VALUE != value) {
                final long key = keys[i];
                int index = Hashing.hash(key, mask);
                
                while (MISSING_VALUE != tempValues[index]) {
                    index = ++index & mask;
                }

                tempKeys[index] = key;
                tempValues[index] = value;
            }
        }

        keys = tempKeys;
        values = tempValues;
    }
	
	public long remove(final long key) {
        final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        long value;
        while (MISSING_VALUE != (value = values[index])) {
        	
            if (key == keys[index]) {
                values[index] = MISSING_VALUE;
                --size;

                compactChain(index);
                break;
            }

            index = ++index & mask;
        }

        return value;
    }
	
	private void compactChain(int deleteIndex)
    {
        final int mask = values.length - 1;
        int index = deleteIndex;
        while (true)
        {
            index = ++index & mask;
            if (MISSING_VALUE == values[index])
            {
                break;
            }

            final int hash = Hashing.hash(keys[index], mask);

            if ((index < hash && (hash <= deleteIndex || deleteIndex <= index)) ||
                (hash <= deleteIndex && deleteIndex <= index))
            {
                keys[deleteIndex] = keys[index];
                values[deleteIndex] = values[index];

                values[index] = MISSING_VALUE;
                deleteIndex = index;
            }
        }
    }

	public boolean equals(final Object o)
    {
		if(!(o instanceof Long2LongMap)) {
			return false;
		}
		
		Long2LongMap map2 = (Long2LongMap)o;
        if (this == map2)
        {
            return true;
        }

        if (size != map2.size())
        {
            return false;
        }

        for (int i = 0, length = values.length; i < length; i++)
        {
            final long thisValue = values[i];
            if (MISSING_VALUE != thisValue)
            {
                final long thatValue = map2.get(keys[i]);
                if (thatValue == MISSING_VALUE || thisValue != thatValue)
                {
                    return false;
                }
            }
        }

        return true;
    }
	
	public void clear()
    {
        if (size > 0)
        {
            Arrays.fill(values, MISSING_VALUE);
            size = 0;
        }
    }
	
	public long[] keys() {
		return keys;
	}
	
	public long[] values() {
		return values;
	}
}
