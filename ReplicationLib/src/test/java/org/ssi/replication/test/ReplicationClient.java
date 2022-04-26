package org.ssi.replication.test;

import org.ssi.replication.process.ReplicationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.wire.DocumentContext;

public class ReplicationClient {
	private static final Logger LOG = LoggerFactory.getLogger(ReplicationClient.class);
	static ChronicleQueue queue  = ChronicleQueue.singleBuilder("test/queueslave").rollCycle(RollCycles.DAILY).strongAppenders(true)
			.timeoutMS(2000).build();		
	final static ExcerptAppender appender = queue.acquireAppender();
	final static  ExcerptTailer tailer = queue.createTailer();
	
	public static void main(String args[]) {	
			
		ReplicationProcessor.start("src/main/resources/ha_slave.properties", new PublishEvent(appender), new HASwitcheImplTest()
				, appender, tailer, 1_500_000);
		LOG.info("Queue first index {}", appender.queue().firstIndex());
		LOG.info("Queue tailer index {}", tailer.index());
		LOG.info("Long max {}",Long.MAX_VALUE);
		LOG.info("Tailer end index {}", tailer.toEnd().index());
		
//		testQueue();
//		testConnection();
		testWhenBecameMaster();
	}
	
	public static void testQueue() {
		System.out.println("\n\n-----------testWriteQueue----------------------");
		
		byte[] data ="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(); //80byte
		appender.writeBytes(Bytes.wrapForRead(data));
		
		LOG.info("Queue first index {}", appender.queue().firstIndex());
		LOG.info("Queue last index {}", appender.lastIndexAppended());
		LOG.info("Queue tailer index {}", tailer.index());
		LOG.info("Tailer end index {}", tailer.toEnd().index());		
		
		
		long firtIndex = appender.queue().firstIndex();
//		tailer.direction(TailerDirection.BACKWARD).moveToIndex(firtIndex);
		tailer.moveToIndex(firtIndex);
		LOG.info("After move, Queue tailer index {}", tailer.index());
		try (final DocumentContext dc = tailer.readingDocument()) {
			if (!dc.isData()) {
				return;
			}
			Bytes<?> bytes = dc.wire().bytes();
			
			int len = bytes.length();
			System.out.println(bytes);
            bytes.readSkip(len);
		}catch (Exception e) {
			LOG.error(e.getMessage());
		}
		LOG.info("After change direction and read one msg, Queue tailer index {}, end index {}", tailer.index(),tailer.toEnd().index());
		
		Bytes<?> bytes2 = Bytes.elasticByteBuffer(128);
		tailer.readBytes(bytes2);
		System.out.println(bytes2);
//		System.out.println(moved);
	}
	
	public static void testWhenBecameMaster() {
		int count =0;
		while(true) {
			if(ReplicationProcessor.isWritealbe()) {
				count++;
				ReplicationProcessor.write((Bytes.wrapForRead((count +
				        		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").getBytes())));
				
//				LOG.info("NOW IS MASTER. write msg to queue at index {}, total {}", appender.lastIndexAppended(), count);//				
			}
			Jvm.pause(10);
		}
	}
	
	public static void testConnection() {
		while(true) {
			Jvm.pause(2000);
		}
	}
}
