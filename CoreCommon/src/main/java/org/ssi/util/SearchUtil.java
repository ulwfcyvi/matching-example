package org.ssi.util;

import org.ssi.collection.TwoWayLongArray;

public class SearchUtil {
	
	public static int HOT_AREA_RANGE = 200;
	
	public static int findElementByAscending(TwoWayLongArray longArr, long value) throws Exception {
		int size = longArr.size();
		int limit = size;
		boolean needSearchMore = false;
		
		if(limit > HOT_AREA_RANGE) {
			limit = HOT_AREA_RANGE;
			needSearchMore = true;
		}
		
		for(int i = 0; i < limit; i++) {
			if(longArr.at(i) == value) {
				return i;
			}
		}
		
		if(needSearchMore) {
			int low = limit;
			int high = size - 1;
			
			while(low <= high) {
				int mid = (low + high) >> 1;
				
				if(longArr.at(mid) == value) {
					return mid;
				} else if (longArr.at(mid) > value) {
					high = mid - 1;
				} else {
					low = mid + 1;
				}
			}
		}
		
		return -1;
	}
	
	public static int findElementByDescending(TwoWayLongArray longArr, long value) throws Exception {
		int size = longArr.size();
		int limit = size;
		boolean needSearchMore = false;
		
		if(limit > HOT_AREA_RANGE) {
			limit = HOT_AREA_RANGE;
			needSearchMore = true;
		}
		
		for(int i = 0; i < limit; i++) {
			if(longArr.at(i) == value) {
				return i;
			}
		}
		
		if(needSearchMore) {
			int low = limit;
			int high = size - 1;
			
			while(low <= high) {
				int mid = (low + high) >> 1;
				
				if(longArr.at(mid) == value) {
					return mid;
				} else if (longArr.at(mid) < value) {
					high = mid - 1;
				} else {
					low = mid + 1;
				}
			}
		}
		
		return -1;
	}
	
	public static int findPreElementByAscending(TwoWayLongArray longArr, long value) throws Exception {
		
		int size = longArr.size();
		
		if(size == 0) {
			return -1;
		}
		
		if(value < longArr.at(0)) {
			return -1;
		}
		
		if(value > longArr.at(size - 1)) {
			return size - 1;
		}
		
		int limit = size;
		boolean needSearchMore = false;
		
		if(limit > HOT_AREA_RANGE) {
			limit = HOT_AREA_RANGE;
			needSearchMore = true;
		}
		
		for(int i = 0; i < limit; i++) {
			if(longArr.at(i) > value) {
				return i - 1;
			}
		}
		
		if(needSearchMore) {
			int low = limit;
			int high = size - 1;
			
			while(low <= high) {
				int mid = (low + high) >> 1;
				
				if(longArr.at(mid) == value) {
					return mid;
				} else if (longArr.at(mid) > value) {
					high = mid - 1;
				} else {
					low = mid + 1;
				}
			}
			
			return high;
		}
		
		return limit - 1;
	}
	
	public static int findPreElementByDescending(TwoWayLongArray longArr, long value) throws Exception {
		
		int size = longArr.size();
		
		if(size == 0) {
			return -1;
		}
		
		if(value > longArr.at(0)) {
			return -1;
		}
		
		if(value < longArr.at(size - 1)) {
			return size - 1;
		}
		
		int limit = size;
		boolean needSearchMore = false;
		
		if(limit > HOT_AREA_RANGE) {
			limit = HOT_AREA_RANGE;
			needSearchMore = true;
		}
		
		for(int i = 0; i < limit; i++) {
			if(longArr.at(i) < value) {
				return i - 1;
			}
		}
		
		if(needSearchMore) {
			int low = limit;
			int high = size - 1;
			
			while(low <= high) {
				int mid = (low + high) >> 1;
				
				if(longArr.at(mid) == value) {
					return mid;
				} else if (longArr.at(mid) < value) {
					high = mid - 1;
				} else {
					low = mid + 1;
				}
			}
			
			return high;
		}
		
		return limit - 1;
	}
}
