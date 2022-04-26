package org.ssi.response.model;

import org.ssi.io.BufferedChannel;

import java.io.IOException;

public class OrderHistoryModel {
	public long orderId;
	public long userId;
	public int symbolId;
	public long created;
	public long updated;
	public byte orderType;
	public byte orderSide;
	public byte orderStatus;
	public long price;
	public long filled;
	public long totalFilled;
	public long amount;
	public long averagePrice;
	public long stopPrice;
	public long clientOrderId;
	public long referId;
	public int brokerId;
	public long matchingPrice;
	public byte feeSide;
	public OrderHistoryModel() {
		
	}
	
	public OrderHistoryModel(long orderId, long userId, int symbolId, long created, long updated, byte orderType,
                             byte orderSide, byte orderStatus, long price, long filled, long totalFilled, long amount, long averagePrice,
                             long stopPrice, long clientOrderId, long referId, int brokerId, long matchingPrice, byte feeSide) {
		super();
		this.orderId = orderId;
		this.userId = userId;
		this.symbolId = symbolId;
		this.created = created;
		this.updated = updated;
		this.orderType = orderType;
		this.orderSide = orderSide;
		this.orderStatus = orderStatus;
		this.price = price;
		this.filled = filled;
		this.totalFilled = totalFilled;
		this.amount = amount;
		this.averagePrice = averagePrice;
		this.stopPrice = stopPrice;
		this.clientOrderId = clientOrderId;
		this.referId = referId;
		this.brokerId = brokerId;
		this.matchingPrice = matchingPrice;
		this.feeSide = feeSide;
	}

	public OrderHistoryModel(String s) {
		super();
		if (s == null || s.length() < 2)
			throw new IllegalArgumentException("Invalid parameters " + s);
		s = s.substring(1, s.length() - 1);
		String[] arr = s.split(",");
		if (arr == null || arr.length < 2)
			throw new IllegalArgumentException("Invalid parameters " + s);
		
		int index = 0;
		this.orderId = Long.parseLong(arr[index++]);
		this.userId = Long.parseLong(arr[index++]);
		
		this.symbolId = Integer.parseInt(arr[index++]);
		this.created = Long.parseLong(arr[index++]);
		this.orderType = Byte.parseByte(arr[index++]);
		this.orderSide = Byte.parseByte(arr[index++]);
		this.orderStatus = Byte.parseByte(arr[index++]);
		this.price = Long.parseLong(arr[index++]);
		this.filled = Long.parseLong(arr[index++]);          // 9
		this.totalFilled = Long.parseLong(arr[index++]);     // 10
		this.amount = Long.parseLong(arr[index++]);          // 11
		this.averagePrice = Long.parseLong(arr[index++]);
		this.stopPrice = Long.parseLong(arr[index++]);
		this.clientOrderId = Long.parseLong(arr[index++]);
		this.referId = Long.parseLong(arr[index++]);
		this.matchingPrice = Long.parseLong(arr[index++]);
		this.feeSide = Byte.parseByte(arr[index++]);
	}

	public void setData(OrderHistoryModel event) {
		this.orderId = event.orderId; 
		this.userId = event.userId;
		this.symbolId = event.symbolId;
		this.updated = event.updated;
		this.orderType = event.orderType;
		this.orderSide = event.orderSide;
		this.orderStatus = event.orderStatus;
		this.price = event.price;
		this.filled = event.filled;
		this.totalFilled = event.totalFilled;
		this.amount = event.amount;
		this.averagePrice = event.averagePrice;
		this.stopPrice = event.stopPrice;
		if (event.clientOrderId != 0) {
			this.clientOrderId = event.clientOrderId;
		}
		this.referId = event.referId;
		this.matchingPrice = event.matchingPrice;
		this.feeSide = event.feeSide;
	}
	
	public void reset() {
		this.orderId = 0;
		this.userId = 0;
		this.symbolId = 0;
		this.created = 0;
		this.updated = 0;
		this.orderType = 0;
		this.orderSide = 0;
		this.orderStatus = 0;
		this.price = 0;
		this.filled = 0;
		this.totalFilled = 0;
		this.amount = 0;
		this.averagePrice = 0;
		this.stopPrice = 0;
		this.clientOrderId = 0;
		this.referId = 0;
		this.brokerId = 1;
		this.matchingPrice = 0;
		this.feeSide = 0;
	}

	@Override
	public String toString() {
		return new StringBuffer("[")
		.append(orderId).append(",")
		.append(userId).append(",")
		.append(symbolId).append(",")
		.append(created).append(",")
		.append(updated).append(",")
		.append(orderType).append(",")
		.append(orderSide).append(",")
		.append(orderStatus).append(",")
		.append(price).append(",")
		.append(filled).append(",")
		.append(totalFilled).append(",")
		.append(amount).append(",")
		.append(averagePrice).append(",")
		.append(stopPrice).append(",")
		.append(clientOrderId).append(",")
		.append(referId).append(",")
		.append(brokerId).append(",")
		.append(matchingPrice).append(",")
		.append(feeSide)
		.append("]").toString();
	}
	public String toJsonString() {
		return new StringBuilder("{")
			.append("\"orderId\":").append(orderId).append(",")
			.append("\"userId\":\"").append(userId).append("\",")
			.append("\"symbolId\":\"").append(symbolId).append("\",")
			.append("\"created\":").append(created).append(",")
			.append("\"updated\":").append(updated).append(",")
			.append("\"orderType\":").append(orderType).append(",")
			.append("\"orderSide\":").append(orderSide).append(",")
			.append("\"orderStatus\":").append(orderStatus).append(",")
			.append("\"price\":").append(price).append(",")
			.append("\"filled\":").append(filled).append(",")
			.append("\"totalFilled\":").append(totalFilled).append(",")
			.append("\"amount\":").append(amount).append(",")
			.append("\"averagePrice\":").append(averagePrice).append(",")
			.append("\"stopPrice\":").append(stopPrice).append(",")
			.append("\"clientOrderId\":").append(clientOrderId).append(",")
			.append("\"referId\":").append(referId).append(",")
			.append("\"brokerId\":\"").append(brokerId).append("\"")
//			.append(",")
//			.append("\"matchingPrice\":").append(matchingPrice)
			
			.append("}").toString();
	}

	public void serialize(BufferedChannel channel) throws IOException {
		channel.putLong(orderId);
		channel.putLong(userId);
		channel.putInt(symbolId);
		channel.putLong(created);
		channel.putLong(updated);
		channel.putByte(orderType);
		channel.putByte(orderSide);
		channel.putByte(orderStatus);
		channel.putLong(price);
		channel.putLong(filled);
		channel.putLong(totalFilled);
		channel.putLong(amount);
		channel.putLong(averagePrice);
		channel.putLong(stopPrice);
		channel.putLong(clientOrderId);
		channel.putLong(referId);
		channel.putInt(brokerId);
		channel.putLong(matchingPrice);
		channel.putByte(feeSide);
	}
	
	public void deserialize(BufferedChannel channel) throws IOException {
		orderId = channel.getLong();
		userId = channel.getLong();
		symbolId = channel.getInt();
		created = channel.getLong();
		updated = channel.getLong();
		orderType = channel.getByte();
		orderSide = channel.getByte();
		orderStatus = channel.getByte();
		price = channel.getLong();
		filled = channel.getLong();
		totalFilled = channel.getLong();
		amount = channel.getLong();
		averagePrice = channel.getLong();
		stopPrice = channel.getLong();
		clientOrderId = channel.getLong();
		referId = channel.getLong();
		brokerId = channel.getInt();
		matchingPrice = channel.getLong();
		feeSide = channel.getByte();
	}
	
	public static void main(String args[]) {
		OrderHistoryModel o = new OrderHistoryModel();
		System.out.println(o.toJsonString());
	}
}
