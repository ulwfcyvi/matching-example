package org.ssi.type;

public class MutualLong {
	private long value;
	
	public MutualLong(long v) {
		value = v;
	}
	
	public long get() {
		return value;
	}
	
	public void set(long v) {
		value = v;
	}
	
	public long increaseAndGet() {
		value++;
		return value;
	}
}
