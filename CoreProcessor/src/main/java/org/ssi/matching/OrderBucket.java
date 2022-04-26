package org.ssi.matching;

import org.ssi.collection.ObjectPool;
import org.ssi.event.TradeEventManager;
import org.ssi.matching.OrderBook.FilledResult;
import org.ssi.model.Order;
import org.ssi.model.OrderStatus;
import org.ssi.model.TradeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderBucket {
	private static final Logger LOG = LoggerFactory.getLogger(OrderBucket.class);
	
	public long amount = 0;
	public int length = 0	;
	
	public Order head = null;
	public Order tail = null;
	
	public PriceLadder ladder = null;
	
	public void init() {
		amount = length = 0;
		head = tail = null;
	}
	
	public void addOrder(Order order) {
		if(head == null) {
			head = tail = order;
		} else {
			tail.nextOrder = order;
			order.prevOrder = tail;
			tail = order;
		}
		length++;
		amount += order.amount - order.filled;
		order.bucket = this;
	}

	
	public void removeOrder(Order order) {
		length--;
		amount -= order.amount - order.filled;
		
		Order prevOrder = order.prevOrder;
		Order nextOrder = order.nextOrder;
		
		if(prevOrder != null && nextOrder != null) {
			prevOrder.nextOrder = nextOrder;
			nextOrder.prevOrder = prevOrder;			
		} else if (prevOrder != null) {
			prevOrder.nextOrder = null;
			tail = prevOrder;
		} else if (nextOrder != null) {
			nextOrder.prevOrder = null;
			head = nextOrder;
		} else {
			tail = head = null;
		}
	}


	
	public void changeOrderAmount(Order order, long newAmount, TradeEventManager eventManager) {
		long delta = newAmount - (order.amount - order.filled);
		order.amount += delta;
		amount += delta;
		
		if(eventManager != null) {
			eventManager.sendCancelEvent(order);
		}
	}
	
	public long match(long wantedAmount, FilledResult result, OrderMap orderMap, ObjectPool<Order> orderPool, TradeEventManager eventManager) {
		long filled = 0;
		double filledCost = 0;
		
		while(head != null && wantedAmount > 0) {
			long matchedAmount = Math.min(wantedAmount, head.amount - head.filled);
			wantedAmount -= matchedAmount;						
			amount -= matchedAmount;

			head.filled += matchedAmount;
			filled += matchedAmount;
			result.filled += matchedAmount;
			
			filledCost = (matchedAmount / result.baseCurrencyScale) * head.price;
			head.filledCost += filledCost;
			result.filledCost += filledCost;

			if(head.tradeOption == TradeOption.OCO){
				result.orderOcoLimitArr.add(head.id);
			}

			//order has been fully matched -> remove from bucket then return it to pool
			if(head.amount == head.filled) {
				head.status = OrderStatus.FILLED;
				length--;
				orderMap.remove(head.id);
				orderPool.returnPool(head);
				if(eventManager != null) {
					eventManager.sendTradeEvent(head, matchedAmount);
				}
				head = head.nextOrder;
				
				if(head != null) {
					head.prevOrder = null;
				}
			} else {
				head.status = OrderStatus.PARTIALLY_FILLED;
				if(eventManager != null) {
					eventManager.sendTradeEvent(head, matchedAmount);
				}
				break;
			}
		}
		
		if(head == null) {
			tail = null;
		}

		return filled;
	}
	
	private String getDisplayStr() {
		String result = "";
		
		Order cursor = head;
		result = "amount = " + amount + "; length = " + length;
		while(cursor != null) {
			result += " [order_id = " + cursor.id + "; symbol = " + cursor.symbol + "; price = " + cursor.price + "; amount = " + cursor.amount + "; filled = " + cursor.filled + "]";
			cursor = cursor.nextOrder;
		}
		
		return result;
	}
	
	public boolean equals(Object o) {
		
		if(this == o) {
			return true;
		}		
		
		if(!(o instanceof OrderBucket)) {
			return false;
		}
		
		OrderBucket bucket2 = (OrderBucket) o;		
		
		if(amount != bucket2.amount || length != bucket2.length) {
			return false;
		} else {
		
			Order o1 = head;
			Order o2 = bucket2.head;
			
			for(int i = 0; i < length; i++) {
				if(!o1.equals(o2)) {
					return false;
				}
				
				o1 = o1.nextOrder;
				o2 = o2.nextOrder;
			}
		}
		
		return true;
	}
}
