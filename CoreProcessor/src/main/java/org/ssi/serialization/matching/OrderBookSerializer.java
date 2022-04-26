package org.ssi.serialization.matching;

import org.ssi.CoreGlobalValue;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.io.BufferedChannel;
import org.ssi.matching.OrderBook;
import org.ssi.matching.OrderMap;
import org.ssi.matching.PriceLadder;
import org.ssi.model.Order;

import java.io.IOException;

public class OrderBookSerializer {

	public static void encode(OrderBook ob, BufferedChannel channel) throws IOException {
		//write market price
		channel.putLong(ob.marketPrice());
		
		//write base decimal
		channel.putInt(ob.tradeDecimals().baseDec);
		
		//write counter decimal
		channel.putInt(ob.tradeDecimals().counterDec);
		
		//write price decimal
		channel.putInt(ob.tradeDecimals().priceDec);
		
		//write ask ladder to file
		PriceLadderSerializer.encode(ob.askLadder(), channel);
		
		//write bid ladder to file
		PriceLadderSerializer.encode(ob.bidLadder(), channel);
		
		//write stop ask ladder to file
		PriceLadderSerializer.encode(ob.stopAskLadder(), channel);
		
		//write stop bid ladder to file
		PriceLadderSerializer.encode(ob.stopBidLadder(), channel);
		
		//write oco map to file
		encode(ob.ocoOrderMap(), channel);
	}
	
	public static void decode(OrderBook ob, BufferedChannel channel) throws Exception {
		//read market price
		long marketPrice = channel.getLong();
		ob.marketPrice(marketPrice);
		
		//read base decimal
		int baseDec = channel.getInt();		
		//read counter decimal
		int counterDec = channel.getInt();		
		//read price decimal
		int priceDec = channel.getInt();
		
		ob.tradeDecimals(baseDec, counterDec, priceDec);		
		
		//read ask ladder
		PriceLadder askLadder = new PriceLadder(CoreGlobalValue.MAX_LEVEL_NUM >> 1, true);
		PriceLadderSerializer.decode(askLadder, ob.orderMap(), ob.orderPool(), ob.bucketPool(), channel);
		ob.askLadder(askLadder);
		
		//read bid ladder
		PriceLadder bidLadder = new PriceLadder(CoreGlobalValue.MAX_LEVEL_NUM >> 1, false);
		PriceLadderSerializer.decode(bidLadder, ob.orderMap(), ob.orderPool(), ob.bucketPool(), channel);
		ob.bidLadder(bidLadder);
		
		//read stop ask ladder
		PriceLadder stopAskLadder = new PriceLadder(CoreGlobalValue.MAX_LEVEL_NUM >> 2, false);
		PriceLadderSerializer.decodeStopOrderLadder(stopAskLadder, ob.orderMap(), ob.orderPool(), ob.bucketPool(), channel);
		ob.stopAskLadder(stopAskLadder);
		
		//read stop bid ladder
		PriceLadder stopBidLadder = new PriceLadder(CoreGlobalValue.MAX_LEVEL_NUM >> 2, true);
		PriceLadderSerializer.decodeStopOrderLadder(stopBidLadder, ob.orderMap(), ob.orderPool(), ob.bucketPool(), channel);
		ob.stopBidLadder(stopBidLadder);
		
		decode(ob.ocoOrderMap(), ob.orderMap(), channel);
	}
	
	private static void encode(Long2ObjectMap<Order> ocoOrderMap, BufferedChannel channel) throws IOException {
		int size = ocoOrderMap.size();
		
		//write oco map size
		channel.putInt(size);
		
		long[] keys = ocoOrderMap.keys();
		Object[] values = ocoOrderMap.values();
		
		for(int i = 0; i < keys.length; i++) {
			if(values[i] != null) {
				channel.putLong(keys[i]);
				channel.putLong(((Order)values[i]).id);
			}
		}		
	}
	
	private static void decode(Long2ObjectMap<Order> ocoOrderMap, OrderMap orderMap, BufferedChannel channel) throws Exception {
		//read oco map size
		int size = channel.getInt();
		
		for(int i = 0; i < size; i++) {
			long orderId = channel.getLong();
			long ocoOrderId = channel.getLong();
			
			ocoOrderMap.put(orderId, orderMap.get(ocoOrderId));
		}		
	}
}
