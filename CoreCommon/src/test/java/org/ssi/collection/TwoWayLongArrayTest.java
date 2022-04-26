package org.ssi.collection;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TwoWayLongArrayTest {
	
	@Test
	public void testInsert() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		assertTrue(longArr.head() == 8);
		
		longArr.insertBeforeHead(8);
		assertTrue(longArr.head() == 7);
		
		longArr.insertAfterTail(7);
		assertTrue(longArr.tail() == 9);
		
		for(int i = 6; i >= 1; i--) {
			longArr.insertAfterTail(i);
		}
		
		assertTrue(longArr.tail() == 15);
		
		//touch tail -> move block to center
		longArr.insertAfterTail(0);
		assertTrue(longArr.tail() == 13 && longArr.head() == 4);
		
		longArr.insertAt(3, 11);
		assertTrue(longArr.tail() == 13 && longArr.head() == 3);
		
		longArr.insertAt(5, 12);
		assertTrue(longArr.tail() == 14 && longArr.head() == 3);
	}
	
	@Test
	public void testRemove() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		
		for(int i = 10; i >= 1; i--) {
			longArr.insertAfterTail(i);
		}
		
		assertTrue(longArr.tail() == 14 && longArr.head() == 4);
		
		longArr.removeHead();
		assertTrue(longArr.tail() == 14 && longArr.head() == 5);
		
		longArr.removeTail();
		assertTrue(longArr.tail() == 13 && longArr.head() == 5);
		
		longArr.removeAt(3);
		assertTrue(longArr.tail() == 13 && longArr.head() == 6);
		
		longArr.removeByDescending(3);
		assertTrue(longArr.tail() == 12 && longArr.head() == 6);
	}
	
	@Test
	public void testResize() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		for(int i = 1; i <= 12; i++) {
			longArr.insertAfterTail(i);
		}
		
		assertTrue(longArr.capacity() == 20);
		assertTrue(longArr.head() == 4 && longArr.tail() == 16);
	}
	
	@Test
	public void testEqual() throws Exception {
		TwoWayLongArray longArr1 = new TwoWayLongArray(1024);
		TwoWayLongArray longArr2 = new TwoWayLongArray(512);
		
		for(int i = 0 ; i <= 200; i++) {
			longArr1.insertByAscending(i);
		}
		
		for(int i = 100; i >= 0; i--) {
			longArr2.insertByAscending(i);
		}
		
		for(int i = 101; i <= 200; i++) {
			longArr2.insertByAscending(i);
		}
		
		assertTrue(longArr1.equals(longArr2));
		
		longArr2.removeByAscending(100);
		assertTrue(!longArr1.equals(longArr2));
		
		longArr2.insertByAscending(100);
		assertTrue(longArr1.equals(longArr2));
	}

}
