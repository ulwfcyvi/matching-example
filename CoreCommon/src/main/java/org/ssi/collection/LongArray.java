package org.ssi.collection;

public class LongArray {
	private static int INITIAL_CAPACITY = 16;
	private static int INCREMENT = 8;
	
	private long[] data;
	private PaddedInt count;
	
	public LongArray() {
		this(INITIAL_CAPACITY);
	}
	
	public LongArray(int capacity) {
		data = new long[capacity];
		count = new PaddedInt();
	}
	
	public void add(long value) {
		if(count.value == data.length) {
			increaseSize();
		}
		
		data[count.value] = value;
		count.value++;
	}
	
	public long get(int index) {
		return data[index];
	}
	
	private void increaseSize() {
		int capacity = data.length + INCREMENT;
		
		long[] newData = new long[capacity];		
		System.arraycopy(data, 0, newData, 0, count.value);
		
		data = newData;
	}

	public void clear() {
		count.value = 0;
	}
	
	public int size() {
		return count.value;
	}
	
	public int capacity() {
		return data.length;
	}	
}
