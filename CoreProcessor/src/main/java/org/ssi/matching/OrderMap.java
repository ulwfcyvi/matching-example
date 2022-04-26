package org.ssi.matching;

import org.ssi.model.Order;
import org.ssi.util.BitUtil;

public class OrderMap {
	
	static final int MAX_CAPACITY = 1 << 30;
	
	private Order[] table;
	private int size;
	private int capacity;
	private int mask;
	
	public OrderMap(int size) {
		
		if(size <= 0) {
			throw new IllegalArgumentException("Illegal size = " + size);
		}
		
        capacity = BitUtil.findNextPositivePowerOfTwo(size);
		
        if (capacity > MAX_CAPACITY) {
        	capacity = MAX_CAPACITY;
		}
		
        mask = capacity - 1;
        table = new Order[capacity];
        size = 0;
	}
	
	public int capacity() {
		return capacity;
	}
	
	public int size() {
		return size;
	}
	
	public void put(long oid, Order order) {
		int index = getIndex(oid);
		
		if(table[index] == null) {
			table[index] = order;
		} else {
			order.nextOrderInMap = table[index];
			table[index] = order;
		}		
		
		size++;
	}
	
	public Order get(long oid) {
		int index = getIndex(oid);
		
		Order order = table[index];
		while(order != null) {
			if(order.id == oid) {
				return order;
			}
			
			order = order.nextOrderInMap;
		}
		
		return null;		
	}
	
	public Order remove(long oid) {
		int index = getIndex(oid);
		
		Order prev = table[index];
		Order e = prev;
		
		
		while(e != null) {
			Order next = e.nextOrderInMap;
			if(e.id == oid) {
				size--;
				if(prev == e) {
					table[index] = next;
				} else {
					prev.nextOrderInMap = next;
				}
				
				return e;
			}
			
			prev = e;
			e = next;
		}
		
		return null;	
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof OrderMap)) {
			return false;
		}
		
		OrderMap oMap2 = (OrderMap)o;
		if(size != oMap2.size()) {
			return false;
		}
		
		for(int i = 0; i < capacity; i++) {
				
			Order thisOrder = table[i];				
			Order thatOrder = null;
			
			while(thisOrder != null) {
				thatOrder = oMap2.get(thisOrder.id);
				
				if(thatOrder == null && !thisOrder.equals(thatOrder)) {
					return false;
				}
				
				thisOrder = thisOrder.nextOrder;
			}
		}
		
		return true;
	}
	
	private int getIndex(long key) {
		return (int)(key & mask);
	}
	
}
