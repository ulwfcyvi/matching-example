package org.ssi.replication.network;


import org.ssi.replication.process.ReplicationProcessor;

import com.lmax.disruptor.EventHandler;

import net.openhft.chronicle.bytes.Bytes;

public class EventComsummer implements EventHandler<Bytes<?>>{
//	private static final Logger LOG = LoggerFactory.getLogger(EventComsummer.class);
	@Override
	public void onEvent(Bytes<?> event, long sequence, boolean endOfBatch) throws Exception {
		
//		if(ReplicationProcessor.getLastReplicateRemoteIndex() >= 0) {
			ReplicationProcessor.slaveProcessReplicateEvent(event);		
			event.release();
//		}else {
//			LOG.info("IN PROGRESS SYNC DATA. Drop even.");
//		}
	}
}
