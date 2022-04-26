package org.ssi.collection;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ObjectPoolTest {
	
	@Test
	public void testAllocateOrder() {
		ObjectPool<Integer> intPool = new ObjectPool<Integer>(8, () -> new Integer(0));
		
		intPool.fillPool(1);
		
		intPool.returnPool(new Integer(1));
		intPool.returnPool(new Integer(2));
		intPool.returnPool(new Integer(3));
		intPool.returnPool(new Integer(4));		
		
		Integer four = intPool.allocate();
		Integer three = intPool.allocate();
		Integer two = intPool.allocate();
		Integer one = intPool.allocate();
		
		assertTrue(four.intValue() == 4);
		assertTrue(three.intValue() == 3);
		assertTrue(two.intValue() == 2);
		assertTrue(one.intValue() == 1);		
	}
	
	@Test
	public void testAllocateOrNewOrder() {
		ObjectPool<Integer> intPool = new ObjectPool<Integer>(2, () -> new Integer(0));		
		intPool.fillPool(1);			
		
		Integer one = intPool.allocate();
		Integer two = intPool.allocate();

		assertTrue(two == null);
		
		two = intPool.allocateOrNew();
		
		assertTrue(two != null);
	}
	
	@Test
	public void testExtendPool() {
		ObjectPool<Integer> intPool = new ObjectPool<Integer>(8, () -> new Integer(0));
		
		Integer value = intPool.allocate();		
		assertTrue(value == null);
		
		intPool.fillPool(8);
		
		intPool.returnPool(new Integer(8));
		assertTrue(intPool.capacity() == 12);
	}
}
