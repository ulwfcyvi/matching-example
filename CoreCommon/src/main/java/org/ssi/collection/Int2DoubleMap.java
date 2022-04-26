package org.ssi.collection;

import static org.ssi.util.BitUtil.findNextPositivePowerOfTwo;
import static org.ssi.util.CollectionUtil.validateLoadFactor;

import java.util.Arrays;

import org.ssi.util.Hashing;

public class Int2DoubleMap {
	private static final int MIN_CAPACITY = 128;
	public static final double MISSING_VALUE = Double.MIN_VALUE;

	private final float loadFactor;
	private int resizeThreshold;
	private int size;

	private int[] keys;
	private double[] values;

	public Int2DoubleMap() {
		this(MIN_CAPACITY, Hashing.DEFAULT_LOAD_FACTOR);
	}

	public Int2DoubleMap(final int initialCapacit) {
		this(initialCapacit, Hashing.DEFAULT_LOAD_FACTOR);
	}

	public Int2DoubleMap(final int initialCapacity, final float loadFactor) {
		validateLoadFactor(loadFactor);

		this.loadFactor = loadFactor;

		final int capacity = findNextPositivePowerOfTwo(Math.max(MIN_CAPACITY, initialCapacity));
		resizeThreshold = (int) (capacity * loadFactor);

		keys = new int[capacity];
		values = new double[capacity];

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

	public boolean containsKey(final int key) {
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

	public double get(final int key) {
		final int mask = values.length - 1;
		int index = Hashing.hash(key, mask);

		double value;
		while (MISSING_VALUE != (value = values[index])) {
			if (key == keys[index]) {
				break;
			}

			index = ++index & mask;
		}

		return value;
	}

	public double put(final int key, final double value) {
		double oldValue = MISSING_VALUE;
		final int mask = values.length - 1;
		int index = Hashing.hash(key, mask);

		while (MISSING_VALUE != values[index]) {
			if (key == keys[index]) {
				oldValue = values[index];
				break;
			}

			index = ++index & mask;
		}

		if (MISSING_VALUE == oldValue) {
			++size;
			keys[index] = key;
		}

		values[index] = value;

		if (size > resizeThreshold) {
			increaseCapacity();
		}

		return oldValue;
	}

	private void increaseCapacity() {
		final int newCapacity = values.length << 1;
		if (newCapacity < 0) {
			throw new IllegalStateException("Max capacity reached at size = " + size);
		}

		rehash(newCapacity);
	}

	private void rehash(final int newCapacity) {
		final int mask = newCapacity - 1;
		resizeThreshold = (int) (newCapacity * loadFactor);

		final int[] tempKeys = new int[newCapacity];
		final double[] tempValues = new double[newCapacity];

		Arrays.fill(tempValues, MISSING_VALUE);

		for (int i = 0, size = values.length; i < size; i++) {
			final double value = values[i];
			if (MISSING_VALUE != value) {
				final int key = keys[i];
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

	public double remove(final int key) {
		final int mask = values.length - 1;
		int index = Hashing.hash(key, mask);

		double value;
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

	private void compactChain(int deleteIndex) {
		final int mask = values.length - 1;
		int index = deleteIndex;
		while (true) {
			index = ++index & mask;
			if (MISSING_VALUE == values[index]) {
				break;
			}

			final int hash = Hashing.hash(keys[index], mask);

			if ((index < hash && (hash <= deleteIndex || deleteIndex <= index))
					|| (hash <= deleteIndex && deleteIndex <= index)) {
				keys[deleteIndex] = keys[index];
				values[deleteIndex] = values[index];

				values[index] = MISSING_VALUE;
				deleteIndex = index;
			}
		}
	}

	public boolean equals(final Object o) {
		if (!(o instanceof Int2DoubleMap)) {
			return false;
		}

		Int2DoubleMap map2 = (Int2DoubleMap) o;
		if (this == map2) {
			return true;
		}

		if (size != map2.size()) {
			return false;
		}

		for (int i = 0, length = values.length; i < length; i++) {
			final double thisValue = values[i];
			if (MISSING_VALUE != thisValue) {
				final double thatValue = map2.get(keys[i]);
				if (thatValue == MISSING_VALUE || thisValue != thatValue) {
					return false;
				}
			}
		}

		return true;
	}

	public void clear() {
		if (size > 0) {
			Arrays.fill(values, MISSING_VALUE);
			size = 0;
		}
	}

	public int[] keys() {
		return keys;
	}

	public double[] values() {
		return values;
	}
}
