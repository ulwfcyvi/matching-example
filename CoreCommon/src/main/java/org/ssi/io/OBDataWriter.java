package org.ssi.io;

import org.ssi.model.BaseEvent;
import org.ssi.model.DataAction;
import org.ssi.model.EventResult;
import org.ssi.model.EventType;
import org.ssi.model.MatchingResult;
import org.ssi.model.MatchingType;
import org.ssi.model.OrderSide;
import org.ssi.model.TradeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDataWriter extends QWriter {
	private static final Logger LOG = LoggerFactory.getLogger(OBDataWriter.class);

	@Override
	public void writeEvent(BaseEvent event) {
		BaseEvent cursor = event;
		while(cursor != null) {
			writeSingleEvent(cursor);
			cursor = cursor.next;
		}
	}
	
	public void writeSingleEvent(BaseEvent event) {
		if(event.eventResult == EventResult.OK) {
			switch(event.eventType) {
				case EventType.PLACE_ORDER:
					
					if(event.isStopOrderEvent()) {
						return;
					}
					
					MatchingResult mr = event.matchingResult;
					long filled = 0;
					byte markerSide = event.orderSide == OrderSide.ASK ? OrderSide.BID : OrderSide.ASK;
					boolean isCancelled = false;
					
					while(mr != null) {
						if(mr.type == MatchingType.TRADE) {
							filled += mr.amount;
							
							if(mr.makerTradeOption != TradeOption.HIDDEN) {
								if(mr.makerAmount == mr.makerFilled) {								
									writeObEvent(DataAction.DELETE, event.symbol, markerSide, mr.price, -mr.amount);
								} else {
									writeObEvent(DataAction.UPDATE, event.symbol, markerSide, mr.price, -mr.amount);
								}
							}
						} else if(mr.type == MatchingType.CANCEL) {
							isCancelled = true;
						}
						
						mr = mr.next;
					}
					
					//order has not been filled completely -> create new order
					if(event.tradeOption != TradeOption.HIDDEN && !isCancelled && event.amount > filled) {
						writeObEvent(DataAction.CREATE, event.symbol, event.orderSide, event.price, event.amount - filled);
					}
					
					break;
					
				case EventType.CANCEL_ORDER:
					
					if(event.tradeOption == TradeOption.HIDDEN || event.isStopOrderEvent()) {
						return;
					}
					
					writeObEvent(DataAction.DELETE, event.symbol, event.orderSide, event.price, -event.amount);
					break;
					
				case EventType.ADD_SYMBOL:
					writeObEvent(DataAction.INIT_SYMBOL, event.symbol, event.orderSide, event.price, event.amount);
					break;	
				case EventType.TAKE_SNAPSHOT:
					writeObEvent(DataAction.TAKE_SNAPSHOT, event.symbol, event.orderSide, event.price, event.amount);
					break;
				case EventType.REPLAY_EVENTS:
					writeObEvent(DataAction.REPLAY, event.symbol, event.orderSide, event.price, event.amount);
					break;
				case EventType.APPLYING_RESULTS:
					buffer.writeByte(DataAction.STOP_APPLYING_RESULTS);
					buffer.writeInt(-1);
					buffer.writeByte(event.eventResult);
					buffer.writeLong(0);
					buffer.writeLong(0);
					appender.writeBytes(b -> b
							.write(buffer, 0, buffer.writePosition()));
					buffer.clear();
					break;
			}
		}
	}
	
	public void writeObEvent(byte type, int symbol, byte side, long price, long amount) {
		buffer.writeByte(type);
		buffer.writeInt(symbol);
		buffer.writeByte(side);
		buffer.writeLong(price);
		buffer.writeLong(amount);
	}
}