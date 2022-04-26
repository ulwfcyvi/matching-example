package org.ssi.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferedChannel {
	
	private static int BYTE_SIZE = 1;
	//private static int CHAR_SIZE = 1;
	private static int SHORT_SIZE = 2;
	private static int INT_SIZE = 4;
	private static int FLOAT_SIZE = 4;
	private static int LONG_SIZE = 8;
	private static int DOUBLE_SIZE = 8;
	
	private ByteBuffer buffer;
	private FileChannel channel;
	
	public BufferedChannel(ByteBuffer buf, FileChannel fc) {
		buffer = buf;
		channel = fc;
		buffer.position(0);
		buffer.limit(0);
	}
	
	public void channel(FileChannel fc) {
		channel = fc;
		buffer.position(0);
		buffer.limit(0);
	}
	
	public void buffer(ByteBuffer buf) {
		buffer = buf;
		buffer.position(0);
		buffer.limit(0);
	}
	
	public ByteBuffer buffer() {
		return buffer;
	}
	
	public BufferedChannel putByte(byte val) throws IOException {
		int remaining = buffer.remaining();
		if(remaining < BYTE_SIZE) {
			flush();
		}			
		
		buffer.put(val);
		return this;
	}
	
	public BufferedChannel putInt(int val) throws IOException {
		int remaining = buffer.remaining();
		if(remaining < INT_SIZE) {
			flush();
		}			
		
		buffer.putInt(val);
		
		return this;
	}	
	
	public BufferedChannel putShort(short val) throws IOException {
		int remaining = buffer.remaining();
		if(remaining < SHORT_SIZE) {
			flush();
		}			
		
		buffer.putShort(val);
		return this;
	}
	
	public BufferedChannel putFloat(float val) throws IOException {
		int remaining = buffer.remaining();
		if(remaining < FLOAT_SIZE) {
			flush();
		}			
		
		buffer.putFloat(val);
		return this;
	}
	
	public BufferedChannel putLong(long val) throws IOException {
		int remaining = buffer.remaining();
		if(remaining < LONG_SIZE) {
			flush();
		}			
		
		buffer.putLong(val);
		return this;
	}
	
	public BufferedChannel putDouble(double val) throws IOException {
		int remaining = buffer.remaining();
		if(remaining < DOUBLE_SIZE) {
			flush();
		}			
		
		buffer.putDouble(val);
		return this;
	}
	
	public byte getByte() throws IOException {
		int remaining = buffer.remaining();
		if(remaining < BYTE_SIZE) {
			fill();
		}			
		
		return buffer.get();
	}	
	
	public short getShort() throws IOException {
		int remaining = buffer.remaining();
		if(remaining < SHORT_SIZE) {
			fill();
		}			
		
		return buffer.getShort();
	}
	
	public int getInt() throws IOException {
		int remaining = buffer.remaining();

		if(remaining < INT_SIZE) {
			fill();
		}			
		
		return buffer.getInt();
	}
	
	public float getFloat() throws IOException {
		int remaining = buffer.remaining();
		if(remaining < FLOAT_SIZE) {
			fill();
		}			
		
		return buffer.getFloat();
	}
	
	public long getLong() throws IOException {
		int remaining = buffer.remaining();
		if(remaining < LONG_SIZE) {
			fill();
		}			
		
		return buffer.getLong();
	}
	
	public double getDouble() throws IOException {
		int remaining = buffer.remaining();
		if(remaining < DOUBLE_SIZE) {
			fill();
		}			
		
		return buffer.getDouble();
	}
	
	public void fill() throws IOException {
		buffer.compact();
		channel.read(buffer);
		buffer.flip();
	}
	
	public void flush() throws IOException {
		buffer.flip();
		channel.write(buffer);
		buffer.clear();
	}
}
