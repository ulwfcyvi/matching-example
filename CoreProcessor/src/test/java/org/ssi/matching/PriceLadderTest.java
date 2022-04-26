package org.ssi.matching;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ssi.collection.TwoWayLongArray;

import org.ssi.util.SearchUtil;

public class PriceLadderTest {
		
	@Test
	public void testBidLadder() throws Exception {
		PriceLadder ladder = new PriceLadder(1 << 10, false);
		
		for(int i = 1; i <= 100; i++) {
			ladder.addBucket(i * 5, new OrderBucket());
		}
		
		TwoWayLongArray priceArr = ladder.priceArr();
		
		for(int i = 1; i <= 100; i++) {
			assertTrue(priceArr.at(i - 1) == (101 - i) * 5);
			assertTrue(ladder.getBucket((101 - i) * 5) != null);
		}
		
		ladder.removeBucket(50);
		
		int index = SearchUtil.findElementByAscending(priceArr, 50);
		
		OrderBucket bucket = ladder.getBucket(50);
		
		assertTrue(index < 0 && bucket == null);
	}
	
	@Test
	public void testAskLadder() throws Exception {
		PriceLadder ladder = new PriceLadder(1 << 10, true);
		
		for(int i = 100; i >= 1; i--) {
			ladder.addBucket(i * 5, new OrderBucket());
		}
		
		TwoWayLongArray priceArr = ladder.priceArr();
		
		for(int i = 1; i <= 100; i++) {
			assertTrue(priceArr.at(i - 1) == i * 5);
			assertTrue(ladder.getBucket(i * 5) != null);
		}
		
		ladder.removeBucket(50);
		
		int index = SearchUtil.findElementByDescending(priceArr, 50);
		
		OrderBucket bucket = ladder.getBucket(50);
		
		assertTrue(index < 0 && bucket == null);
	}
	
	@Test
	public void testGetAmountByPrice() throws Exception {
		//ask ladder
		PriceLadder ladder = new PriceLadder(1 << 10, true);
		
		OrderBucket bucket = new OrderBucket();
		bucket.amount = 1;		
		ladder.addBucket(1, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 2;
		ladder.addBucket(3, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 3;
		ladder.addBucket(5, bucket);
		
		assertTrue(ladder.getTotalAmount(1) == 1 && ladder.getTotalAmount(2) == 1 
				&& ladder.getTotalAmount(3) == 3 && ladder.getTotalAmount(4) == 3
				&& ladder.getTotalAmount(5) == 6 && ladder.getTotalAmount(6) == 6);
		
		//bid ladder
		ladder = new PriceLadder(1 << 10, false);
		
		bucket = new OrderBucket();
		bucket.amount = 1;		
		ladder.addBucket(9, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 2;
		ladder.addBucket(7, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 3;
		ladder.addBucket(5, bucket);
		
		assertTrue(ladder.getTotalAmount(9) == 1 && ladder.getTotalAmount(8) == 1 
				&& ladder.getTotalAmount(7) == 3 && ladder.getTotalAmount(6) == 3 
				&& ladder.getTotalAmount(5) == 6 && ladder.getTotalAmount(4) == 6);		
	}
	
	@Test
	public void testEnoughAmountByPrice() throws Exception {
		//ask ladder
		PriceLadder ladder = new PriceLadder(1 << 10, true);
		
		OrderBucket bucket = new OrderBucket();
		bucket.amount = 1;		
		ladder.addBucket(1, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 2;
		ladder.addBucket(3, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 3;
		ladder.addBucket(5, bucket);
		
		assertTrue(ladder.isEnoughAmountAtRate(1, 1) && ladder.isEnoughAmountAtRate(2, 1) && !ladder.isEnoughAmountAtRate(2, 2)
				&& ladder.isEnoughAmountAtRate(3, 3) && ladder.isEnoughAmountAtRate(4, 3) && !ladder.isEnoughAmountAtRate(4, 6)
				&& ladder.isEnoughAmountAtRate(5, 6) && ladder.isEnoughAmountAtRate(6, 6));
		
		//bid ladder
		ladder = new PriceLadder(1 << 10, false);
		
		bucket = new OrderBucket();
		bucket.amount = 1;		
		ladder.addBucket(9, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 2;
		ladder.addBucket(7, bucket);
		
		bucket = new OrderBucket();
		bucket.amount = 3;
		ladder.addBucket(5, bucket);
		
		assertTrue(ladder.isEnoughAmountAtRate(9, 1) && ladder.isEnoughAmountAtRate(8, 1) && !ladder.isEnoughAmountAtRate(8, 2)
				&& ladder.isEnoughAmountAtRate(7, 3) && ladder.isEnoughAmountAtRate(6, 3) && !ladder.isEnoughAmountAtRate(6, 6)
				&& ladder.isEnoughAmountAtRate(5, 6) && ladder.isEnoughAmountAtRate(4, 6));	
	}
}
