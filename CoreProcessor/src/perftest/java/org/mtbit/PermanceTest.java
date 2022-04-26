package org.ssi;


import org.ssi.collection.ObjectPool;
import org.ssi.matching.OrderBook;
import org.ssi.matching.OrderBucket;
import org.ssi.matching.OrderMap;
import org.ssi.model.BaseEvent;
import org.ssi.model.Order;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.type.MutualLong;

public class PermanceTest {

	public static void main(String[] args) throws Exception {
		testPlaceAndRemoveOrder();
	}
	
	private static void testPlaceAndRemoveOrder() throws Exception {
		OrderMap oMap = new OrderMap(1 << 22);
		MutualLong oid = new MutualLong(0);
		
		ObjectPool<Order> oPool = new ObjectPool<Order>(1 << 21, Order::new);
		oPool.fillPool(1 << 21);
		
		ObjectPool<OrderBucket> bPool = new ObjectPool<OrderBucket>(1 << 20, OrderBucket::new);
		bPool.fillPool(1 << 20);
		
		ObjectPool<BaseEvent> ePool = new ObjectPool<BaseEvent>(1 << 15, BaseEvent::new);
		bPool.fillPool(1 << 15);
		
		OrderBook ob = new OrderBook(1 << 16, oid, oMap, oPool, bPool, ePool, null);
		
		BaseEvent event = createTradeEvent(0, 0, 5, OrderSide.ASK, OrderType.LIMIT);
		int id = 1;
		int loop = 1;
		
		long start = System.nanoTime();
		
		int[] orderIdArr = new int[20000];
		
		while(loop <= 50) {
			for(int i = 20000; i >= 1; i--) {
				event.price = i;
				id++;
				ob.placeOrder(event, true);
				orderIdArr[i - 1] = id;
			}
			
			for(int i = 20000; i >= 1; i--) {
				event.orderId = orderIdArr[20000 - i];
				ob.cancelOrder(event);				
			}
			
//			for(int i = 20000; i >= 1; i--) {
//				event.orderId = orderIdArr[i - 1];
//				ob.cancelOrder(event);				
//			}
			
//			for(int i = 10000; i >= 1; i--) {
//				event.orderId = orderIdArr[20000 - i];
//				ob.cancelOrder(event);
//				event.orderId = orderIdArr[i + 1];
//				ob.cancelOrder(event);				
//			}			
//			event.orderId = orderIdArr[1];
//			ob.cancelOrder(event);	
			
			loop++;
		}
		
		long end = System.nanoTime();
		System.out.println("Duration " + (end - start));
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
