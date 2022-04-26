package org.ssi.io;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.ssi.model.BaseEvent;
import org.ssi.model.DataAction;
import org.ssi.model.EventResult;
import org.ssi.model.EventType;
import org.ssi.model.MatchingResult;
import org.ssi.model.MatchingType;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderType;
import org.ssi.model.TradeOption;

public class OBDataWriterTest {
	
	@Test
	public void testWriteEventForPlaceLimitOrder() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		MatchingResult result1 = createResult(MatchingType.TRADE, 1001, 1, 1, 1, 1);
		MatchingResult result2 = createResult(MatchingType.TRADE, 1000, 2, 2, 2, 2);
		MatchingResult result3 = createResult(MatchingType.TRADE, 999, 1, 3, 1, 1);
		
		event.matchingResult = result1;
		result1.next = result2;
		result2.next = result3;
		
		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);
		
		verify(writer, times(4)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		InOrder inOrder = Mockito.inOrder(writer);		
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1001, -1);
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1000, -2);
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 999, -1);
		inOrder.verify(writer).writeObEvent(DataAction.CREATE, 0, OrderSide.ASK, 999, 1);

	}
	
	@Test
	public void testWriteEventForMatchHiddenLimitOrder() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		event.tradeOption = TradeOption.HIDDEN;
		
		MatchingResult result1 = createResult(MatchingType.TRADE, 1001, 1, 1, 1, 1);
		MatchingResult result2 = createResult(MatchingType.TRADE, 1000, 2, 2, 2, 2);
		MatchingResult result3 = createResult(MatchingType.TRADE, 999, 1, 3, 1, 1);
		result3.makerTradeOption = TradeOption.HIDDEN;
		
		event.matchingResult = result1;
		result1.next = result2;
		result2.next = result3;
		
		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);
		
		verify(writer, times(2)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		InOrder inOrder = Mockito.inOrder(writer);		
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1001, -1);
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1000, -2);
	}
	
	@Test
	public void testWriteEventForPlaceMarketOrder() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.MARKET, EventType.PLACE_ORDER);
		MatchingResult result1 = createResult(MatchingType.TRADE, 1001, 1, 1, 1, 1);
		MatchingResult result2 = createResult(MatchingType.TRADE, 1000, 2, 2, 2, 2);
		MatchingResult result3 = createResult(MatchingType.CANCEL, 999, 2, 0, 0, 0);
		
		event.matchingResult = result1;
		result1.next = result2;
		result2.next = result3;
		
		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);
		
		verify(writer, times(2)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		InOrder inOrder = Mockito.inOrder(writer);		
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1001, -1);
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1000, -2);
	}
	
	@Test
	public void testWriteEventForPlaceStopOrder() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.STOP_LIMIT, EventType.PLACE_ORDER);		

		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);		
		verify(writer, times(0)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.STOP_MARKET, EventType.PLACE_ORDER);
		
		writer.writeSingleEvent(event);
		verify(writer, times(0)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
	}
	
	@Test
	public void testWriteEventForPlaceLimitOrder2() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		MatchingResult result1 = createResult(MatchingType.TRADE, 1001, 1, 1, 1, 1);
		MatchingResult result2 = createResult(MatchingType.TRADE, 1000, 2, 2, 2, 2);
		MatchingResult result3 = createResult(MatchingType.TRADE, 999, 2, 3, 3, 2);
		
		event.matchingResult = result1;
		result1.next = result2;
		result2.next = result3;
		
		
		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);
		
		verify(writer, times(3)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		InOrder inOrder = Mockito.inOrder(writer);		
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1001, -1);
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 1000, -2);
		inOrder.verify(writer).writeObEvent(DataAction.UPDATE, 0, OrderSide.BID, 999, -2);
		
		event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.LIMIT, EventType.PLACE_ORDER);
		result1 = createResult(MatchingType.TRADE, 999, 1, 3, 3, 3);		
		
		event.matchingResult = result1;
		
		writer.writeSingleEvent(event);
		
		inOrder.verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.BID, 999, -1);
		inOrder.verify(writer).writeObEvent(DataAction.CREATE, 0, OrderSide.ASK, 999, 4);
	}
	
	@Test
	public void testWriteEventForCancelLimitOrder() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.LIMIT, EventType.CANCEL_ORDER);
		
		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);
		verify(writer).writeObEvent(DataAction.DELETE, 0, OrderSide.ASK, 999, -5);	
	}
	
	@Test
	public void testWriteEventForCancelStopOrder() {
		
		BaseEvent event = createTradeEvent(100, 999, 5, OrderSide.ASK, OrderType.STOP_MARKET, EventType.CANCEL_ORDER);
		
		OBDataWriter writer = spy(OBDataWriter.class);		
		doNothing().when(writer).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
		
		writer.writeSingleEvent(event);
		verify(writer, times(0)).writeObEvent(anyByte(), anyInt(), anyByte(), anyLong(), anyLong());
	}
	
	public static BaseEvent createTradeEvent(long id, long price, long amount, byte side, byte type, byte eventType) {
		BaseEvent event = new BaseEvent();
		event.orderId = id;
		event.price = price;
		event.amount = amount;
		event.orderSide = side;
		event.orderType = type;
		event.eventType = eventType;
		event.eventResult = EventResult.OK;
		return event;
	}
	
	public static MatchingResult createResult(byte type, long price, long amount, long makerOrderId, long makerAmount, long makerFilled) {
		MatchingResult result = new MatchingResult();
		result.type = type;
		result.price = price;
		result.amount = amount;
		result.makerOrderId = makerOrderId;
		result.makerAmount = makerAmount;
		result.makerFilled = makerFilled;
		
		return result;
	}
}
