package org.ssi.replication.test;

import org.ssi.replication.process.IPublishData;
import org.ssi.replication.process.ReplicationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ExcerptAppender;

public class PublishEvent implements IPublishData{
	private static final Logger LOG = LoggerFactory.getLogger(PublishEvent.class);
	ExcerptAppender appender;
	 
	 public PublishEvent(ExcerptAppender appender) {
		// TODO Auto-generated constructor stub
		 this.appender = appender;
	}

	@Override
	public void publishData(Bytes<?> data) {
		if(ReplicationProcessor.isWritealbe()) {
			LOG.info("kkkkkkkkkkkkkkkkk");
			appender.writeBytes(data);
		}
	}

}
