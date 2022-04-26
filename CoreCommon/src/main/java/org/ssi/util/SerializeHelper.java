package org.ssi.util;

import java.io.IOException;

import org.ssi.collection.Int2DoubleMap;
import org.ssi.collection.Long2IntMap;
import org.ssi.collection.Long2LongMap;
import org.ssi.io.BufferedChannel;

public class SerializeHelper {
	
	public static void serializeInt2DoubleMap(BufferedChannel channel, Int2DoubleMap map) throws IOException {
		int size = map.size();
		channel.putInt(size);
		
		int[] keys = map.keys();
		double[] values = map.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i] != Int2DoubleMap.MISSING_VALUE) {
				channel.putInt(keys[i]);
				channel.putDouble(values[i]);
			}
		}
	}
	
	public static void deserializeInt2DoubleMap(BufferedChannel channel, Int2DoubleMap map) throws IOException {
		if (map == null) {
			map = new Int2DoubleMap();
		}
		int size = channel.getInt();
		
		for (int i = 0; i < size; i++) {
			int k = channel.getInt();
			double v = channel.getDouble();
			map.put(k, v);
		}
	}
	
	public static void serializeLong2LongMap(BufferedChannel channel, Long2LongMap map) throws IOException {
		int size = map.size();
		channel.putInt(size);
		
		long[] keys = map.keys();
		long[] values = map.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i] != Long2LongMap.MISSING_VALUE) {
				channel.putLong(keys[i]);
				channel.putLong(values[i]);
			}
		}
	}
	
	public static void deserializeLong2LongMap(BufferedChannel channel, Long2LongMap map) throws IOException {
		if (map == null) {
			map = new Long2LongMap();
		}
		int size = channel.getInt();
		
		for (int i = 0; i < size; i++) {
			long k = channel.getLong();
			long v = channel.getLong();
			map.put(k, v);
		}
	}
	
	public static void serializeLong2IntMap(BufferedChannel channel, Long2IntMap map) throws IOException {
		int size = map.size();
		channel.putInt(size);
		
		long[] keys = map.keys();
		int[] values = map.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i] != Long2IntMap.MISSING_VALUE) {
				channel.putLong(keys[i]);
				channel.putInt(values[i]);
			}
		}
	}
	
	public static void deserializeLong2IntMap(BufferedChannel channel, Long2IntMap map) throws IOException {
		if (map == null) {
			map = new Long2IntMap();
		}
		int size = channel.getInt();
		
		for (int i = 0; i < size; i++) {
			long k = channel.getLong();
			int v = channel.getInt();
			map.put(k, v);
		}
	}
}
