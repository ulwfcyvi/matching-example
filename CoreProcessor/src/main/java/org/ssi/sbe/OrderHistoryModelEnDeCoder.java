package org.ssi.sbe;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.ssi.response.model.OrderHistoryModel;

import java.nio.ByteBuffer;

public class OrderHistoryModelEnDeCoder {
	private static final OrderHistoryModelDecoder ORDER_HISTORY_MODEL_DECODER = new OrderHistoryModelDecoder();
	private static final OrderHistoryModelEncoder ORDER_HISTORY_MODEL_ENCODER = new OrderHistoryModelEncoder();
	private static final ByteBuffer ORDER_HISTORY_MODEL_BYTE_BUFFER = ByteBuffer.allocate(OrderHistoryModelEncoder.BLOCK_LENGTH);
	private static final UnsafeBuffer ORDER_HISTORY_MODEL_DIRECT_BUFFER = new UnsafeBuffer(
			ORDER_HISTORY_MODEL_BYTE_BUFFER);

	public OrderHistoryModelEnDeCoder() {
		ORDER_HISTORY_MODEL_ENCODER.wrap(ORDER_HISTORY_MODEL_DIRECT_BUFFER, 0);
	}
	
	public DirectBuffer encodeByte(OrderHistoryModel orderHistoryModel) {
		ORDER_HISTORY_MODEL_ENCODER
				.orderId(orderHistoryModel.orderId)
				.userId(orderHistoryModel.userId)
				.symbolId(orderHistoryModel.symbolId)
				.created(orderHistoryModel.created)
				.updated(orderHistoryModel.updated)
				.orderType(orderHistoryModel.orderType)
				.orderSide(orderHistoryModel.orderSide)
				.orderStatus(orderHistoryModel.orderStatus)
				.price(orderHistoryModel.price)
				.filled(orderHistoryModel.filled)
				.totalFilled(orderHistoryModel.totalFilled)
				.amount(orderHistoryModel.amount)
				.averagePrice(orderHistoryModel.averagePrice)
				.stopPrice(orderHistoryModel.stopPrice)
				.clientOrderId(orderHistoryModel.clientOrderId)
				.matchingPrice(orderHistoryModel.matchingPrice)
				;

		return ORDER_HISTORY_MODEL_DIRECT_BUFFER;
	}

	public void decodeByte(byte[] buffer, OrderHistoryModel orderHistoryModel) {
		final UnsafeBuffer directBuffer = new UnsafeBuffer(buffer);
		ORDER_HISTORY_MODEL_DECODER.wrap(directBuffer, 0, OrderHistoryModelDecoder.BLOCK_LENGTH, 0);
		orderHistoryModel.orderId = ORDER_HISTORY_MODEL_DECODER.orderId();
		orderHistoryModel.userId = ORDER_HISTORY_MODEL_DECODER.userId();
		orderHistoryModel.symbolId = ORDER_HISTORY_MODEL_DECODER.symbolId();
		orderHistoryModel.created = ORDER_HISTORY_MODEL_DECODER.created();
		orderHistoryModel.updated = ORDER_HISTORY_MODEL_DECODER.updated();
		orderHistoryModel.orderType = ORDER_HISTORY_MODEL_DECODER.orderType();
		orderHistoryModel.orderSide = ORDER_HISTORY_MODEL_DECODER.orderSide();
		orderHistoryModel.orderStatus = ORDER_HISTORY_MODEL_DECODER.orderStatus();
		orderHistoryModel.price = ORDER_HISTORY_MODEL_DECODER.price();
		orderHistoryModel.filled = ORDER_HISTORY_MODEL_DECODER.filled();
		orderHistoryModel.totalFilled = ORDER_HISTORY_MODEL_DECODER.totalFilled();
		orderHistoryModel.amount = ORDER_HISTORY_MODEL_DECODER.amount();
		orderHistoryModel.averagePrice = ORDER_HISTORY_MODEL_DECODER.averagePrice();
		orderHistoryModel.stopPrice = ORDER_HISTORY_MODEL_DECODER.stopPrice();
		orderHistoryModel.clientOrderId = ORDER_HISTORY_MODEL_DECODER.clientOrderId();
		orderHistoryModel.matchingPrice = ORDER_HISTORY_MODEL_DECODER.matchingPrice();
	}
}
