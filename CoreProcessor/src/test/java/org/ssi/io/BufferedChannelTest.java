package org.ssi.io;

import static org.junit.Assert.assertTrue;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.junit.Test;

public class BufferedChannelTest {

	@Test
	public void testReadWrite() throws Exception {
		int size = 4096;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		RandomAccessFile file = new RandomAccessFile("test.dat", "rw");
		
		BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());
		
		channel.putInt(99999999).putLong(99999999).putByte((byte)10).flush();
		file.close();
		
		file = new RandomAccessFile("test.dat", "rw");

		channel.channel(file.getChannel());
		channel.buffer(buffer);
		
		int n1 = channel.getInt();
		long n2 = channel.getLong();
		byte n3 = channel.getByte();
		
		file.close();
		
		assertTrue(n1 == 99999999 && n2 == 99999999 && n3 == (byte)10);
	}
	
	@Test
	public void testReadWriteOutOfBuffer() throws Exception {
		int size = 4096;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		RandomAccessFile file = new RandomAccessFile("test.dat", "rw");
		
		BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());
		
		for(int i = 0; i < (4096 >> 2) - 1; i++) {
			channel.putInt(i);
		}
		
		channel.putLong(1024);
		
		file.close();
		
		assertTrue(buffer.position() == 8 && buffer.limit() == 4096);
		
		buffer = ByteBuffer.allocate(1024);		
		file = new RandomAccessFile("test.dat", "rw");
		
		channel = new BufferedChannel(buffer, file.getChannel());

		for(int i = 0; i < (4096 >> 2) - 1; i++) {
			int result = channel.getInt();
			assertTrue(result == i);
		}
		
		file.close();		
	}
}
