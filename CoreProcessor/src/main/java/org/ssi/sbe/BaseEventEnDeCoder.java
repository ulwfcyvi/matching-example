package org.ssi.sbe;

import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.ssi.model.BaseEvent;

public class BaseEventEnDeCoder {
	private static final BaseEventDecoder BASE_EVENT_DECODER = new BaseEventDecoder();
	private static final BaseEventEncoder BASE_EVENT_ENCODER = new BaseEventEncoder();
	private static final int ORDER_SIZE_BUFFER = 4096;
	private static final ByteBuffer ORDER_BYTE_BUFFER = ByteBuffer.allocate(ORDER_SIZE_BUFFER);
	private static final UnsafeBuffer ORDER_DIRECT_BUFFER = new UnsafeBuffer(ORDER_BYTE_BUFFER);

	public byte[] encodeByte(BaseEvent baseEvent) {
		BASE_EVENT_ENCODER.wrap(ORDER_DIRECT_BUFFER, 0)
				.eventType(baseEvent.eventType)
				.orderId(baseEvent.orderId)
				.timestamp(baseEvent.timestamp)
				.symbol(baseEvent.symbol)
				.price(baseEvent.price)
				.amount(baseEvent.amount)
				.stopPrice(baseEvent.stopPrice)
				.orderSide(baseEvent.orderSide)
				.orderType(baseEvent.orderType)
				.tradeType(baseEvent.tradeType)
				.tradeOption(baseEvent.tradeOption)
				.userId(baseEvent.userId)
				.clientOrderId(baseEvent.clientOrderId);
		byte[] buffer = new byte[BASE_EVENT_ENCODER.encodedLength()];
		byte[] bufferAll = BASE_EVENT_ENCODER.buffer().byteArray();
		for (int j = 0; j < buffer.length; j++) {
			buffer[j] = bufferAll[j + BASE_EVENT_ENCODER.offset()];
		}
		return buffer;
	}

//	public void decodeByte(byte[] buffer, BaseEvent baseEvent) {
//		final UnsafeBuffer directBuffer = new UnsafeBuffer(buffer);
//		BASE_EVENT_DECODER.wrap(directBuffer, 0, BaseEventDecoder.BLOCK_LENGTH, 0);
//		baseEvent.eventType = BASE_EVENT_DECODER.eventType();
//		baseEvent.orderId = BASE_EVENT_DECODER.orderId();
//		baseEvent.timestamp = BASE_EVENT_DECODER.timestamp();
//		baseEvent.symbol = BASE_EVENT_DECODER.symbol();
//		baseEvent.price = BASE_EVENT_DECODER.price();
//		baseEvent.amount = BASE_EVENT_DECODER.amount();
//		baseEvent.stopPrice = BASE_EVENT_DECODER.stopPrice();
//		baseEvent.orderSide = BASE_EVENT_DECODER.orderSide();
//		baseEvent.orderType = BASE_EVENT_DECODER.orderType();
//		baseEvent.tradeType = BASE_EVENT_DECODER.tradeType();
//		baseEvent.tradeOption = BASE_EVENT_DECODER.tradeOption();
//		baseEvent.userId = BASE_EVENT_DECODER.userId();
//		baseEvent.clientOrderId = BASE_EVENT_DECODER.clientOrderId();
//	}
	
	public void decodeByte(DirectBuffer buffer, int offset, BaseEvent baseEvent) {
		BASE_EVENT_DECODER.wrap(buffer, offset, BaseEventDecoder.BLOCK_LENGTH, 0);
		baseEvent.eventType = BASE_EVENT_DECODER.eventType();
		baseEvent.orderId = BASE_EVENT_DECODER.orderId();
		baseEvent.timestamp = BASE_EVENT_DECODER.timestamp();
		baseEvent.symbol = BASE_EVENT_DECODER.symbol();
		baseEvent.price = BASE_EVENT_DECODER.price();
		baseEvent.amount = BASE_EVENT_DECODER.amount();
		baseEvent.stopPrice = BASE_EVENT_DECODER.stopPrice();
		baseEvent.orderSide = BASE_EVENT_DECODER.orderSide();
		baseEvent.orderType = BASE_EVENT_DECODER.orderType();
		baseEvent.tradeType = BASE_EVENT_DECODER.tradeType();
		baseEvent.tradeOption = BASE_EVENT_DECODER.tradeOption();
		baseEvent.userId = BASE_EVENT_DECODER.userId();
		baseEvent.clientOrderId = BASE_EVENT_DECODER.clientOrderId();
	}
}
