package org.ssi.replication.network;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import org.ssi.replication.process.ReplicationProcessor;
import org.ssi.replication.util.CoreExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import net.openhft.chronicle.bytes.Bytes;
//import net.openhft.chronicle.wire.Wire;

public class ReplicatedConnectionMonitor extends ConnectionMonitor{	
	private static final Logger LOG = LoggerFactory.getLogger(ReplicatedConnectionMonitor.class);	
	private static final int EVENTS_SIZE = 1024; 
	private Disruptor<Bytes<?>> disruptor;
	private RingBuffer<Bytes<?>> ringBuffer;
	private EventComsummer eventComsummer;
	
	public ReplicatedConnectionMonitor() {
		WaitStrategy waitStrategy = new BlockingWaitStrategy();
		eventComsummer = new EventComsummer();
        disruptor = new Disruptor<>(
        		ReplicatedConnectionMonitor::elasticByteBuffer,
                4 * 1024 * 1024,
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE,
                waitStrategy);
        
        disruptor.setDefaultExceptionHandler(new CoreExceptionHandler<Bytes<?>>());
        disruptor.handleEventsWith(eventComsummer);
        
        ringBuffer = disruptor.start();
	}
	
	@SuppressWarnings("deprecation")
	public void resetRingBuffer() {
		ringBuffer.resetTo(0);
	}
	
	static Bytes<?> elasticByteBuffer() {
        return Bytes.elasticByteBuffer(EVENTS_SIZE);
    }
	
	@Override
	public void onConnected(SocketChannel channel, SocketAddress socketAddress) {
		super.onConnected(channel, socketAddress);	
		ReplicationProcessor.slaveFirstSync();
	}
	
	
	@Override
	public void onReceiveMessage(Bytes<?> bytesIn, SocketChannel channel) {	
			bytesIn.readSkip(TCPClient.LENGTH_OF_MSG_SIZE);
			byte msgType = bytesIn.readByte();
        	switch(msgType) {        		
        		case MsgType.REPLICATED:
//        			if(ReplicationProcessor.getLastReplicateRemoteIndex() >= 0) {
	        			LOG.debug("Receive REPLICATED");
	        			final long sequenceId = ringBuffer.next();
	        			Bytes<?> event = ringBuffer.get(sequenceId);
	        			event.write(bytesIn);
	        			ReplicationProcessor.increaseTotalReceive();
	        			ringBuffer.publish(sequenceId);	        			
//        			}
        			break;
        		case MsgType.SYNC_REPLICATED:
        			long index = bytesIn.readLong();
        			LOG.debug("Receive SYNC_REPLICATED index {}",index);
        			ReplicationProcessor.slaveProcessSyncReplicate(index);
        			break;
        		case MsgType.HEARTBEAT_REPLY:        			
//        			LOG.info("Receive HEARTBEAT_REPLY. Time replicate {}",ReplicationProcessor.timeReplicate);    
        			LOG.info("Receive HEARTBEAT_REPLY");
        			if (ReplicationProcessor.getStatus() == 1 && ReplicationProcessor.isConsistency() == 1) {        				
        				ReplicationProcessor.processToBeSlave();
        			}
//        			if (ReplicationProcessor.isConsistency() == 1) {
//        				ReplicationProcessor.syncReplicate(channel);
//        			}
        			
        			break;
        		case MsgType.REQUEST_REPLICATED:
        			index = bytesIn.readLong(); 
        			LOG.info("Receive REQUEST_REPLICATED at index {}",index );        			
        			ReplicationProcessor.slaveReplicateEventBatch(index);
        			break;
        		case MsgType.REQUEST_TO_MASTER_REPLY:
        			byte result = bytesIn.readByte();
        			LOG.info("Receive REQUEST_TO_MASTER_REPLY");
        			if(result == 1) {
            			ReplicationProcessor.processToBeMaster(false);
        			}
        			break;
        		case MsgType.REQUEST_TO_SLAVE_REPLY:
        			LOG.info("Receive REQUEST_TO_SLAVE_REPLY");
        			result =  bytesIn.readByte();
        			if(result == 1) {
            			ReplicationProcessor.processToBeMaster(true);
        			}
        			break;
        		case MsgType.GRANT_TO_MASTER:
        			LOG.info("Receive GRANT_TO_MASTER");
        			processGrantToBeMaster(channel);        			
        			break;        		
        		default:
        			LOG.info("Not support message type: {}", msgType);
        	}
	}
	
	@Override
	public void onFatalFailure(String name, String message) {		
		super.onFatalFailure(name, message);
		//Start to be active server. Call function to do it
		ReplicationProcessor.processToBeMaster(true);
	}		
	
	@Override
	public void onHeartBeatTimeout(String message) {
		super.onHeartBeatTimeout(message);
		ReplicationProcessor.processToBeMaster(true);
	}
	
	
	public  void processGrantToBeMaster(SocketChannel socketChannel) {
		int result = ReplicationProcessor.processToBeMaster(false);
		
		if (result == 1) {
			HaTcpClient.sendMessage(Messages.MSG_BYTES_GRANT_TO_MASTER_REPLY_OK);
		}else {
			HaTcpClient.sendMessage(Messages.MSG_BYTES_GRANT_TO_MASTER_REPLY_FAIL);
		}
	}

}
