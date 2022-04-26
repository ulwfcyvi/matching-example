package org.ssi.replication.network;

public class MsgType {
	public static final byte HEARTBEAT = 0;
	public static final byte HEARTBEAT_REPLY = 1;
	public static final byte REQUEST_TO_MASTER = 2;
	public static final byte REQUEST_TO_MASTER_REPLY = 3;
	public static final byte GRANT_TO_MASTER = 4;
	public static final byte GRANT_TO_MASTER_REPLY = 5;
	public static final byte REQUEST_TO_SLAVE = 6;
	public static final byte REQUEST_TO_SLAVE_REPLY = 7;
	public static final byte REPLICATED = 8;
	public static final byte REPLICATED_REPLY = 9;
	public static final byte SYNC_REPLICATED = 10;	
	public static final byte REQUEST_REPLICATED = 11;
	public static final byte ACK_PROCESSED = 12;
}

