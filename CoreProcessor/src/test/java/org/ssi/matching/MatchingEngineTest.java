package org.ssi.matching;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ssi.collection.Int2ObjectMap;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventResult;
import org.ssi.model.EventType;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.model.SymbolConfig;

public class MatchingEngineTest {
	
	@Test
	public void testHandleTradeEvent() throws Exception {
		SymbolConfig config = new SymbolConfig();
		config.symbol = 0;
		config.maxPriceLevel = 200;
		
		MatchingEngine engine = new MatchingEngine(1 << 10, 1 << 2, new SymbolConfig[] { config });
		BaseEvent event = createTradeEvent(1, 999, 999, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		Int2ObjectMap<OrderBook> orderBookMap = engine.orderBookMap();
		OrderBook orderBook = orderBookMap.get(0);

		Long2ObjectMap<OrderBucket> askBucketMap = orderBook.askLadder().bucketMap();
		assertTrue(orderBook.askLadder().size() == 1 && askBucketMap.size() == 1 && orderBook.askLadder().priceArr().at(0) == 999);		
	}
	
	@Test
	public void testHandleSymboleEvent() throws Exception {

		MatchingEngine engine = new MatchingEngine(1 << 10, 1 << 2, null);
		BaseEvent event = createTradeEvent(0, 0, 0, (byte)0, (byte)0, EventType.ADD_SYMBOL);
		event.symbol = 99;
		
		engine.handleEvent(event);
		
		Int2ObjectMap<OrderBook> orderBookMap = engine.orderBookMap();
		assertTrue(orderBookMap.size() == 1 &&  orderBookMap.get(99) != null);	
	}
	
	public static BaseEvent createTradeEvent(long id, long price, long amount, byte side, byte type, byte eventType) {
		BaseEvent event = new BaseEvent();
		event.orderId = id;
		event.price = price;
		event.amount = amount;
		event.orderSide = side;
		event.orderType = type;
		event.eventType = eventType;
		event.eventResult = EventResult.OK;
		return event;
	}
}
