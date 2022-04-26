package org.ssi.serialization.matching;

import org.ssi.collection.Int2ObjectMap;
import org.ssi.io.BufferedChannel;
import org.ssi.matching.MatchingEngine;
import org.ssi.matching.OrderBook;
import org.ssi.type.MutualLong;

import java.io.IOException;

public class MatchingEngineSerializer {
	
	public static void encode(MatchingEngine engine, BufferedChannel channel) throws IOException {
		Int2ObjectMap<OrderBook> orderBookMap = engine.orderBookMap();
		MutualLong orderId = engine.orderId();
		
		//write last id of order
		channel.putLong(orderId.get());
		
		int size = orderBookMap.size();
		
		//write number of symbol
		channel.putInt(size);
		
		int[] keys = orderBookMap.keys();
		Object[] values = orderBookMap.values();
		
		for(int i = 0 ; i < values.length; i++) {
			if(values[i] != null) {
				//write symbol
				channel.putInt(keys[i]);							
				OrderBookSerializer.encode((OrderBook)values[i], channel);
			}
		}
	}
	
	public static void decode(MatchingEngine engine, BufferedChannel channel) throws Exception {
				
		MutualLong orderId = engine.orderId();
		
		//read last id of order
		long id = channel.getLong();
		orderId.set(id);
		
		int size = channel.getInt();
		Int2ObjectMap<OrderBook> orderbookMap = new Int2ObjectMap<OrderBook>(size << 2);
		
		for(int i = 0; i < size; i++) {
			//read symbol
			int symbol = channel.getInt();
			
			OrderBook ob = new OrderBook(engine.orderId(), engine.orderMap(), 
					engine.orderPool(), engine.bucketPool(), engine.eventPool(), engine.eventManager());
			
			OrderBookSerializer.decode(ob, channel);
			
			orderbookMap.put(symbol, ob);			
		}
		
		engine.orderBookMap(orderbookMap);
	}
}
