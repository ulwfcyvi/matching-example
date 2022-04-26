package org.ssi.matching;

import org.ssi.CoreGlobalValue;
import org.ssi.collection.ConcurrentObjectPool;
import org.ssi.collection.Int2ObjectMap;
import org.ssi.collection.ObjectPool;
import org.ssi.event.TradeEventManager;
import org.ssi.io.BufferedChannel;
import org.ssi.model.*;
import org.ssi.serialization.matching.MatchingEngineSerializer;
import org.ssi.type.MutualLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class MatchingEngine {
	private static final Logger LOG = LoggerFactory.getLogger(MatchingEngine.class);
	
	private static final int MIN_OPEN_ORDER_NUM = 1 << 3;
	private static final int MAX_INTERNAL_EVENT_NUM = 1 << 15;
	private static final int BUFFER_SIZE = 2048 * 4096;
	
	private ObjectPool<Order> orderPool;
	private ObjectPool<OrderBucket> bucketPool;
	private OrderMap idToOrderMap;
	private MutualLong orderId;
	private ConcurrentObjectPool<BaseEvent> eventPool;
	
	private TradeEventManager eventManager;
	private Int2ObjectMap<OrderBook> orderBookMap;
	
	private ByteBuffer buffer;

	public MatchingEngine(int maxOpenOrderNum, int maxSymbolNum, SymbolConfig[] configArr) {
		if(maxOpenOrderNum < MIN_OPEN_ORDER_NUM) {
			throw new IllegalArgumentException("Open order num must be equal or greater than " + MIN_OPEN_ORDER_NUM);
		}
		
		orderId = new MutualLong(0);
		idToOrderMap = new OrderMap(maxOpenOrderNum << 1);
		
		orderPool = new ObjectPool<Order>(maxOpenOrderNum, Order::new);
		orderPool.fillPool(maxOpenOrderNum);
		
		bucketPool = new ObjectPool<OrderBucket>(maxOpenOrderNum >> 1, OrderBucket::new);
		bucketPool.fillPool(maxOpenOrderNum >> 1);
		
		eventPool = new ConcurrentObjectPool<BaseEvent>(MAX_INTERNAL_EVENT_NUM, BaseEvent::new);
		eventPool.fillPool(MAX_INTERNAL_EVENT_NUM);
		
		eventManager = new TradeEventManager(maxOpenOrderNum >> 1);
		
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		if(configArr != null) {
			orderBookMap = new Int2ObjectMap<OrderBook>(maxSymbolNum << 1);
			for(SymbolConfig config : configArr) {
				orderBookMap.put(config.symbol, new OrderBook(config.maxPriceLevel, orderId, idToOrderMap, orderPool, bucketPool, eventPool, eventManager));
			}
		} else {
			orderBookMap = new Int2ObjectMap<OrderBook>(maxSymbolNum << 1);
		}
	}
	
	//constructor for loading data from snapshot
	public MatchingEngine(int maxOpenOrderNum, int maxSymbolNum) {
		this(maxOpenOrderNum, maxSymbolNum, null);
	} 
	
	public ObjectPool<Order> orderPool() {
		return orderPool;
	}
	
	public ObjectPool<OrderBucket> bucketPool() {
		return bucketPool;
	}
	
	public ConcurrentObjectPool<BaseEvent> eventPool() {
		return eventPool;
	}
	
	public OrderMap orderMap() {
		return idToOrderMap;
	}
	
	public Int2ObjectMap<OrderBook> orderBookMap() {
		return orderBookMap;
	}
	
	public void orderBookMap(Int2ObjectMap<OrderBook> map) {
		orderBookMap = map;
	}
	
	public MutualLong orderId() {
		return orderId;
	}
	
	public TradeEventManager eventManager() {
		return eventManager;
	}
	
	public void handleEvent(BaseEvent event) throws Exception {
		
		if(event.eventResult != EventResult.OK) {
			return;
		}

		OrderBook ob = orderBookMap.get(event.symbol);
		switch(event.eventType) {
			case EventType.PLACE_ORDER:
				if(ob == null) {
					return;
				}
				ob.placeOrder(event, true);
				break;
			case EventType.CHANGE_ORDER:
				if(ob == null) {
					return;
				}
				ob.changeOrder(event);
				break;
			case EventType.CANCEL_ORDER:
				if(ob == null) {
					return;
				}
				ob.cancelOrder(event);
				break;
			case EventType.TAKE_SNAPSHOT:
//				saveSnapshot(file);
				return;
			case EventType.REPLAY_EVENTS:
//				loadSnapshot(file);
			case EventType.ADD_SYMBOL:

				if(ob == null) {
					ob = new OrderBook(CoreGlobalValue.MAX_LEVEL_NUM, orderId, idToOrderMap, orderPool, bucketPool, eventPool, eventManager);
					orderBookMap.put(event.symbol, ob);
				}
				
				int baseDigit = event.orderSide;
				int counterDigit = event.orderType;
				int priceDigit = event.tradeType;
				
				ob.tradeDecimals(baseDigit, counterDigit, priceDigit);
				
				return;
			default:
				break;
		}
		

		

		
//		// Do not do matching order when an order has POST_ONLY option
//		if(event.tradeOption == TradeOption.POST_ONLY && (event.eventType == EventType.PLACE_ORDER || event.eventType == EventType.CHANGE_ORDER)) {
//			if(ob.isMatchingPrice(event.orderSide, event.price)) {
//				event.eventResult = EventResult.ORDER_REJECT;
//				return;
//			}
//		}
////
//		switch(event.eventType) {
//
//			default:
//				break;
//		}
	}	

	
	public boolean equals(Object o) {
		
		if(o == this) {
			return true;
		}
		
		if(!(o instanceof MatchingEngine)) {
			return false;
		}
		
		MatchingEngine engine = (MatchingEngine)o;		
		
		return 	orderBookMap().equals(engine.orderBookMap()) 
				&& orderId().get() == engine.orderId().get()
				&& orderMap().equals(engine.orderMap());
	}
	
	public void saveSnapshot(String filePath) throws IOException {
		try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());		
		
			MatchingEngineSerializer.encode(this, channel);
			channel.flush();
		}
	}
	
	public void loadSnapshot(String filePath) throws Exception {
		try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());		
		
			MatchingEngineSerializer.decode(this, channel);
			channel.flush();
		}
	}
}
