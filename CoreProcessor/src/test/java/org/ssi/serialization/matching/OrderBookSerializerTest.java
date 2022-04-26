package org.ssi.serialization.matching;

import static org.junit.Assert.assertTrue;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.ssi.collection.ConcurrentObjectPool;
import org.ssi.collection.ObjectPool;
import org.ssi.io.BufferedChannel;
import org.ssi.matching.OrderBook;
import org.ssi.matching.OrderBucket;
import org.ssi.matching.OrderMap;
import org.ssi.model.BaseEvent;
import org.ssi.model.Order;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.model.TradeOption;
import org.ssi.type.MutualLong;

public class OrderBookSerializerTest {
	
	@Test
	public void testEncodAndDecode() throws Exception {
		//==========================encode=====================================
		OrderMap oMap = new OrderMap(1 << 4);
		MutualLong oId = new MutualLong(0);
		
		ObjectPool<Order> oPool = new ObjectPool<Order>(1 << 5, Order::new);
		oPool.fillPool(1 << 4);
		
		ObjectPool<OrderBucket> bPool = new ObjectPool<OrderBucket>(1 << 3, OrderBucket::new);
		bPool.fillPool(1 << 3);
		
		ConcurrentObjectPool<BaseEvent> ePool = new ConcurrentObjectPool<BaseEvent>(1 << 3, BaseEvent::new);
		ePool.fillPool(1 << 3);
		
		OrderBook ob = new OrderBook(1 << 3, oId, oMap, oPool, bPool, ePool, null);
		
		ob.tradeDecimals(1, 1, 1);
		
		//ask order
		BaseEvent event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 997, 1, OrderSide.ASK, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		//bid order
		event = createTradeEvent(0, 996, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 997, 1, OrderSide.BID, OrderType.LIMIT);
		ob.placeOrder(event, true);
		
		assertTrue(ob.marketPrice() == 997);		
		
		//stop ask order
		event = createTradeEvent(0, 0, 1, OrderSide.ASK, OrderType.STOP_MARKET);
		event.stopPrice = 996;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.ASK, OrderType.STOP_MARKET);
		event.stopPrice = 995;
		ob.placeOrder(event, true);
		
		//stop limit ask order
		event = createTradeEvent(0, 995, 1, OrderSide.ASK, OrderType.STOP_LIMIT);
		event.stopPrice = 996;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 994, 1, OrderSide.ASK, OrderType.STOP_LIMIT);
		event.stopPrice = 995;
		ob.placeOrder(event, true);
		
		//stop bid order
		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.stopPrice = 998;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 0, 1, OrderSide.BID, OrderType.STOP_MARKET);
		event.stopPrice = 999;
		ob.placeOrder(event, true);
		
		//stop limit bid order
		event = createTradeEvent(0, 999, 1, OrderSide.BID, OrderType.STOP_LIMIT);
		event.stopPrice = 998;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 1000, 1, OrderSide.BID, OrderType.STOP_LIMIT);
		event.stopPrice = 999;
		ob.placeOrder(event, true);
		
		//oco order
		event = createTradeEvent(0, 994, 1, OrderSide.BID, OrderType.LIMIT);
		event.stopPrice = 1000;
		event.tradeOption = TradeOption.OCO;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 995, 1, OrderSide.BID, OrderType.LIMIT);
		event.stopPrice = 1001;
		event.tradeOption = TradeOption.OCO;
		ob.placeOrder(event, true);
		
		event = createTradeEvent(0, 999, 1, OrderSide.ASK, OrderType.LIMIT);
		event.stopPrice = 993;
		event.tradeOption = TradeOption.OCO;
		ob.placeOrder(event, true);
		event = createTradeEvent(0, 998, 1, OrderSide.ASK, OrderType.LIMIT);
		event.stopPrice = 992;
		event.tradeOption = TradeOption.OCO;
		ob.placeOrder(event, true);
		
		assertTrue(ob.stopAskLadder().size() == 4 && ob.stopBidLadder().size() == 4 && ob.ocoOrderMap().size() == 8);
		
		int size = 4096;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		RandomAccessFile file = new RandomAccessFile("orderbook.dat", "rw");
		
		BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());
		
		OrderBookSerializer.encode(ob, channel);
		channel.flush();
		
		file.close();
		
		//==========================decode=====================================
		
		//init empty order book
		oMap = new OrderMap(1 << 4);
		oId = new MutualLong(0);
		
		oPool = new ObjectPool<Order>(1 << 4, Order::new);
		oPool.fillPool(1 << 4);
		
		bPool = new ObjectPool<OrderBucket>(1 << 3, OrderBucket::new);
		bPool.fillPool(1 << 3);
		
		ePool = new ConcurrentObjectPool<BaseEvent>(1 << 3, BaseEvent::new);
		ePool.fillPool(1 << 3);
		
		OrderBook newOb = new OrderBook(oId, oMap, oPool, bPool, ePool, null);
		
		buffer = ByteBuffer.allocate(size);
		file = new RandomAccessFile("orderbook.dat", "rw");
		
		channel = new BufferedChannel(buffer, file.getChannel());
		
		OrderBookSerializer.decode(newOb, channel);
		file.close();

		assertTrue(ob.equals(newOb));
	}
	
	public static BaseEvent createTradeEvent(long id, long price, long amount, byte side, byte type) {
		BaseEvent event = new BaseEvent();
		event.orderId = id;
		event.price = price;
		event.amount = amount;
		event.orderSide = side;
		event.orderType = type;
		return event;
	}
}
