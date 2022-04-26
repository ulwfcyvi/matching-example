package org.ssi.replication.network;

import static net.openhft.chronicle.bytes.Bytes.elasticByteBuffer;

import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.chronicle.wire.WireType;

public class Messages {
	
	//=============================== WIRE  =======================================
	
//	public static final WireOut MSG_REQUEST_TO_MASTER_REPLY_OK = WireType.BINARY_LIGHT.apply(elasticByteBuffer(16));
//	public static final WireOut MSG_REQUEST_TO_MASTER_REPLY_FAIL = WireType.BINARY_LIGHT.apply(elasticByteBuffer(16));
//	public static final WireOut MSG_REQUEST_TO_SLAVE_REPLY_OK = WireType.BINARY_LIGHT.apply(elasticByteBuffer(16));
//	public static final WireOut MSG_REQUEST_TO_SLAVE_REPLY_FAIL = WireType.BINARY_LIGHT.apply(elasticByteBuffer(16));
//	public static final WireOut MSG_GRANT_TO_MASTER = WireType.BINARY_LIGHT.apply(elasticByteBuffer(8));
//	public static final WireOut MSG_GRANT_TO_MASTER_REPLY_OK = WireType.BINARY_LIGHT.apply(elasticByteBuffer(16));
//	public static final WireOut MSG_GRANT_TO_MASTER_REPLY_FAIL = WireType.BINARY_LIGHT.apply(elasticByteBuffer(16));
//	public static final WireOut MSG_REQUEST_TO_MASTER = WireType.BINARY_LIGHT.apply(elasticByteBuffer(8));
//	public static final WireOut MSG_REQUEST_TO_SLAVE = WireType.BINARY_LIGHT.apply(elasticByteBuffer(8));
//	
//
//	public static final WriteMarshallable DATA_REQUEST_TO_MASTER_REPLY_OK = wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.REQUEST_TO_MASTER_REPLY);
//		wireOut.write(() -> "result").int32(1);
//	};
//	
//	public static final WriteMarshallable DATA_REQUEST_TO_MASTER_REPLY_FAIL =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.REQUEST_TO_MASTER_REPLY);
//		wireOut.write(() -> "result").int32(0);
//	};
//	
//	
//	public static final WriteMarshallable DATA_REQUEST_TO_SLAVE_REPLY_OK =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.REQUEST_TO_MASTER_REPLY);
//		wireOut.write(() -> "result").int32(1);
//	};
//	
//	public static final WriteMarshallable DATA_REQUEST_TO_SLAVE_REPLY_FAIL =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.REQUEST_TO_SLAVE_REPLY);
//		wireOut.write(() -> "result").int32(0);
//	};
//	
//	public static final WriteMarshallable DATA_GRANT_TO_MASTER =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.GRANT_TO_MASTER);
//	};
//	
//	public static final WriteMarshallable DATA_GRANT_TO_MASTER_REPLY_OK =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.GRANT_TO_MASTER_REPLY);
//		wireOut.write(() -> "result").int32(1);
//	};
//	
//	public static final WriteMarshallable DATA_GRANT_TO_MASTER_REPLY_FAIL =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.GRANT_TO_MASTER_REPLY);
//		wireOut.write(() -> "result").int32(0);
//	};
//	
//	public static final WriteMarshallable DATA_REQUEST_TO_MASTER =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.REQUEST_TO_MASTER);
//	};
//	
//	public static final WriteMarshallable DATA_REQUEST_TO_SLAVE =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.REQUEST_TO_SLAVE);
//	};
//	
//	static {
//		MSG_REQUEST_TO_MASTER_REPLY_OK.writeDocument(DATA_REQUEST_TO_MASTER_REPLY_OK);
//		
//		MSG_REQUEST_TO_MASTER_REPLY_FAIL.writeDocument(DATA_REQUEST_TO_MASTER_REPLY_FAIL);
//		
//		MSG_REQUEST_TO_SLAVE_REPLY_OK.writeDocument(DATA_REQUEST_TO_SLAVE_REPLY_OK);
//		
//		MSG_REQUEST_TO_SLAVE_REPLY_FAIL.writeDocument(DATA_REQUEST_TO_SLAVE_REPLY_FAIL);
//		
//		MSG_GRANT_TO_MASTER.writeDocument(DATA_GRANT_TO_MASTER);		
//		
//		MSG_GRANT_TO_MASTER_REPLY_OK.writeDocument(DATA_GRANT_TO_MASTER_REPLY_OK);
//		
//		MSG_GRANT_TO_MASTER_REPLY_FAIL.writeDocument(DATA_GRANT_TO_MASTER_REPLY_FAIL);
//		
//		MSG_REQUEST_TO_MASTER.writeDocument(DATA_REQUEST_TO_MASTER);
//		
//		MSG_REQUEST_TO_SLAVE.writeDocument(DATA_REQUEST_TO_SLAVE);
//	}	
//
//	public static final WriteMarshallable HEARTBEAT =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.HEARTBEAT);
//	};
//	
//	public static final WriteMarshallable HEARTBEAT_REPLY =  wireOut -> {
//		wireOut.write(() -> "type").int32(MsgType.HEARTBEAT_REPLY);
//	};
	
	//========================================================================================================
	//=============================== BYTES =================================================================
	public static final WireOut MSG_BYTES_REQUEST_TO_MASTER_REPLY_OK = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_REQUEST_TO_MASTER_REPLY_FAIL = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_REQUEST_TO_SLAVE_REPLY_OK = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_REQUEST_TO_SLAVE_REPLY_FAIL = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_GRANT_TO_MASTER = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_GRANT_TO_MASTER_REPLY_OK = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_GRANT_TO_MASTER_REPLY_FAIL = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_REQUEST_TO_MASTER = WireType.RAW.apply(elasticByteBuffer(4));
	public static final WireOut MSG_BYTES_REQUEST_TO_SLAVE = WireType.RAW.apply(elasticByteBuffer(4));
	
	public static final WriteBytesMarshallable DATA_BYTES_REQUEST_TO_MASTER_REPLY_OK = bytesOut -> {
		bytesOut.writeByte(MsgType.REQUEST_TO_MASTER_REPLY);
		bytesOut.writeByte((byte)1);
	};
	

	public static final WriteBytesMarshallable DATA_BYTES_REQUEST_TO_MASTER_REPLY_FAIL =  bytesOut -> {
		bytesOut.writeByte(MsgType.REQUEST_TO_MASTER_REPLY);
		bytesOut.writeByte((byte)0);
	};
	
	
	public static final WriteBytesMarshallable DATA_BYTES_REQUEST_TO_SLAVE_REPLY_OK =  bytesOut -> {
		bytesOut.writeByte(MsgType.REQUEST_TO_MASTER_REPLY);
		bytesOut.writeByte((byte)1);
	};
	
	public static final WriteBytesMarshallable DATA_BYTES_REQUEST_TO_SLAVE_REPLY_FAIL =  bytesOut -> {
		bytesOut.writeByte(MsgType.REQUEST_TO_SLAVE_REPLY);
		bytesOut.writeByte((byte)0);
	};
	
	public static final WriteBytesMarshallable DATA_BYTES_GRANT_TO_MASTER =  bytesOut -> {
		bytesOut.writeByte(MsgType.GRANT_TO_MASTER);
	};
	
	public static final WriteBytesMarshallable DATA_BYTES_GRANT_TO_MASTER_REPLY_OK =  bytesOut -> {
		bytesOut.writeByte(MsgType.GRANT_TO_MASTER_REPLY);
		bytesOut.writeByte((byte)1);
	};
	
	public static final WriteBytesMarshallable DATA_BYTES_GRANT_TO_MASTER_REPLY_FAIL =  bytesOut -> {
		bytesOut.writeByte(MsgType.GRANT_TO_MASTER_REPLY);
		bytesOut.writeByte((byte)0);
	};
	
	public static final WriteBytesMarshallable DATA_BYTES_REQUEST_TO_MASTER =  bytesOut -> {
		bytesOut.writeByte(MsgType.REQUEST_TO_MASTER);
	};
	
	public static final WriteBytesMarshallable DATA_BYTES_REQUEST_TO_SLAVE =  bytesOut -> {
		bytesOut.writeByte(MsgType.REQUEST_TO_SLAVE);
	};
	
	static {
		MSG_BYTES_REQUEST_TO_MASTER_REPLY_OK.writeBytes(DATA_BYTES_REQUEST_TO_MASTER_REPLY_OK);
		
		MSG_BYTES_REQUEST_TO_MASTER_REPLY_FAIL.writeBytes(DATA_BYTES_REQUEST_TO_MASTER_REPLY_FAIL);
		
		MSG_BYTES_REQUEST_TO_SLAVE_REPLY_OK.writeBytes(DATA_BYTES_REQUEST_TO_SLAVE_REPLY_OK);
		
		MSG_BYTES_REQUEST_TO_SLAVE_REPLY_FAIL.writeBytes(DATA_BYTES_REQUEST_TO_SLAVE_REPLY_FAIL);
		
		MSG_BYTES_GRANT_TO_MASTER.writeBytes(DATA_BYTES_GRANT_TO_MASTER);		
		
		MSG_BYTES_GRANT_TO_MASTER_REPLY_OK.writeBytes(DATA_BYTES_GRANT_TO_MASTER_REPLY_OK);
		
		MSG_BYTES_GRANT_TO_MASTER_REPLY_FAIL.writeBytes(DATA_BYTES_GRANT_TO_MASTER_REPLY_FAIL);
		
		MSG_BYTES_REQUEST_TO_MASTER.writeBytes(DATA_BYTES_REQUEST_TO_MASTER);
		
		MSG_BYTES_REQUEST_TO_SLAVE.writeBytes(DATA_BYTES_REQUEST_TO_SLAVE);
	}	

	public static final WriteBytesMarshallable HEARTBEAT_BYTES =  bytesOut -> {
		bytesOut.writeByte(MsgType.HEARTBEAT);
	};
	
	public static final WriteBytesMarshallable HEARTBEAT_REPLY_BYTES =  bytesOut -> {
		bytesOut.writeByte(MsgType.HEARTBEAT_REPLY);
	};
	
	
}
