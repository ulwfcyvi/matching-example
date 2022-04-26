package org.ssi.event;

import org.ssi.collection.ConcurrentObjectPool;
import org.ssi.model.BaseEvent;
import org.ssi.model.MatchingResult;
import org.ssi.model.MatchingType;
import org.ssi.model.Order;
import org.ssi.model.TradeOption;

public class TradeEventManager {
	
	private ConcurrentObjectPool<MatchingResult> mrPool;
	private BaseEvent currentEvent;
	private MatchingResult tailResult;
	
	public TradeEventManager(int poolSize) {
		mrPool = new ConcurrentObjectPool<MatchingResult>(poolSize, MatchingResult::new);
		mrPool.fillPool(poolSize);
	}

	public void currentEvent(BaseEvent event) {
		currentEvent = event;
		tailResult = null;
	}
	
	public void sendTradeEvent(Order makerOrder, long amount) {
		MatchingResult result = mrPool.allocateOrNew();
		
		result.next = null;		
		result.type = MatchingType.TRADE;
		result.price = makerOrder.price;
		result.amount = amount;
		result.timestamp = System.currentTimeMillis();

		result.makerStopPrice = makerOrder.stopPrice;
		result.makerOrderType = makerOrder.type;
		result.makerOrderId = makerOrder.id;		
		result.makerId = makerOrder.userId;
		result.makerAmount = makerOrder.amount;
		result.makerFilled = makerOrder.filled;
		result.makerFilledCost = makerOrder.filledCost;
		result.makerTradeOption = makerOrder.tradeOption;
		
		if(tailResult == null) {
			currentEvent.matchingResult = result;
			tailResult = result;
		} else {
			tailResult.next = result;
			tailResult = result;
		}
	}
	
	public void sendCancelEvent(long left) {
		MatchingResult result = mrPool.allocateOrNew();

		result.next = null;		
		result.type = MatchingType.CANCEL;
		result.price = 0;
		result.amount = left;
		result.timestamp = System.currentTimeMillis();
		
		result.makerStopPrice = 0;
		result.makerOrderType = currentEvent.orderType;
		result.makerId = currentEvent.userId;
		result.makerOrderId = currentEvent.orderId;		
		result.makerAmount = 0;
		result.makerFilled = 0;
		result.makerFilledCost = 0;
		result.makerTradeOption = TradeOption.NONE;
		
		if(tailResult == null) {
			currentEvent.matchingResult = result;
			tailResult = result;
		} else {
			tailResult.next = result;
			tailResult = result;
		}
	}
	
	public void sendCancelEvent(Order order) {
		MatchingResult result = mrPool.allocateOrNew();
		
		result.next = null;		
		result.type = MatchingType.CANCEL;
		result.price = 0;
		result.amount = order.amount - order.filled;
		result.timestamp = System.currentTimeMillis();
		
		result.makerStopPrice = order.stopPrice;
		result.makerOrderType = order.type;
		result.makerId = order.userId;
		result.makerOrderId = order.id;		
		result.makerAmount = order.amount;
		result.makerFilled = order.filled;
		result.makerFilledCost = order.filledCost;
		result.makerTradeOption = TradeOption.NONE;
		
		if(tailResult == null) {
			currentEvent.matchingResult = result;
			tailResult = result;
		} else {
			tailResult.next = result;
			tailResult = result;
		}
	}
	
	public void returnPool(MatchingResult result) {
		mrPool.returnPool(result);
	}
}
