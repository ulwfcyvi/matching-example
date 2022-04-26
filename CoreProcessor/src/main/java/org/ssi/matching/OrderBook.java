package org.ssi.matching;

import org.ssi.collection.ConcurrentObjectPool;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.collection.LongArray;
import org.ssi.collection.ObjectPool;
import org.ssi.collection.TwoWayLongArray;
import org.ssi.event.TradeEventManager;
import org.ssi.model.*;
import org.ssi.type.MutualLong;
import org.ssi.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

public class OrderBook {
	private static final Logger LOG = LoggerFactory.getLogger(OrderBook.class);
	
	private static final int MAX_PENDING_ORDER_NUM = 1 << 10;
	private static final int OCO_ORDER_NUM = 1 << 10;
	private static final long NO_PRICE = -1;
	
	private OrderMap idToOrderMap;
	private MutualLong orderId;
	
	private PriceLadder bidLadder;
	private PriceLadder askLadder;
	
	private PriceLadder stopBidLadder;
	private PriceLadder stopAskLadder;
	
	private Long2ObjectMap<Order> idToOcoOrderMap;
	
	private ObjectPool<Order> orderPool;
	private ObjectPool<OrderBucket> bucketPool;
	private ConcurrentObjectPool<BaseEvent> eventPool;
	
	private TradeEventManager eventManager;	
	private ArrayDeque<BaseEvent> pendingEventQueue;	
	private FilledResult filledResult;
	private TradeDecimals tradeDecimals;
	
	private long marketPrice = NO_PRICE;
	private double symbolScale = 1;
	
	public OrderBook(int maxPriceLevelNum, MutualLong id, OrderMap oMap, 
			ObjectPool<Order> oPool, ObjectPool<OrderBucket> bPool, ConcurrentObjectPool<BaseEvent> ePool, TradeEventManager eventMan) {
		idToOrderMap = oMap;
		orderPool = oPool;
		bucketPool = bPool;
		eventPool = ePool;		
		orderId = id;
		eventManager = eventMan;
		
		pendingEventQueue = new ArrayDeque<BaseEvent>(MAX_PENDING_ORDER_NUM);
		idToOcoOrderMap = new Long2ObjectMap<Order>(OCO_ORDER_NUM);
				
		bidLadder = new PriceLadder(maxPriceLevelNum >> 1, false);
		askLadder = new PriceLadder(maxPriceLevelNum >> 1, true);
		
		stopBidLadder = new PriceLadder(maxPriceLevelNum >> 2, true);
		stopAskLadder = new PriceLadder(maxPriceLevelNum >> 2, false);		
		
		filledResult = new FilledResult();
		tradeDecimals = new TradeDecimals();
	}
	
	//constructor for loading data from snapshot
	public OrderBook(MutualLong id, OrderMap oMap, 
			ObjectPool<Order> oPool, ObjectPool<OrderBucket> bPool, ConcurrentObjectPool<BaseEvent> ePool, TradeEventManager eventMan) {		
		idToOrderMap = oMap;
		orderPool = oPool;
		bucketPool = bPool;
		eventPool = ePool;
		orderId = id;
		eventManager = eventMan;
		
		pendingEventQueue = new ArrayDeque<BaseEvent>(MAX_PENDING_ORDER_NUM);
		idToOcoOrderMap = new Long2ObjectMap<Order>(OCO_ORDER_NUM);
		
		filledResult = new FilledResult();
		tradeDecimals = new TradeDecimals();
	}

	public PriceLadder bidLadder() {
		return bidLadder;
	}
	
	public void bidLadder(PriceLadder ladder) {
		bidLadder = ladder;
	}
	
	public PriceLadder askLadder() {
		return askLadder;
	}
	
	public void askLadder(PriceLadder ladder) {
		askLadder = ladder;
	}
	
	public PriceLadder stopBidLadder() {
		return stopBidLadder;
	}
	
	public void stopBidLadder(PriceLadder ladder) {
		stopBidLadder = ladder;
	}
	
	public PriceLadder stopAskLadder() {
		return stopAskLadder;
	}
	
	public void stopAskLadder(PriceLadder ladder) {
		stopAskLadder = ladder;
	}
	
	public double symbolScale() {
		return symbolScale;
	}
	
	public ConcurrentObjectPool<BaseEvent> eventPool() {
		return eventPool;
	}
	
	public void tradeDecimals(int baseDec, int counterDec, int priceDec) {		
		tradeDecimals.baseDec = baseDec;
		tradeDecimals.priceDec = priceDec;
		tradeDecimals.counterDec = counterDec;
		
		int aggreation = baseDec + priceDec - counterDec;
		
		symbolScale = Math.pow(10, aggreation);
		filledResult.baseCurrencyScale = Math.pow(10, baseDec);
	}
	
	public TradeDecimals tradeDecimals() {
		return tradeDecimals;
	}
	
	public long marketPrice() {
		return marketPrice;
	}
	
	public void marketPrice(long price) {
		marketPrice = price;
	}
	
	public OrderMap orderMap( ) {
		return idToOrderMap;
	}
	
	public ObjectPool<Order> orderPool() {
		return orderPool;
	}
	
	public ObjectPool<OrderBucket> bucketPool() {
		return bucketPool;
	}
	
	public Long2ObjectMap<Order> ocoOrderMap() {
		return idToOcoOrderMap;
	}

	public void placeOrder(BaseEvent event, boolean isExternalEvent) throws Exception {
		placeOrder(event, isExternalEvent, true);		
	}
	
	private void placeOrder(BaseEvent event, boolean isExternalEvent, boolean isNewOrder) throws Exception {
		event.eventResult = EventResult.ORDER_REJECT;	

		if(event.orderType == OrderType.LIMIT && event.tradeOption == TradeOption.POST_ONLY){
			if(isMatchingPrice(event.orderSide, event.price)) {
				return;
			}
		}
		
		if(isNewOrder) {
			event.orderId = orderId.increaseAndGet();
		}
		
		if(eventManager != null) {
			eventManager.currentEvent(event);
		}
		
		if(event.orderType == OrderType.FILL_OR_KILL){
			if(!checkFillOrKill(event.orderSide, event.price, event.amount)){
				if(eventManager != null) {
					eventManager.sendCancelEvent(event.amount);
				}
				event.eventResult = EventResult.OK;
				return;
			}
		}

		//stop order.
		if(event.isStopOrderEvent()) {
			Order order = createOrder(event);
			
			boolean ok = event.orderSide == OrderSide.ASK ? addNewStopAskOrder(order) : addNewStopBidOrder(order);
			if(ok) {
				event.eventResult = EventResult.OK;
				if(order.tradeOption == TradeOption.OCO) {
					Order limitOrder = idToOcoOrderMap.get(order.id);
					//place pair oco_limit_order_id -> oco_stop_order into map
					idToOcoOrderMap.put(limitOrder.id, order);
				}
			} else {
				if(order.tradeOption == TradeOption.OCO) {
					//remove added-before pair oco_stop_order_id -> limit_order
					idToOcoOrderMap.remove(event.orderId);
				}
			}
			return;
		}

		Order takerOrder = createOrder(event);
		
		//matching
		if(event.orderSide == OrderSide.ASK) {
			tryMatchAskOrder(takerOrder, filledResult);
		} else {
			tryMatchBidOrder(takerOrder, filledResult);
		}

		if(filledResult.filled > 0) {
			long oldMarketPrice = marketPrice;
			marketPrice = filledResult.newMarketPrice;
			
			//cancel OCO stop order when OCO limit order is matched
			cancelOcoStopOrder(filledResult);
			
			checkStopOrderActivated(oldMarketPrice, marketPrice);
		}
		
		//check matching result
		if(filledResult.filled == event.amount) {
			event.eventResult = EventResult.OK;
			orderPool.returnPool(takerOrder);
		} else {
			//cancel remain amount for market and IOC order.
			if(event.orderType == OrderType.MARKET
                    || event.orderType == OrderType.IMMEDIATE_OR_CANCEL) {
				if(eventManager != null) {
					eventManager.sendCancelEvent(takerOrder.amount - filledResult.filled);
				}
				orderPool.returnPool(takerOrder);
				event.eventResult = EventResult.OK;
				
			} else {
				if(takerOrder.side == OrderSide.ASK) {
					addNewAskOrder(takerOrder);
				} else {
					addNewBidOrder(takerOrder);
				}
				event.eventResult = EventResult.OK;
			}

			//add OCO stop order, if filled > 0 -> it does not add to stop list.
			if(isExternalEvent && event.tradeOption == TradeOption.OCO && filledResult.filled == 0) {
				BaseEvent internalEvent = eventPool.allocateOrNew();
				internalEvent.copy(event);
				//remove stop price from oco limit order
				event.stopPrice = 0;
				
				internalEvent.orderId = orderId.increaseAndGet();
				internalEvent.orderType = OrderType.STOP_MARKET;
				internalEvent.tradeOption = TradeOption.OCO;
				internalEvent.ignoreCheckBalance = true;
				internalEvent.eventResult = EventResult.OK;
				internalEvent.eventType = EventType.PLACE_ORDER;
				//set reserved amount
				internalEvent.price = 
						event.orderSide == OrderSide.BID ? MathUtils.ceil((event.amount / symbolScale) * event.price) : 0;
				
				//place pair oco_stop_order_id -> oco_limit_order into map
				idToOcoOrderMap.put(internalEvent.orderId, takerOrder);

				pendingEventQueue.add(internalEvent);
			} 

			event.eventResult = EventResult.OK;
		}

		//submit pending orders
		if(isExternalEvent && pendingEventQueue.size() > 0 ) {
			submitPendingEvents(event);			
		}
	}
	
	public void cancelOrder(BaseEvent event) throws Exception {
		event.eventResult = EventResult.ORDER_REJECT;
		
		if(eventManager != null) {
			eventManager.currentEvent(event);
		}
		
		Order order = idToOrderMap.get(event.orderId);
		
		if(order != null) {
			if(event.userId != order.userId) {
				return;
			}
			
			event.amount = order.amount - order.filled;
			event.price = order.price;
			event.stopPrice = order.stopPrice;
			event.symbol = order.symbol;
			event.orderSide = order.side;
			event.orderType = order.type;
			event.tradeOption = order.tradeOption;
			
			cancelOrder(order);
			
			if(order.tradeOption == TradeOption.OCO) {
				boolean ok = removeOrderPairInOcOMap(order.id);
				if(ok) {
					event.ignoreCheckBalance = true;
				}
			}
			
			//only attach matching result for cancel event when an order has already filled partially
			if(eventManager != null && order.filled > 0) {
				eventManager.sendCancelEvent(order);
			}
			
			event.eventResult = EventResult.OK;
		}
	}

	public void cancelOrder(Order order) throws Exception {
		//remove order from order bucket
		OrderBucket bucket = order.bucket;
		if(bucket != null){
			bucket.removeOrder(order);
			
			//remove bucket having amount 0
			if(bucket.amount == 0) {
				long price = (order.type == OrderType.STOP_LIMIT || order.type == OrderType.STOP_MARKET) ?
						order.stopPrice : order.price;
				removeBucket(price, bucket);
			}
		}

		//remove order book from orderMap then return it to pool
		removeOrder(order);

		order.status = OrderStatus.CANCELLED;
	}

	public void changeOrder(BaseEvent event) throws Exception {
		
		event.eventResult = EventResult.ORDER_REJECT;
		
		if(eventManager != null) {
			eventManager.currentEvent(event);
		}
		
		Order order = idToOrderMap.get(event.orderId);
		
		if(order != null) {			
			if(event.userId != order.userId) {
				return;
			}
			
			//price does not change
			if(order.price == event.price) {
				if(order.amount - order.filled != event.amount) {
					order.bucket.changeOrderAmount(order, event.amount, eventManager);
				}
			} else {
				long oldFilled = order.filled;
				
				//seperate change_order event into 2 events: cancel and place order
				BaseEvent placeEvent = eventPool.allocateOrNew();				
				placeEvent.copy(event);
				
				event.next = placeEvent;
				
				//remove order at old price level
				event.eventType = EventType.CANCEL_ORDER;
				cancelOrder(event);
			
				//place order at new price
				placeEvent.eventType = EventType.PLACE_ORDER;
				placeOrder(placeEvent, true, false);
				
				order = idToOrderMap.get(event.orderId);
				
				//order with new price does not match or match partially
				if(order != null) {
					order.amount += oldFilled;
					order.filled += oldFilled;
				}
			}
		}
	}
	
	public boolean isMatchingPrice(byte side, long price) throws Exception {
		if(side == OrderSide.ASK) {
			return bidLadder.isMatchingPrice(price);
		} else if (side == OrderSide.BID) {
			return askLadder.isMatchingPrice(price);
		}
		
		return false;
	}
	
	private void submitPendingEvents(BaseEvent event) throws Exception {
		
		BaseEvent lastEvent = event;
		
		while(pendingEventQueue.size() > 0) {

			BaseEvent internalEvent = pendingEventQueue.pollFirst();

			lastEvent.next = internalEvent;
			lastEvent = internalEvent;
			
			if(internalEvent.eventType == EventType.PLACE_ORDER) {
				if(event.orderId > 0) {
					placeOrder(internalEvent, false, false);
				} else {
					placeOrder(internalEvent, false, true);
				}
			} else if (internalEvent.eventType == EventType.CANCEL_ORDER) {
				cancelOrder(internalEvent);
			}
		}
	}
	
	private void addNewBidOrder(Order order) throws Exception {
	
		if(bidLadder.isOutOfRangePrice(order.price)) {
			OrderBucket bucket = createBucket(order);
			bidLadder.addBucket(order.price, bucket);
		} else {			
			OrderBucket bucket = bidLadder.getBucket(order.price);
			
			//price level has already existed
			if(bucket != null) {
				bucket.addOrder(order);
			} else {
				bucket = createBucket(order);
				bidLadder.addBucket(order.price, bucket);
			}
		}

		idToOrderMap.put(order.id, order);
	}
	
	private void addNewAskOrder(Order order) throws Exception {
			
		if(askLadder.isOutOfRangePrice(order.price)) {
			OrderBucket bucket = createBucket(order);
			askLadder.addBucket(order.price, bucket);
		} else {			
			OrderBucket bucket = askLadder.getBucket(order.price);
			
			//price level has already existed
			if(bucket != null) {
				bucket.addOrder(order);
			} else {
				bucket = createBucket(order);
				askLadder.addBucket(order.price, bucket);
			}
		}

		idToOrderMap.put(order.id, order);
	}
	
	private boolean addNewStopBidOrder(Order order) throws Exception {

		if(marketPrice >= 0) {
			if(order.stopPrice > marketPrice) {

				OrderBucket bucket = stopBidLadder.getBucket(order.stopPrice);
				
				//price level has already existed
				if(bucket != null) {
					bucket.addOrder(order);
				} else {
					bucket = createBucket(order);
					stopBidLadder.addBucket(order.stopPrice, bucket);
				}

				idToOrderMap.put(order.id, order);
				return true;
			}
		} 
		
		return false;
	}
	
	private boolean addNewStopAskOrder(Order order) throws Exception {
		
		if(marketPrice >= 0) {			
			if(order.stopPrice < marketPrice) {

				OrderBucket bucket = stopAskLadder.getBucket(order.stopPrice);

				//price level has already existed
				if(bucket != null) {
					bucket.addOrder(order);
				} else {
					bucket = createBucket(order);
					stopAskLadder.addBucket(order.stopPrice, bucket);
				}

				idToOrderMap.put(order.id, order);
				return true;
			}			
		}
		
		return false;
	}
	
	private long tryMatchBidOrder(Order takerOrder, FilledResult result) throws Exception {
		Long2ObjectMap<OrderBucket> orderBucketMap = askLadder.bucketMap();
		TwoWayLongArray priceLevelArr = askLadder.priceArr();
		
		int size = priceLevelArr.size();
		
		result.init();
		
		if(size == 0) {
			return result.filled;
		}
		
		long maxPrice = takerOrder.type == OrderType.MARKET ? Long.MAX_VALUE : takerOrder.price;
		long orderAmount = takerOrder.amount;
		long availBalance = 0;
		
		if(takerOrder.type == OrderType.MARKET) {
			availBalance = takerOrder.price;
		}		
		
		while(size > 0) {
			long levelPrice = priceLevelArr.at(0);
			if(maxPrice < levelPrice) {
				break;
			}
			
			long amountLeft = orderAmount - result.filled;
			
			if(takerOrder.type == OrderType.MARKET) {
				if(availBalance <= 0) {
					break;
				}
				long maxAmount = (long)(((double)availBalance / levelPrice) * symbolScale);
				amountLeft = Math.min(maxAmount, amountLeft);
			}
			
			OrderBucket bucket = orderBucketMap.get(levelPrice);
			long matched = bucket.match(amountLeft, result, idToOrderMap, orderPool, eventManager);
			
			if(matched > 0) {
				result.newMarketPrice = levelPrice;
				takerOrder.filled = result.filled;
				takerOrder.filledCost = result.filledCost;
				
				if(takerOrder.type == OrderType.MARKET) {
					//ceil up cost value
					availBalance -= MathUtils.ceil((matched / symbolScale) * levelPrice);
				}
			}
			
			if(bucket.amount == 0) {
				//remove bucket and price level associating with bucket
				size--;
				orderBucketMap.remove(levelPrice);
				bucketPool.returnPool(bucket);		
				priceLevelArr.removeAt(0);				
			} else {
				break;
			}
			
			if(result.filled == orderAmount) {
				break;
			}
		}

		return result.filled;
	}
	
	private long tryMatchAskOrder(Order takerOrder, FilledResult result) throws Exception {
		Long2ObjectMap<OrderBucket> orderBucketMap = bidLadder.bucketMap();
		TwoWayLongArray priceLevelArr = bidLadder.priceArr();
		
		int size = priceLevelArr.size();
		
		result.init();
		
		if(size == 0) {
			return result.filled;
		}
		
		long minPrice = takerOrder.type == OrderType.MARKET ? Long.MIN_VALUE : takerOrder.price;
		long orderAmount = takerOrder.amount;
		
		while(size > 0) {
			long levelPrice = priceLevelArr.at(0);
			if(minPrice > levelPrice) {
				break;
			}
			
			long amountLeft = orderAmount - result.filled;
			
			OrderBucket bucket = orderBucketMap.get(levelPrice);
			long matched = bucket.match(amountLeft, result, idToOrderMap, orderPool, eventManager);

			if(matched > 0) {
				result.newMarketPrice = levelPrice;
				takerOrder.filled = result.filled;
				takerOrder.filledCost = result.filledCost;
			}
			
			if(bucket.amount == 0) {
				//remove bucket and price level associating with bucket
				size--;
				orderBucketMap.remove(levelPrice);
				bucketPool.returnPool(bucket);
				priceLevelArr.removeAt(0);				
			} else {
				break;
			}
			
			if(result.filled == orderAmount) {
				break;
			}
		}

		return result.filled;
	}

    private boolean checkFillOrKill(byte orderSide, long price, long amount) throws Exception {

    	PriceLadder ladder = null;
    	
    	if(orderSide == OrderSide.BID) {
    		ladder = askLadder;
    	} else {
    		ladder = bidLadder;
    	}
    	
    	return ladder.isEnoughAmountAtRate(price, amount);
    }
    
    private void checkStopOrderActivated(long oldMarketPrice, long marketPrice) throws Exception {
    	if(oldMarketPrice != NO_PRICE && marketPrice != oldMarketPrice) {
			if(marketPrice < oldMarketPrice) { //price go down -> check if stop ask order is activated
				checkStopOrderActivated(marketPrice, false);
			} else { //price go up -> check if stop bid order is activated
				checkStopOrderActivated(marketPrice, true);
			}
		}
    }
	
	private void checkStopOrderActivated(long marketPrice, boolean isBidOrder) throws Exception {
		PriceLadder priceLadder = isBidOrder ? stopBidLadder : stopAskLadder;
		Long2ObjectMap<OrderBucket> orderBucketMap = priceLadder.bucketMap();
		TwoWayLongArray priceLevelArr = priceLadder.priceArr();
		
		int size = priceLevelArr.size();
		while(size > 0) {
			
			long price = priceLevelArr.at(0);
			if((isBidOrder && price > marketPrice) || (!isBidOrder && price < marketPrice)) {
				break;
			}
			
			OrderBucket bucket = orderBucketMap.get(price);
			Order head = bucket.head;
			
			while(head != null) {
				//cancel OCO limit order when OCO stop order is activate
				if(head.tradeOption == TradeOption.OCO) {
					Order ocoLimitOrder = idToOcoOrderMap.get(head.id);
					
					if(ocoLimitOrder != null) {
						BaseEvent internalEvent = eventPool.allocateOrNew();
						ocoLimitOrder.copyToCancelEvent(internalEvent);
						
						pendingEventQueue.addLast(internalEvent);
					}
				}
				
				BaseEvent internalEvent = eventPool.allocateOrNew();
				head.copyToEvent(internalEvent);
				
				pendingEventQueue.addLast(internalEvent);
				removeOrder(head);
				
				head = head.nextOrder;
			}
			
			size--;
			priceLadder.removeBucket(price);			
		}		
	}	

	private boolean removeOrderPairInOcOMap(long orderId) {
		Order orderOco = idToOcoOrderMap.remove(orderId);
		
		if(orderOco != null) {
			idToOcoOrderMap.remove(orderOco.id);
			return true;
		}
		return false;
	}
	
	private void cancelOcoStopOrder(FilledResult result) {
		//cancel OCO stop order when OCO limit order is matched
		for(int i = 0; i < result.orderOcoLimitArr.size(); i++) {
			Order ocoStopOrder = idToOcoOrderMap.get(result.orderOcoLimitArr.get(i));				
			if(ocoStopOrder != null) {
				BaseEvent internalEvent = eventPool.allocateOrNew();
				ocoStopOrder.copyToCancelEvent(internalEvent);

				pendingEventQueue.addLast(internalEvent);
			}
		}
	}

	private boolean removeBucket(long price, OrderBucket bucket) throws Exception {
		bucket.ladder.removeBucket(price);
		bucketPool.returnPool(bucket);
		
		return true;
	}

	private boolean removeOrder(Order order) {		
		idToOrderMap.remove(order.id);
		orderPool.returnPool(order);
		return true;
	}
	
	private OrderBucket createBucket(Order order) {
		OrderBucket bucket = bucketPool.allocateOrNew();	
		bucket.init();
		bucket.addOrder(order);

		return bucket;		
	}
	
	private Order createOrder(BaseEvent event) {
		Order order = orderPool.allocateOrNew();
		order.copyFromEvent(event);
		
		return order;
	}

    public boolean equals(Object o) {
		if(!(o instanceof OrderBook)) {
			return false;
		}
		
		OrderBook ob2 = (OrderBook)o;
	
		return marketPrice == ob2.marketPrice && tradeDecimals.equals(ob2.tradeDecimals)
				&& bidLadder.equals(ob2.bidLadder) && askLadder.equals(ob2.askLadder)
				&& stopBidLadder.equals(ob2.stopBidLadder) && stopAskLadder.equals(ob2.stopAskLadder) 
				&& idToOcoOrderMap.equals(ob2.idToOcoOrderMap);
	}	
	
	public static class FilledResult {
		public long filled;
		public double filledCost;
		public long newMarketPrice;
		public double baseCurrencyScale;
		public LongArray orderOcoLimitArr;
		
		public FilledResult() {
			orderOcoLimitArr = new LongArray();
		}
		
		public void init() {
			filled = 0;
			filledCost = 0;
			newMarketPrice = 0;
			orderOcoLimitArr.clear();
		}
	}
}
