package org.ssi.model;

import org.ssi.matching.OrderBucket;

public class Order {
	
	public long id;
	public long timestamp;
	public int symbol;
	public long userId;
	
	public long price;
	public long stopPrice;
	public long amount;
	public long filled;
	public double filledCost;	
	
	public byte side;
	public byte type;	
	public byte status;
	public byte tradeOption;
	
	public Order nextOrder = null;
	public Order prevOrder = null;
	public OrderBucket bucket = null;
	
	public Order nextOrderInMap = null;
	
	public void copyFromEvent(BaseEvent event) {
		id = event.orderId;
		timestamp = System.currentTimeMillis();
		symbol = event.symbol;
		userId = event.userId;
		
		price = event.price;
		stopPrice = event.stopPrice;
		amount = event.amount;
		filled = 0;		
		filledCost = 0;		
		
		side = event.orderSide;
		type = event.orderType;
		status = OrderStatus.OPEN;
		tradeOption = event.tradeOption;
				
		nextOrder = null;
		prevOrder = null;
		bucket = null;
		nextOrderInMap = null;
	}
	
	public void copyToEvent(BaseEvent event) {
		event.eventType = EventType.PLACE_ORDER;
		
		event.orderId = id;
		event.timestamp = System.currentTimeMillis();
		
		event.symbol = symbol;
		event.price =  price;
		event.amount = amount;
		event.stopPrice = (type == OrderType.STOP_MARKET) ? stopPrice : 0;

		event.tradeOption = tradeOption;
		event.orderSide = side;
		//change order_type when converting STOP order to LIMIT or MARKET order
		event.orderType = (type == OrderType.STOP_MARKET) ? OrderType.MARKET : OrderType.LIMIT;
		event.userId = userId;
		
		event.eventResult = EventResult.OK;
		event.matchingResult = null;
	}
	
	public void copyToCancelEvent(BaseEvent event) {
		event.eventType = EventType.CANCEL_ORDER;
		
		event.orderId = id;
		event.timestamp = System.currentTimeMillis();
		
		event.symbol = symbol;
		event.price = price;
		event.amount = amount;
		event.stopPrice = 0;

		event.tradeOption = TradeOption.NONE;
		event.orderSide = side;

		event.orderType = type;
		event.userId = userId;
		
		event.eventResult = EventResult.OK;
		event.matchingResult = null;
	}
	
	public boolean equals(Object o) {
		
		if(o == this) {
			return true;
		}
		
		if(!(o instanceof Order)) {
			return false;
		}
		
		Order o2 = (Order)o;		
		
		return 	id == o2.id
				&& timestamp == o2.timestamp
				&& symbol == o2.symbol
				&& userId == o2.userId				
				&& price == o2.price
				&& stopPrice == o2.stopPrice
				&& amount == o2.amount
				&& filled == o2.filled
				&& Math.abs(filledCost - o2.filledCost) < 0.00000001				
				&& side == o2.side
				&& type == o2.type
				&& status == o2.status
				&& tradeOption == o2.tradeOption;
	}
}
