package org.ssi.matching;

import org.ssi.collection.Long2ObjectMap;
import org.ssi.collection.TwoWayLongArray;

public class PriceLadder {

	public static final int INITIAL_LEVEL_NUM = 1 << 10;
	
	private TwoWayLongArray priceArr;	
	private Long2ObjectMap<OrderBucket> priceToBucketMap;
	private boolean isAscending;
	
	public PriceLadder() {
		this(INITIAL_LEVEL_NUM, true);
	}
	
	public PriceLadder(int levelNum, boolean isAsc) {
		priceArr = new TwoWayLongArray(levelNum);		
		priceToBucketMap = new Long2ObjectMap<OrderBucket>(levelNum << 1);
		isAscending = isAsc;
	}
	
	public void removeBucket(long price) throws Exception {
		priceToBucketMap.remove(price);
		
		if(isAscending) {
			priceArr.removeByAscending(price);
		} else {
			priceArr.removeByDescending(price);
		}
	}
	
	public void addBucket(long price, OrderBucket bucket) throws Exception {
		bucket.ladder = this;
		priceToBucketMap.put(price, bucket);
		
		if(isAscending) {
			priceArr.insertByAscending(price);
		} else {
			priceArr.insertByDescending(price);
		}
	}
	
	public boolean isMatchingPrice(long price) throws Exception {
		if(priceArr.size() == 0) {
			return false;
		}
		
		return isAscending ? price >= priceArr.at(0) : price <= priceArr.at(0); 
	}
	public boolean isOutOfRangePrice(long price) throws Exception {
		if(priceArr.size() == 0) {
			return true;
		}
		
		return isAscending ? 
			(price < priceArr.peekHead() || price > priceArr.peekTail()) 
			: (price > priceArr.peekHead() || price < priceArr.peekTail());
	}
	
	public long getTotalAmount(long price) throws Exception {
		long totalAmount = 0;
		int size = priceArr.size();
		
		for(int i = 0; i < size; i++) {
			long levelPrice = priceArr.at(i);
			
			if((isAscending && levelPrice > price) || (!isAscending && levelPrice < price)) {
				break;
			}
			
			OrderBucket bucket = priceToBucketMap.get(levelPrice);
			totalAmount += bucket.amount;
		}
		
		return totalAmount;
	}
	
	public boolean isEnoughAmountAtRate(long price, long amount) throws Exception {
		long totalAmount = 0;
		int size = priceArr.size();
		
		for(int i = 0; i < size; i++) {
			long levelPrice = priceArr.at(i);
			
			if((isAscending && levelPrice > price) || (!isAscending && levelPrice < price)) {
				break;
			}
			
			OrderBucket bucket = priceToBucketMap.get(levelPrice);
			totalAmount += bucket.amount;
			
			if(totalAmount >= amount) {
				return true;
			}
		}
		
		return false;
	}
	
	public TwoWayLongArray priceArr() {
		return priceArr;
	}
	
	public Long2ObjectMap<OrderBucket> bucketMap() {
		return priceToBucketMap;
	}
	
	public void priceArr(TwoWayLongArray arr) {
		priceArr = arr;
	}
	
	public void bucketMap(Long2ObjectMap<OrderBucket> map) {
		priceToBucketMap = map;
	}
	
	public int size() {
		return priceArr.size();
	}
	
	public OrderBucket getBucket(long price) {
		return priceToBucketMap.get(price);
	}
	
	public boolean equals(Object o) {
		
		if(o == this) {
			return true;
		}
		
		if(!(o instanceof PriceLadder)) {
			return false;
		}
		
		PriceLadder ld = (PriceLadder)o;
		
		return priceArr.equals(ld.priceArr()) && priceToBucketMap.equals(ld.bucketMap());
	}
}
