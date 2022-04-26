package org.ssi.replication.network;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.ssi.replication.process.ReplicationProcessor;
import org.ssi.replication.util.ConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.network.connection.FatalFailureMonitor;

public class ConnectionMonitor implements FatalFailureMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionMonitor.class);

	public void onConnected(SocketChannel channel, SocketAddress socketAddress) {
		LOG.info("Connected to server ");
	}

	public void onDisconnected(SocketChannel channel, SocketAddress socketAddress) {
		LOG.error("Disconnected connection");
		// retry connect here
	}

//	public void onReceiveMessage(Wire inWire, SocketChannel channel) {	
//		inWire.readDocument(null, data -> {
//            int msgtype= data.read(() -> "type").int32();
//            
//        	switch(msgtype) {
//        		case MsgType.HEARTBEAT_REPLY:        			
//        			LOG.info("Receive HEARTBEAT_REPLY");        			
//        			break;
//        		case MsgType.REPLICATED_REPLY:
//        			LOG.info("Receive REPLICATED_REPLY");
//        			
//        			break;
//        		case MsgType.SYNC_REPLICATED:
//        			LOG.info("Receive SYNC_REPLICATED ");
//        			
//        			break;
//        		case MsgType.REQUEST_TO_MASTER_REPLY:
//        			int result = inWire.read("result").int32();
//        			LOG.info("Receive REQUEST_TO_MASTER_REPLY");
//        			if(result != 0) {
//            			ReplicationProcessor.processToBeSlave();
//        			}
//        			break;
//        		case MsgType.REQUEST_TO_SLAVE_REPLY:
//        			LOG.info("Receive REQUEST_TO_SLAVE_REPLY");
//        			result = inWire.read("result").int32();
//        			if(result != 0) {
//            			ReplicationProcessor.processToBeSlave();
//        			}
//        			break;
//        		case MsgType.GRANT_TO_MASTER:
//        			LOG.info("Receive GRANT_TO_MASTER");
//        			processGrantToBeMaster(channel);        			
//        			break;
//        		
//        		default:
//        			LOG.warn("Not support this message type: {}",msgtype);
//        	}
//        });
//		
//	}

	public void onReceiveMessage(Bytes<?> bytesIn, SocketChannel channel) {
		bytesIn.readSkip(TCPClient.LENGTH_OF_MSG_SIZE);
		byte msgtype = bytesIn.readByte();

		switch (msgtype) {
		case MsgType.HEARTBEAT_REPLY:
			LOG.info("Receive HEARTBEAT_REPLY");
			break;
		case MsgType.REPLICATED_REPLY:
			LOG.info("Receive REPLICATED_REPLY");
			break;
		case MsgType.SYNC_REPLICATED:
			LOG.info("Receive SYNC_REPLICATED ");
			break;
		case MsgType.REQUEST_TO_MASTER_REPLY:
			byte result = bytesIn.readByte();
			LOG.info("Receive REQUEST_TO_MASTER_REPLY");
			if (result != 0) {
				ReplicationProcessor.processToBeSlave();
			}
			break;
		case MsgType.REQUEST_TO_SLAVE_REPLY:
			LOG.info("Receive REQUEST_TO_SLAVE_REPLY");
			result = bytesIn.readByte();
			if (result != 0) {
				ReplicationProcessor.processToBeSlave();
			}
			break;
		case MsgType.GRANT_TO_MASTER:
			LOG.info("Receive GRANT_TO_MASTER");
			processGrantToBeMaster(channel);
			break;

		default:
			LOG.warn("Not support this message type: {}", msgtype);
		}
	}

	public void onFatalFailure(String name, String message) {
		LOG.error("onFatalFailure: Can't connect to server after RETRY_NUM ");
	}

	public void onHeartBeatTimeout(String message) {
		LOG.error("onHeartBeatTimeout: Can't receive heartbeat from server after HEATBEAT_TIMEOUT_PERIOD milisecond ");
	}

	public void processGrantToBeMaster(SocketChannel socketChannel) {
		int result = ReplicationProcessor.processToBeMaster(false);
		if (socketChannel != null) {
//			Wire outWire = WireType.RAW.apply(elasticByteBuffer());			
//			outWire.writeDocument(false, wireOut -> {
//				wireOut.write(()->"type").int32(MsgType.GRANT_TO_MASTER_REPLY);
//				wireOut.write(()->"result").int32(result);
//			});
			if (result == 1) {
				ConnectionUtils.writeSocket(Messages.MSG_BYTES_GRANT_TO_MASTER_REPLY_OK, socketChannel);
			} else {
				ConnectionUtils.writeSocket(Messages.MSG_BYTES_GRANT_TO_MASTER_REPLY_FAIL, socketChannel);
			}
		}
	}

}
