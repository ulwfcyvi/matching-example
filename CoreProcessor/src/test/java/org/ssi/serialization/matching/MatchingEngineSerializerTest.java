package org.ssi.serialization.matching;

import static org.junit.Assert.assertTrue;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.ssi.io.BufferedChannel;
import org.ssi.matching.MatchingEngine;
import org.ssi.matching.OrderBook;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventResult;
import org.ssi.model.EventType;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.model.SymbolConfig;

public class MatchingEngineSerializerTest {

	@Test
	public void testEncodeAndDecode() throws Exception {
		
		//===============================encode=================================
		MatchingEngine engine = new MatchingEngine(1 << 10, 1 << 2, getConfigArr());
		
		//ask order
		
		//symbol 0
		BaseEvent event = createTradeEvent(0, 0, 999, 999, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(0, 0,999, 1, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(0, 0, 998, 1, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(0, 0, 997, 1, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		//symbol 1
		event = createTradeEvent(1, 0, 999, 999, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(1, 0,999, 1, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(1, 0, 998, 1, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(1, 0, 997, 1, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		
		//bid order
		
		//symbol 0
		event = createTradeEvent(0, 0, 996, 1, OrderSide.BID, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(0, 0, 995, 1, OrderSide.BID, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(0, 0, 994, 1, OrderSide.BID, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		
		//symbol 1
		event = createTradeEvent(1, 0, 996, 1, OrderSide.BID, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(1, 0, 995, 1, OrderSide.BID, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);
		event = createTradeEvent(1, 0, 994, 1, OrderSide.BID, OrderType.LIMIT, EventType.PLACE_ORDER);
		engine.handleEvent(event);

		int size = 4096;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		RandomAccessFile file = new RandomAccessFile("engine.dat", "rw");
		
		BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());
		
		MatchingEngineSerializer.encode(engine, channel);
		channel.flush();
		
		file.close();
		
		//===============================decode=================================
		MatchingEngine newEngine = new MatchingEngine(1 << 10, 1 << 2);
		
		buffer = ByteBuffer.allocate(size);
		file = new RandomAccessFile("engine.dat", "rw");
		
		channel = new BufferedChannel(buffer, file.getChannel());
		
		MatchingEngineSerializer.decode(newEngine, channel);
		
		file.close();

		assertTrue(engine.equals(newEngine));
		
		OrderBook ob = newEngine.orderBookMap().get(0);
		assertTrue(ob.askLadder() == ob.askLadder().getBucket(999).ladder);
		
		//Fix bug: #MV-121
		assertTrue(ob.eventPool() == newEngine.eventPool());
	}
	
	private SymbolConfig[] getConfigArr() {
		SymbolConfig config1 = new SymbolConfig();
		config1.symbol = 0;
		config1.maxPriceLevel = 1 << 3;
		
		SymbolConfig config2 = new SymbolConfig();
		config2.symbol = 1;
		config2.maxPriceLevel = 1 << 3;
		
		SymbolConfig[] configArr = new SymbolConfig[] {config1, config2};
		
		return configArr;
	}
	
	public static BaseEvent createTradeEvent(int symbol, long id, long price, long amount, byte side, byte type, byte eventType) {
		BaseEvent event = new BaseEvent();
		event.orderId = id;
		event.symbol = symbol;
		event.price = price;
		event.amount = amount;
		event.orderSide = side;
		event.orderType = type;
		event.eventType = eventType;
		event.eventResult = EventResult.OK;
		
		return event;
	}
}
