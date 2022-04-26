package org.ssi.serialization.matching;

import org.ssi.collection.Long2ObjectMap;
import org.ssi.collection.ObjectPool;
import org.ssi.collection.TwoWayLongArray;
import org.ssi.io.BufferedChannel;
import org.ssi.matching.OrderBucket;
import org.ssi.matching.OrderMap;
import org.ssi.matching.PriceLadder;
import org.ssi.model.Order;

import java.io.IOException;

public class PriceLadderSerializer {
	
	public static void encode(PriceLadder priceLadder, BufferedChannel channel)
			throws IOException {
		
		TwoWayLongArray priceArr = priceLadder.priceArr();
		Long2ObjectMap<OrderBucket> bucketMap = priceLadder.bucketMap();
		
		int size = priceArr.size();
		//write size of price array
		channel.putInt(size);
		
		for(int i = 0; i < size; i++) {
			//write value of price
			long price = priceArr.at(i);
			channel.putLong(price);
			
			//write bucket
			OrderBucket bucket = bucketMap.get(price);
			
			if(bucket != null) {
				//write bucket length
				channel.putInt(bucket.length);
				
				//encode order
				Order start = bucket.head;				
				while(start != null) {
					OrderSerializer.encode(start, channel);
					start = start.nextOrder;
				}
			}
		}
	}
	
	public static void decode(PriceLadder ladder, OrderMap orderMap,
			ObjectPool<Order> orderPool, ObjectPool<OrderBucket> bucketPool, BufferedChannel channel) throws Exception {

		//read size of price array
		int size = channel.getInt();
		
		TwoWayLongArray priceArr = ladder.priceArr();
		Long2ObjectMap<OrderBucket> bucketMap = ladder.bucketMap();
		
		for(int i = 0; i < size; i++) {
			//read value of price
			long price = channel.getLong();
			OrderBucket bucket = bucketPool.allocateOrNew();
			bucket.init();
			bucket.ladder = ladder;
			
			//read bucket length
			int length = channel.getInt();
			for(int j = 0; j < length; j++) {
				Order order = orderPool.allocateOrNew();
				
				//decode order
				OrderSerializer.decode(order, channel);
				bucket.addOrder(order);
				orderMap.put(order.id, order);
			}
			
			priceArr.insertAfterTail(price);
			bucketMap.put(price, bucket);
		}
	}
	
	public static void decodeStopOrderLadder(PriceLadder ladder, OrderMap orderMap,
			ObjectPool<Order> orderPool, ObjectPool<OrderBucket> bucketPool, BufferedChannel channel) throws Exception {

		//read size of price array
		int size = channel.getInt();
		
		TwoWayLongArray priceArr = ladder.priceArr();
		Long2ObjectMap<OrderBucket> bucketMap = ladder.bucketMap();
		
		for(int i = 0; i < size; i++) {
			//read value of price
			long price = channel.getLong();
			OrderBucket bucket = bucketPool.allocateOrNew();
			bucket.init();
			bucket.ladder = ladder;
			
			//read bucket length
			int length = channel.getInt();
			for(int j = 0; j < length; j++) {
				Order order = orderPool.allocateOrNew();
				
				//decode order
				OrderSerializer.decode(order, channel);
				bucket.addOrder(order);
				
				orderMap.put(order.id, order);
			}
			
			priceArr.insertAfterTail(price);
			bucketMap.put(price, bucket);
		}
	}
}
