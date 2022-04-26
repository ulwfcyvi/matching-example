package org.ssi.matching;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ssi.model.Order;

public class OrderMapTest {

	@Test
	public void testAll() {
		OrderMap map = new OrderMap(24);
		assertTrue(map.capacity() == 32);
		
		Order one = createOrder(1, 1, 1);
		Order two = createOrder(2, 2, 2);
		Order three = createOrder(33, 33, 33);
		Order four = createOrder(65, 65, 65);
		
		map.put(one.id, one);
		map.put(two.id, two);
		map.put(three.id, three);
		map.put(four.id, four);
		
		assertTrue(map.size() == 4);
		assertTrue(three.nextOrderInMap != null);
		assertTrue(three.nextOrderInMap.id == 1);
		
		assertTrue(four.nextOrderInMap != null);
		assertTrue(four.nextOrderInMap.id == 33);
		
		map.remove(33);
		assertTrue(four.nextOrderInMap != null);
		assertTrue(four.nextOrderInMap.id == 1);
		
		Order test = map.get(33);
		assertTrue(test == null);
		
		test = map.get(65);
		assertTrue(test != null);
		
		map.remove(1);
		assertTrue(four.nextOrderInMap == null);
		
		test = map.remove(15);
		assertTrue(test == null);
	}
	
	private Order createOrder(long id, long price, long amount) {
		Order order = new Order();
		order.id = id;
		order.price = price;
		order.amount = amount;
		
		return order;
	}
}
