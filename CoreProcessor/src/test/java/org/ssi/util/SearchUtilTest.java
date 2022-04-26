package org.ssi.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ssi.collection.TwoWayLongArray;

public class SearchUtilTest {
	
	@Test
	public void testFindElementByAscending() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		
		for(int i = 1; i <= 12; i++) {
			longArr.insertAfterTail(i);
		}
		
		assertTrue(longArr.head() == 4 && longArr.tail() == 16);
		
		SearchUtil.HOT_AREA_RANGE = 3;
		int index = SearchUtil.findElementByAscending(longArr, 2);
		assertTrue(index == 1);
		
		index = SearchUtil.findElementByAscending(longArr, 13);
		assertTrue(index == -1);
	}

	@Test
	public void testFindElementByDescending() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		
		for(int i = 12; i >= 1; i--) {
			longArr.insertAfterTail(i);
		}
		
		SearchUtil.HOT_AREA_RANGE = 3;
		int index = SearchUtil.findElementByDescending(longArr, 2);
		assertTrue(index == 10);
		
		index = SearchUtil.findElementByDescending(longArr, 13);
		assertTrue(index == -1);
	}
	
	@Test
	public void testPreElementByAscending() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		
		for(int i = 0; i <= 12; i++) {
			longArr.insertAfterTail(2 * i);
		}
		
		SearchUtil.HOT_AREA_RANGE = 3;
		int index = SearchUtil.findPreElementByAscending(longArr, 23);
		assertTrue(index == 11);
		
		index = SearchUtil.findPreElementByAscending(longArr, 21);
		assertTrue(index == 10);
		
		index = SearchUtil.findPreElementByAscending(longArr, 1);
		assertTrue(index == 0);
	}
	
	@Test
	public void testPreElementByDescending() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(8);
		
		for(int i = 12; i >= 0; i--) {
			longArr.insertAfterTail(2 * i);
		}
		
		SearchUtil.HOT_AREA_RANGE = 3;
		int index = SearchUtil.findPreElementByDescending(longArr, 23);
		assertTrue(index == 0);
		
		index = SearchUtil.findPreElementByDescending(longArr, 21);
		assertTrue(index == 1);
		
		index = SearchUtil.findPreElementByDescending(longArr, 1);
		assertTrue(index == 11);
	}
	
	@Test
	public void testInsertAndRemoveByAscending() throws Exception {
		TwoWayLongArray longArr = new TwoWayLongArray(1 << 15);
		
		SearchUtil.HOT_AREA_RANGE = 100;
		
		for(int i = 1; i <= 1 << 14; i++) {
			longArr.insertByAscending(i);
		}		
		assertTrue(longArr.size() == 1 << 14);
		
		//delete higher part
		for(int i = 1 << 13; i <= 1 << 14; i++) {
			longArr.removeByAscending(i);
		}		
		assertTrue(longArr.size() ==  (1 << 14) - (1 << 13) - 1);
		
		//delete lower part
		for(int i = (1 << 13) - 1; i >= 1; i--) {
			longArr.removeByAscending(i);
		}
		
		assertTrue(longArr.size() == 0);		
	}
	
	@Test
	public void testInsertAndRemoveByDescending() throws Exception {
		
		TwoWayLongArray longArr = new TwoWayLongArray(1 << 15);
		
		SearchUtil.HOT_AREA_RANGE = 100;
		
		for(int i = 1 << 14; i >= 1; i--) {
			longArr.insertByDescending(i);
		}		
		assertTrue(longArr.size() == 1 << 14);
		
		//delete higher part
		for(int i = 1 << 13; i <= 1 << 14; i++) {
			longArr.removeByDescending(i);
		}		
		assertTrue(longArr.size() ==  (1 << 14) - (1 << 13) - 1);
		
		//delete lower part
		for(int i = (1 << 13) - 1; i >= 1; i--) {
			longArr.removeByDescending(i);
		}
		
		assertTrue(longArr.size() == 0);		
	}
	
}
