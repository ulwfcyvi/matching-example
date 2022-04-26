package org.ssi.replication.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.apache.commons.io.FileUtils;
import org.ssi.io.BufferedChannel;
import org.ssi.replication.process.ReplicationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;

public class ReplicationServer {

	private static final Logger LOG = LoggerFactory.getLogger(ReplicationServer.class);
	private static final String BASE_DIR = "test/queuemaster/";
	private static final String INDEX_FILE_NAME = "replicatedIndex";
	private static final int BUFFER_SIZE = 128;
	private static final StringBuilder sb = new StringBuilder(256);
	private static ByteBuffer fileBuffer = ByteBuffer.allocate(64);
	private static final String QUEUE_PATH = "/data";
	static SingleChronicleQueue queue;
	static ExcerptAppender appender;
	static ExcerptTailer tailer;

	public static void main(String args[]) {
//		checkLoadSnapshot(false);
		
		initQueue();
		
//		saveSnapshot();
//		loadSnapshot();
		
//		int numberWrite = 400000;
//		writeNtoQueue(numberWrite);
		
		ReplicationProcessor.start("src/main/resources/ha_master.properties", new PublishEvent(appender),
				new HASwitcheImplTest(), appender, tailer, 3_000_000);

		testWithQueue(1,2000_000);
		
//		testConnection();
//		writeBatchToQueue(1);
//		testUpdateRecord();
	}

	public static void initQueue() {
		queue = ChronicleQueue.singleBuilder(BASE_DIR + QUEUE_PATH).strongAppenders(true).rollCycle(RollCycles.DAILY).timeoutMS(2000).build();
		appender = queue.acquireAppender();
		tailer = queue.createTailer();
	}

	public static void testWithQueue(int numberTest, int numberMsgOneTest) {
		LOG.info("Long max {}", Long.MAX_VALUE);
		LOG.info("Queue first index {}", appender.queue().firstIndex());
		LOG.info("Tailer index {}", tailer.index());
		LOG.info("Tailer end index {}", tailer.toEnd().index());
		LOG.info("Total event in queue {}", queue.entryCount());

		while (ReplicationProcessor.isConsistency() == 0) {
			Jvm.pause(2000);
			LOG.info("==> Waiting Sync data");
		}
		LOG.info("==> DONE Sync data");

		// =============== Write test ==================
		
//		int timeSleep = 0;

		LOG.info("START TESTING NOW. NUMBER OF TEST : {} ", numberTest);
		LOG.info("Tailer index {}", tailer.index());
		LOG.info("Tailer end index {}", tailer.toEnd().index());

		int j = 1;
		long timeStartTest = System.currentTimeMillis();
		long timeTest = 0;
		while (true) {
			if (j <= numberTest) {
				LOG.info("============================== TEST NO: {} ==============================", j);
				writeToQueue(j, numberMsgOneTest);
//				writeOneToQueue(j);
				j++;
			} else {
				if (timeTest == 0) {
					timeTest = System.currentTimeMillis() - timeStartTest;
				}

				LOG.info("NO MORE TEST. LAST WRITTEN INDEX {} ,tailer index {},Time test {}\n\n",
						appender.lastIndexAppended() + 1, tailer.index(), timeTest);
				Jvm.pause(10000);
			}

//			Jvm.pause(1000);
		}
	}

	private static void writeToQueue(int j, int numberMsgOneTest) {
		LOG.info(" APPEND {} ELEMENTS TO QUEUE", numberMsgOneTest);
		int i = 1;
		long timeStartWrite = System.currentTimeMillis();
		Bytes<?> samepleMsg = Bytes.wrapForRead((j+
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").getBytes());
		while (i <= numberMsgOneTest) {
//			Bytes<?> samepleMsg = Bytes.wrapForRead((j + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + i)
//					.getBytes()); // 80byte
			if (ReplicationProcessor.isWritealbe()) {
				ReplicationProcessor.write(samepleMsg);
				i++;
			}
		}
		LOG.info("Wrote Test No {}, Number of msg {}, time wrote {} ", j, i - 1,
				System.currentTimeMillis() - timeStartWrite);
	}
	
	private static void writeNtoQueue(int n) {
		LOG.info("WRITE {} event TO QUEUE", n);
		for(int i =0; i<n; i++) {
			byte[] data = (i + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
					.getBytes(); // 80byte
			
			appender.writeBytes(Bytes.wrapForRead(data));
		}
		
		
		LOG.info("\tQueue first index {}, total {}", appender.queue().firstIndex(),
				queue.entryCount());
	}

	private static void writeOneToQueue(int j) {
		LOG.info("WRITE ONE TO QUEUE");

		byte[] data = (j + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
				.getBytes(); // 80byte
		LOG.info("Write to queue msg length {} : {}", data.length, data);

		appender.writeBytes(Bytes.wrapForRead(data));
		LOG.info("\tQueue first index {}, LAST WRITE INDEX {}", appender.queue().firstIndex(),
				appender.lastIndexAppended());
	}

	private static void writeBatchToQueue(int j) {
		Bytes<?> buffer = Bytes.elasticByteBuffer();
		byte[] data = (j + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
				.getBytes();

		buffer.write(data);
		buffer.write(data);
		buffer.write(data);
		buffer.write(data);

		appender.writeBytes(buffer);

		Bytes<?> buffer2 = Bytes.elasticByteBuffer();
		tailer.readBytes(buffer2);
		System.out.println(buffer2);
		System.out.println("\n\n===================");
		buffer2.clear();
		tailer.readBytes(buffer2);
		System.out.println(buffer2);
	}

	public static void testConnection() {
		LOG.info("Queue first index {}", appender.queue().firstIndex());
		LOG.info("Tailer end index {}", tailer.toEnd().index());
		long localIndex = ReplicationProcessor.getLocalIndex();
		LOG.info("Last local index  {}", localIndex);
		while (true) {
			try {

				Thread.sleep(2000);
			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void testUpdateRecord() {
		long index = tailer.index();
		LOG.info("tailer index {}, Tailer end index {}, first Index {}, last appended index {}", tailer.index(),
				tailer.toEnd().index(), appender.queue().firstIndex(),
				appender.wire() == null ? 0 : appender.lastIndexAppended());

		if (index > 0) {
			tailer.moveToIndex(index);
			try (DocumentContext dc = tailer.readingDocument()) {
				if (!dc.isData()) {
					return;
				}

				Wire wire = dc.wire();
				Bytes<?> bytes = wire.bytes();
				LOG.info("{}", bytes);
			}
		}
		LOG.info("Tailer index {}", tailer.index());
		Jvm.pause(10000);
		for (int i = 0; i < 15; i++) {

			appender.writeBytes(Bytes.wrapForRead(
					((i +1) + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
							.getBytes()));

			LOG.info("Tailer index {}, last append Index {}, first Index{}", tailer.index(),
					appender.lastIndexAppended(), appender.queue().firstIndex());
			Jvm.pause(3000);
			try (DocumentContext dc = tailer.readingDocument()) {
				if (!dc.isData()) {
					return;
				}

				Wire wire = dc.wire();
				Bytes<?> bytes = wire.bytes();
//				long startReadPosition = bytes.readPosition();
//				LOG.info("Bytes read position {}, start {}",bytes.readPosition(), bytes.start());
//				byte a = bytes.readByte();
//				LOG.info("{}",a);
//				LOG.info("Bytes read position {}",bytes.readPosition());
//				bytes.readPosition(startReadPosition);
//				LOG.info("Bytes read position after reset {}",bytes.readPosition());
//				byte b = bytes.readByte();
//				LOG.info("{}",b);
				LOG.info("{}", bytes);
			}

		}

//		DocumentContext dc = tailer.readingDocument();
//		if (!dc.isData()) {
//			return;
//		}
//
//		Wire wire = dc.wire();
//		Bytes<?> bytes = wire.bytes();
//		LOG.info("{}", bytes);
//
//		wire.clear();
//		LOG.info("After clear {}", wire.bytes());
//		wire.writeBytes(b -> {
//			b.write("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb".getBytes());
//		});
//		LOG.info("After change {}", wire.bytes());
//
//		LOG.info(queue.dump());
//
//		index = tailer.index() - 1;
//		LOG.info("Read index {}", index);
//		tailer.moveToIndex(index);
//		DocumentContext dc2 = tailer.readingDocument();
//		if (!dc2.isData()) {
//			LOG.info("No data");
//			return;
//		}
//		Wire wire2 = dc2.wire();
//		Bytes<?> bytes2 = wire2.bytes();
//		LOG.info("{}", bytes2);

	}

	public static void checkLoadSnapshot(boolean yes) {
		if (yes) {
			loadSnapshot();
		} else {
			try {
				FileUtils.deleteDirectory(new File(BASE_DIR));
			} catch (IOException e) {
				LOG.error("Error when load snapshot replication {}", e);
				return;
			}
		}
	}

	public static void saveSnapshot() {
		if (appender != null) {
			long index = appender.queue().firstIndex();
			if (index == Long.MAX_VALUE) {
				index = 0;
			} else if (appender.wire() == null) {
				index = tailer.toEnd().index();
			} else {
				index = appender.lastIndexAppended() + 1;
			}
			LOG.info("====> Take snapshot for replication at index {}", index);
			sb.delete(0, sb.length());
			sb.append(BASE_DIR).append(INDEX_FILE_NAME);

			try (RandomAccessFile file = new RandomAccessFile(sb.toString(), "rw")) {

				BufferedChannel channel = new BufferedChannel(fileBuffer, file.getChannel());

				channel.putLong(index);
				channel.flush();
			} catch (Exception e) {
				LOG.error("ERROR", e);
			}
		}
	}

	public static void loadSnapshot() {
		sb.delete(0, sb.length());
		sb.append(BASE_DIR).append(INDEX_FILE_NAME);
		try (RandomAccessFile file = new RandomAccessFile(sb.toString(), "rw")) {
			BufferedChannel channel = new BufferedChannel(fileBuffer, file.getChannel());

			long lastIndex = channel.getLong();
			tailer.moveToIndex(lastIndex);
			LOG.info("====> Reload snapshot for replication service. Last Index  {}", lastIndex);
			channel.flush();
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}
	}

}
