package org.ssi.matching;

import org.junit.Test;
import org.ssi.collection.ObjectPool;
import org.ssi.model.Order;

import static org.junit.Assert.assertTrue;


public class OrderBucketTest {
	
	@Test
	public void testInsert() {
		OrderBucket bucket = new OrderBucket();
		
		bucket.addOrder(createOrder(1, 1, 1, 0));
		bucket.addOrder(createOrder(2, 1, 2, 1));
		bucket.addOrder(createOrder(3, 1, 3, 1));
		bucket.addOrder(createOrder(4, 1, 4, 1));
		
		assertTrue(bucket.head.id == 1 && bucket.tail.id == 4 && bucket.length == 4 && bucket.amount == 7);
	}
	
	@Test
	public void testRemove() {
		OrderBucket bucket = new OrderBucket();
		
		Order one = createOrder(1, 1, 1, 0);
		Order two = createOrder(2, 1, 2, 1);
		Order three = createOrder(3, 1, 3, 1);
		Order four = createOrder(4, 1, 4, 1);
		
		bucket.addOrder(one);
		bucket.addOrder(two);
		bucket.addOrder(three);
		bucket.addOrder(four);
			
		bucket.removeOrder(one);
		assertTrue(bucket.head.id == 2 && bucket.tail.id == 4 && bucket.amount == 6);
		
		bucket.removeOrder(three);
		assertTrue(bucket.head.id == 2 && bucket.tail.id == 4 && bucket.amount == 4);
	}
	
	@Test
	public void testMatch() {
		OrderBucket bucket = new OrderBucket();
		OrderMap orderMap = new OrderMap(16);
		OrderBook.FilledResult result = new OrderBook.FilledResult();
		result.baseCurrencyScale = 1;
		
		bucket.addOrder(createOrder(1, 1, 1, 0)); //id, price, amount, filled
		bucket.addOrder(createOrder(2, 1, 2, 1));
		bucket.addOrder(createOrder(3, 1, 3, 1));
		bucket.addOrder(createOrder(4, 1, 4, 1));
		
		ObjectPool<Order> oPool = new ObjectPool<Order>(16, Order::new);
		
		bucket.match(1, result, orderMap, oPool, null);
		assertTrue(bucket.head.id == 2 && bucket.tail.id == 4 && bucket.amount == 6 
				&& result.filled == 1 && result.filledCost == 1);
		
		bucket.match(1, result, orderMap, oPool, null);
		assertTrue(bucket.head.id == 3 && bucket.head.filled == 1 && bucket.tail.id == 4 && bucket.amount == 5
				&& result.filled == 2 && result.filledCost == 2);		
		
		bucket.match(1, result, orderMap, oPool, null);
		assertTrue(bucket.head.id == 3 && bucket.head.filled == 2 && bucket.tail.id == 4 && bucket.amount == 4
				&& result.filled == 3 && result.filledCost == 3);
		
		bucket.match(5, result, orderMap, oPool, null);
		assertTrue(bucket.head == null && bucket.tail == null && result.filled == 7 && result.filledCost == 7);
	}
	
	@Test
	public void testChangeOrderAmount() {
		OrderBucket bucket = new OrderBucket();
		
		Order one = createOrder(1, 1, 3, 1);
		Order two = createOrder(2, 1, 2, 1);
		
		bucket.addOrder(one);
		bucket.addOrder(two);
		
		bucket.changeOrderAmount(one, 3, null);
		
		assertTrue(one.amount == 4 && one.filled == 1);
		assertTrue(bucket.amount == 4);
	}
	
	@Test
	public void testEqual() {
		OrderBucket bucket1 = new OrderBucket();
		OrderBucket bucket2 = new OrderBucket();
		
		Order one = createOrder(1, 1, 3, 1);
		Order two = createOrder(2, 2, 2, 1);
		
		bucket1.addOrder(one);
		bucket1.addOrder(two);
		
		Order three = createOrder(1, 1, 3, 1);
		Order four = createOrder(2, 2, 2, 1);
		
		
		bucket2.addOrder(three);
		bucket2.addOrder(four);
		
		assertTrue(bucket1.equals(bucket2));
		
		Order five = createOrder(3, 3, 2, 1);
		bucket2.addOrder(five);
		assertTrue(!bucket1.equals(bucket2));
	}
	
	private Order createOrder(long id, long price, long amount, long filled) {
		Order order = new Order();
		order.id = id;
		order.price = price;
		order.amount = amount;
		order.filled = filled;
		
		return order;
	}
}
