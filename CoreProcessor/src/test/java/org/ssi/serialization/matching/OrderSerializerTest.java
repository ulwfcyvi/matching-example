package org.ssi.serialization.matching;

import static org.junit.Assert.assertTrue;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.ssi.io.BufferedChannel;
import org.ssi.model.Order;
import org.ssi.model.OrderSide;
import org.ssi.model.OrderStatus;
import org.ssi.model.OrderType;

public class OrderSerializerTest {
	
	@Test
	public void testEncodAndDecode() throws Exception {
		
		int size = 4096;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		RandomAccessFile file = new RandomAccessFile("order.dat", "rw");		
		BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());
		
		Order order = new Order();
		order.id = 1;
		order.timestamp = 100;
		order.symbol = 0;
		order.userId = 1;
		order.price = 100;
		order.amount = 100;
		order.filled = 10;
		order.side = OrderSide.ASK;
		order.type = OrderType.LIMIT;
		order.status = OrderStatus.FILLED;
		
		OrderSerializer.encode(order, channel);
		
		channel.flush();
		file.close();
		
		buffer = ByteBuffer.allocate(size);
		file = new RandomAccessFile("order.dat", "rw");		
		channel = new BufferedChannel(buffer, file.getChannel());
		
		Order newOrder = new Order();
		
		OrderSerializer.decode(newOrder, channel);
		
		file.close();
		
		assertTrue(order.equals(newOrder));
	} 
}
