package org.ssi.serialization.matching;

import java.io.IOException;

import org.ssi.io.BufferedChannel;
import org.ssi.model.Order;

public class OrderSerializer {
	
	public static void encode(Order order, BufferedChannel channel) throws IOException {
		channel.putLong(order.id);
		channel.putLong(order.timestamp);
		channel.putInt(order.symbol);
		channel.putLong(order.userId);
		
		channel.putLong(order.price);
		channel.putLong(order.stopPrice);
		channel.putLong(order.amount);
		channel.putLong(order.filled);
		channel.putDouble(order.filledCost);
		
		channel.putByte(order.side);
		channel.putByte(order.type);
		channel.putByte(order.status);
		channel.putByte(order.tradeOption);
	}
	
	public static void decode(Order order, BufferedChannel channel) throws IOException {
		order.id = channel.getLong();
		order.timestamp = channel.getLong();
		order.symbol = channel.getInt();
		order.userId = channel.getLong();
		
		order.price = channel.getLong();
		order.stopPrice = channel.getLong();
		order.amount = channel.getLong();
		order.filled = channel.getLong();
		order.filledCost = channel.getDouble();
		
		order.side = channel.getByte();
		order.type = channel.getByte();
		order.status = channel.getByte();
		order.tradeOption = channel.getByte();
	}
}
