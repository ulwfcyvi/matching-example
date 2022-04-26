package org.ssi.replication.process;

import java.io.IOException;

import org.ssi.replication.network.HaTcpClient;
import org.ssi.replication.network.HaTcpServer;
import org.ssi.replication.network.MsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.WireOut;

public class ReplicationProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(ReplicationProcessor.class);
	private static byte isConsistency = 0;
	private static byte hasEvent = 0;
	public static int BATCH_SIZE = 1000000;
	private static ExcerptTailer tailer;
	private static ExcerptAppender appender;
	private static IPublishData dataPublisher;
	private static HASwitcher processHA;
	private static byte status = 1;
	private static final byte REMOVED_EVENT_IDENTY = (byte) -111;
	private static Bytes<?> eventsBufferWriter = Bytes.elasticByteBuffer(2 * 1024 * 1024);
	private static Bytes<?> eventsBufferReader = Bytes.elasticByteBuffer(128);
	private static long firstQueueIndex = 0;
	private static long totalEventInQueue = 0;
	private static boolean peerIsMaster = false;
	private static long lastReplicatedIndex = 0;
	private static long previousReplicatedIndexOfSlave = 0;
	private static boolean connectedToSlave = false;
	private static SingleChronicleQueue queue;
	private static long totalMsgReceived = 0;
	private static long totalMsgProcess = 0;
	private static long lauchedIndex = 0;
	private static long timeWaitSlaveSync = 0;
	private static long numberInvalidEvent = 0;

//=== open when test perfromance
//		private static long startTimeReplicate = 0;
//		public static long timeReplicate = 0;

	public static void write(Bytes<?> bytes) {
		appender.writeBytes(bytes);
		totalEventInQueue++;
		hasEvent = 1;
	}

	public static void slaveProcessReplicateEvent(Bytes<?> bytesIn) {
//		if (startTimeReplicate == 0) {
//			startTimeReplicate = System.currentTimeMillis();
//		}

		int batchLength = bytesIn.readInt();
		long beginRemoteIndex = bytesIn.readLong();
//		LOG.info("readPosition {}, readRemaining {}, beginRemoteIndex {}", bytesIn.readPosition(), bytesIn.readRemaining(), beginRemoteIndex);
		long lastLocalNextIndex = getLocalIndex();

		if (beginRemoteIndex >= 0) {
			lastReplicatedIndex = beginRemoteIndex;
			if (lastLocalNextIndex != 0 && beginRemoteIndex != lastLocalNextIndex) {
				LOG.info("NOTICEMENT: Sync Index not match: local {}, remote {}. Sync index now", lastLocalNextIndex,
						beginRemoteIndex);
				isConsistency = 0;
				totalMsgProcess++;
				slaveSyncReplicate(lastLocalNextIndex);
				return;
			}
		}
//		LOG.info("RECEVIE: {} bytes, lastReplicated {}, lastLocalNextIndex {}, bytesIn.readPosition() {}",batchLength,lastReplicatedIndex, lastLocalNextIndex, bytesIn.readPosition());

		if (beginRemoteIndex < 0) {
			while (bytesIn.readPosition() < batchLength) {
				eventsBufferReader.writeByte(REMOVED_EVENT_IDENTY);
				bytesIn.read(eventsBufferReader, bytesIn.readByte());
				appender.writeBytes(eventsBufferReader);
				totalEventInQueue++;
				numberInvalidEvent++;

				lastReplicatedIndex++;
				eventsBufferReader.clear();
			}
		} else {
			while (bytesIn.readPosition() < batchLength) {
				bytesIn.read(eventsBufferReader, bytesIn.readByte());
				if (lauchedIndex == 0) {
					appender.writeBytes(eventsBufferReader);
					totalEventInQueue++;
				} else {
					lauchedIndex++;
					if (lauchedIndex == totalEventInQueue) {
						LOG.info(
								"Replicated from master, lauchedIndex equals totalEventInQueue: {}. Set lauchedIndex = 0",
								totalEventInQueue);
						lauchedIndex = 0;
					}
				}

				if (dataPublisher != null) {
					byte isRemovedEvent = eventsBufferReader.readByte();
					if (isRemovedEvent != REMOVED_EVENT_IDENTY) {
						eventsBufferReader.readPosition(eventsBufferReader.readPosition() - 1);
						dataPublisher.publishData(eventsBufferReader);
					}
				}

				lastReplicatedIndex++;
				eventsBufferReader.clear();
			}
		}
		totalMsgProcess++;
//		timeReplicate = System.currentTimeMillis() - startTimeReplicate;
		LOG.info(
				"Received from master beginRemoteIndex {}. lastReplicated {}, total {}, lauchedIndex {}, invalidEvent {}",
				beginRemoteIndex, lastReplicatedIndex, totalEventInQueue, lauchedIndex, numberInvalidEvent);

	}

	public static void slaveReplicateEventBatch(long remoteEventNumber) {
//		if(lauchedIndex > remoteEventNumber) {
//			lauchedIndex = 0;
//		}
		long numberAvaiable = moveToIndex(remoteEventNumber);
		if (numberAvaiable < 0) {
			return;
		}
		final long size = numberAvaiable > BATCH_SIZE ? BATCH_SIZE : numberAvaiable;
		lastReplicatedIndex = remoteEventNumber;
		int totalReadedByte = 0;
		int countSendEvent = 0;
		int countReadedEvent = 0;
		for (long i = 0; i < size; i++) {
			try (final DocumentContext dc = tailer.readingDocument()) {
				if (!dc.isData()) {
					break;
				}

				Bytes<?> bytes = dc.wire().bytes();
				int len = bytes.length();
				byte isRemoveEvent = bytes.readByte();

				if (isRemoveEvent != REMOVED_EVENT_IDENTY
						|| (numberInvalidEvent <= 0 && (lauchedIndex == 0 || lastReplicatedIndex <= lauchedIndex))) {
					eventsBufferWriter.writeByte((byte) len);
					eventsBufferWriter.write(bytes, bytes.readPosition() - 1, len);
					bytes.readSkip(len - 1);
					totalReadedByte += len + 1;
					countSendEvent++;
				} else {
					numberInvalidEvent--;
				}
				countReadedEvent++;
			} catch (Exception e) {
				LOG.error("{}", e);
			}
		}

		if (totalReadedByte > 0) {
			int totalLength = totalReadedByte;
			HaTcpClient.sendMessage(bytesOut -> {
				bytesOut.writeByte(MsgType.REPLICATED);
				bytesOut.writeInt(totalLength);
				bytesOut.write(eventsBufferWriter.toByteArray());
			});
			lastReplicatedIndex += countReadedEvent;
			eventsBufferWriter.clear();
			LOG.info(
					"Send to Master from index {}, got {}, send {}, lastReplicated {}, lauchedIndex {}, invalidEvent {}, total {}",
					remoteEventNumber, size, countSendEvent, lastReplicatedIndex, lauchedIndex, numberInvalidEvent,
					totalEventInQueue);
		} else {
			if (lastReplicatedIndex == totalEventInQueue) {
				slaveSyncReplicateWithStatus(2);
			} else {
				LOG.info("Replicate to master: Not found valid event. Size reading : {}, readed {}", size,
						countReadedEvent);
				slaveReplicateEventBatch(remoteEventNumber + size);
			}

		}
	}

	public static void slaveProcessSyncReplicate(final long remoteIndex) {
		if (lauchedIndex == remoteIndex) {
			lauchedIndex = 0;
		}
		long localIndex = getLocalIndex();

		LOG.info("process SyncReplicate remoteIndex {}, localIndex {}, lastReplicated {}", remoteIndex, localIndex,
				lastReplicatedIndex);
		if (localIndex < remoteIndex) {
			isConsistency = 0;
			slaveRequestReplicate(localIndex);
		} else if (localIndex > remoteIndex) {
			lauchedIndex = 0;
			isConsistency = 0;
			if (lastReplicatedIndex > remoteIndex) {
				lastReplicatedIndex = remoteIndex;
			}
			slaveReplicateEventBatch(lastReplicatedIndex);
		} else {
			lastReplicatedIndex = remoteIndex;
			LOG.info("DATA IS CONSISTENCY at {}", lastReplicatedIndex);
			if (status == 1) {
				processToBeSlave();
			}
			slaveSyncReplicate();
			isConsistency = 1;
			lauchedIndex = 0;
		}
	}

	// sync the index
	public static void slaveSyncReplicate() {
		long index = getLocalIndex();
		LOG.info("Send SYNC local index: {}", index);
		HaTcpClient.sendMessage(bytesOut -> {
			bytesOut.writeByte(MsgType.SYNC_REPLICATED);
			bytesOut.writeLong(index);
			bytesOut.writeByte(status);
		});
	}

	public static void slaveSyncReplicateWithStatus(int status) {
		long index = getLocalIndex();
		LOG.info("Send SYNC local index: {}, status {}", index, status);
		HaTcpClient.sendMessage(bytesOut -> {
			bytesOut.writeByte(MsgType.SYNC_REPLICATED);
			bytesOut.writeLong(index);
			bytesOut.writeByte((byte) 2);
		});
	}

	// sync the index
	public static void slaveSyncReplicate(final long index) {
		LOG.info("Send SYNC at Local Index {}", index);
		HaTcpClient.sendMessage(byteOut -> {
			byteOut.writeByte(MsgType.SYNC_REPLICATED);
			byteOut.writeLong(index);
			byteOut.writeByte(status);
		});
	}

	// sync the index
	public static void slaveRequestReplicate(final long index) {
		HaTcpClient.sendMessage(byteOut -> {
			byteOut.writeByte(MsgType.REQUEST_REPLICATED);
			byteOut.writeLong(index);
		});
	}

	public static void slaveFirstSync() {
		long localIndex = getLocalIndex();
		if (lastReplicatedIndex > 0 && status == 1) {
			slaveSyncReplicate(lastReplicatedIndex);
//			if (lastReplicatedIndex == localIndex) {
//				LOG.info("No event come after master down");
//				processToBeSlave();
//			}			
		} else {
			slaveSyncReplicate(localIndex);
		}
	}

//================================================================================================
//================================================================================================
//================================================================================================

	public static void masterProcessSyncReplicate(long remoteIndex, byte slaveStatus, WireOut outWire) {

		long localIndex = getLocalIndex();
		LOG.info("process SyncReplicate remoteIndex {}, localIndex {}", remoteIndex, localIndex);
		if (localIndex < remoteIndex) {

			isConsistency = 0;
			peerIsMaster = true;
			hasEvent = 0;
			connectedToSlave = true;
			lastReplicatedIndex = remoteIndex;
			if (slaveStatus != 2) {
				masterRequestReplicate(localIndex, outWire);
			} else {
				lauchedIndex = 0;
				if (totalEventInQueue > remoteIndex) {
					hasEvent = 1;
					previousReplicatedIndexOfSlave = totalEventInQueue;
				} else {
					masterSyncReplicate();
				}
			}
		} else if (localIndex > remoteIndex) {
			if (lauchedIndex > 0) {
				lauchedIndex = remoteIndex;
			}
			if (slaveStatus != 0) {
				peerIsMaster = true;
				previousReplicatedIndexOfSlave = remoteIndex;
				LOG.info("Slave is master but local index {} bigger than slave index {}", localIndex, remoteIndex);
			} else {
				peerIsMaster = false;
			}

			isConsistency = 0;
			lastReplicatedIndex = remoteIndex;
			hasEvent = 1;
			connectedToSlave = true;
		} else {
			lastReplicatedIndex = remoteIndex;
			if (slaveStatus != 0) {
				LOG.info("Slave is master and local index equals slave index {}", remoteIndex);
				masterSyncReplicate(remoteIndex, outWire);
				peerIsMaster = true;
			} else {
				peerIsMaster = false;
				isConsistency = 1;
				lauchedIndex = 0;
				LOG.info("DATA IS CONSISTENCY at index {}", lastReplicatedIndex);
			}
			connectedToSlave = true;
		}

	}

	public static void masterProcessReplicateEvent(Bytes<?> bytesIn, WireOut outWire) {
		int batchLength = bytesIn.readInt();
		int count = 0;
		while (bytesIn.readPosition() < batchLength) {
			int eventLength = bytesIn.readByte();
			bytesIn.read(eventsBufferReader, eventLength);

			count++;
			// in case load snapshot
			if (lauchedIndex == 0) {

//				LOG.info("Replicated event {}", eventsBufferReader);
				appender.writeBytes(eventsBufferReader);
				totalEventInQueue++;
				lastReplicatedIndex++;
			} else {
				lauchedIndex++;
				if (lauchedIndex >= lastReplicatedIndex) {
					lauchedIndex = 0;
					LOG.info("lauchedIndex = lastReplicatedIndex {} so Set lauchedIndex =0. Total in queue {}",lastReplicatedIndex, totalEventInQueue);
					if (lastReplicatedIndex < totalEventInQueue) {
						LOG.info("lastReplicatedIndex {} < totalEventInQueue {}", lastReplicatedIndex,
								totalEventInQueue);
						previousReplicatedIndexOfSlave = lastReplicatedIndex;
						hasEvent = 1;
						peerIsMaster = true;
						eventsBufferReader.clear();
						return;
					}
				}
			}

			if (dataPublisher != null) {
				byte isRemovedEvent = eventsBufferReader.readByte();
				if (isRemovedEvent != REMOVED_EVENT_IDENTY) {
					eventsBufferReader.readPosition(eventsBufferReader.readPosition() - 1);
					dataPublisher.publishData(eventsBufferReader);
				}
			}

			eventsBufferReader.clear();
		}

		LOG.info("Received {} events from slave, lastReplicatedIndex {}, lauchedIndex {}, totalInQueue {}", count,
				lastReplicatedIndex, lauchedIndex, totalEventInQueue);
		masterSyncReplicate(outWire);
	}

	public static void replicateToSlave() {

//		if (lastReplicatedIndex == 0) {
//			lastReplicatedIndex = appender.queue().firstIndex();
//		}

		final long numberAvaiable = moveToIndex(lastReplicatedIndex);// getLocalIndex() - lastReplicatedIndex;

		if (numberAvaiable > 0) {
			final long size = numberAvaiable > BATCH_SIZE ? BATCH_SIZE : numberAvaiable;
			int numberReaded = 0;
			long beginIndex = lastReplicatedIndex;
			long count = 0;
			for (long i = 0; i < size; i++) {
				try (final DocumentContext dc = tailer.readingDocument()) {
					if (!dc.isData()) {
						hasEvent = 0;
						LOG.info("Queue is empty, set hasEvent = 0, last replicated index {}", lastReplicatedIndex);
						break;
					}
					Bytes<?> bytes = dc.wire().bytes();
					int len = bytes.length();
//					if(len > 86) {
//						LOG.info("Len {}, count {},  msg {}, bytes.readPosition {}", len,count +1, bytes, bytes.readPosition());
//					}
					eventsBufferWriter.writeByte((byte) len);
					eventsBufferWriter.write(bytes, bytes.readPosition(), len);
					bytes.readSkip(len);
					numberReaded += len + 1;

					count++;
				}
			}
			if (numberReaded > 0) {
				int totalLength = numberReaded + 8;

				if (HaTcpServer.sendMessage(bytesOut -> {

					bytesOut.writeByte(MsgType.REPLICATED);
					bytesOut.writeInt(totalLength);

					if (peerIsMaster) {
						bytesOut.writeLong(-1);
					} else {
						bytesOut.writeLong(beginIndex);
					}
					bytesOut.write(eventsBufferWriter);
				})) {
					LOG.info("replicated to Slave from index {}, quantity {}, totalEventInQueue {}",
							lastReplicatedIndex, count, totalEventInQueue);
					lastReplicatedIndex += count;
				} else {
					lastReplicatedIndex = beginIndex;
					connectedToSlave = false;
					LOG.error("Connection to Slave was closed");
				}
				eventsBufferWriter.clear();

			}

		} else {
			hasEvent = 0;
			LOG.info("Number avaiable event = 0, last replicated {}", lastReplicatedIndex);
			if (peerIsMaster) {
				LOG.info("Waiting slave sync data...");
				Jvm.pause(timeWaitSlaveSync);
				masterSyncReplicate(previousReplicatedIndexOfSlave);
			} else {
				isConsistency = 1;
				lauchedIndex = 0;
			}
		}
	}

	public static void masterSendReplicateEventBatch(long remoteIndex, WireOut outWire) {

		final long numberAvaiable = moveToIndex(lastReplicatedIndex);
		LOG.info("Replicate to slave from index {} , quantity {}", lastReplicatedIndex, numberAvaiable);
		if (numberAvaiable > 0) {
			final long size = numberAvaiable > BATCH_SIZE ? BATCH_SIZE : numberAvaiable;
			int numberReaded = 0;
			long count = 0;
			for (long i = 0; i < size; i++) {
				try (final DocumentContext dc = tailer.readingDocument()) {
					if (!dc.isData()) {
						hasEvent = 0;
						LOG.info("Queue is empty, set hasEvent = 0, last replicated index {}", lastReplicatedIndex);
						break;
					}
					Bytes<?> bytes = dc.wire().bytes();
					int len = bytes.length();
					eventsBufferWriter.writeByte((byte) len);
					eventsBufferWriter.write(bytes, bytes.readPosition() - 1, len);
					bytes.readSkip(len);
					numberReaded += len + 1;
					count++;
				}
			}
			long beginIndex = remoteIndex;

			if (numberReaded > 0) {
				int totalLength = numberReaded;
				outWire.writeBytes(bytesOut -> {
					bytesOut.writeByte(MsgType.REPLICATED);
					bytesOut.writeInt(totalLength);
					if (peerIsMaster) {
						bytesOut.writeLong(-1);
					} else {
						bytesOut.writeLong(beginIndex);
					}
					bytesOut.write(eventsBufferWriter);
				});
				lastReplicatedIndex += count;
//				LOG.info("Send to slave  {} , lastReplicatedIndex {}", count, lastReplicatedIndex);
				eventsBufferWriter.clear();
			} else {
				masterSyncReplicate(outWire);
			}
		} else {
			hasEvent = 0;
			if (peerIsMaster) {
				masterSyncReplicate(outWire);
			} else {
				lauchedIndex = 0;
				isConsistency = 1;
			}
		}
	}

	private static void masterRequestReplicate(long localIndex, WireOut outWire) {
		LOG.info("Send REQUEST REPLICATE at local index {}", localIndex);
		outWire.writeBytes(bytesOut -> {
			bytesOut.writeByte(MsgType.REQUEST_REPLICATED);
			bytesOut.writeLong(localIndex);
		});
	}

	public static void masterSyncReplicate(WireOut outWire) {
		long index = getLocalIndex();
		LOG.info("Send SYNC at local index {}", index);

		outWire.writeBytes(bytesOut -> {
			bytesOut.writeByte(MsgType.SYNC_REPLICATED);
			bytesOut.writeLong(index);
		});
	}

	public static void masterSyncReplicate(long index, WireOut outWire) {
		LOG.info("Send SYNC at local index {}", index);

		outWire.writeBytes(bytesOut -> {
			bytesOut.writeByte(MsgType.SYNC_REPLICATED);
			bytesOut.writeLong(index);
		});
	}

	public static void masterSyncReplicate(long index) {
		LOG.info("Send SYNC at local index {}", index);

		HaTcpServer.sendMessage(bytesOut -> {
			bytesOut.writeByte(MsgType.SYNC_REPLICATED);
			bytesOut.writeLong(index);
		});
	}

	public static void masterSyncReplicate() {
		long index = getLocalIndex();
		LOG.info("Master Send SYNC at local index {}", index);
		HaTcpServer.sendMessage(byteOut -> {
			byteOut.writeByte(MsgType.SYNC_REPLICATED);
			byteOut.writeLong(index);
		});
	}

// ================================================================================================
// ================================================================================================
// ================================================================================================

	public static long moveToIndex(long numberEvent) {
		long firstIndex = getFirstIndex();
		if (numberEvent == 0) {
			if (!tailer.moveToIndex(firstIndex)) {
				return -1;
			}
			return queue.entryCount();
		}

		int lastCycle = queue.lastCycle();
		long index = queue.rollCycle().toIndex(lastCycle, 0);
		long totalToCycle = queue.countExcerpts(firstIndex, index);

		if (totalToCycle > numberEvent) {
			while (totalToCycle > numberEvent) {
				lastCycle--;
				totalToCycle -= queue.exceptsPerCycle(lastCycle);
			}
		}

		index = queue.rollCycle().toIndex(lastCycle, numberEvent - totalToCycle);
		long available = queue.entryCount() - numberEvent;
		if (available > 0) {
			if (!tailer.moveToIndex(index)) {
				LOG.info("Move to index {} failed", index);
				return -1;
			}
		}

		return available;
	}

	public static final byte isConsistency() {
		return isConsistency;
	}

	public static final long getLocalIndex() {
//		long localIndex = appender.queue().firstIndex();
//		if (localIndex == Long.MAX_VALUE) {
//			return 0;
//		} else if (appender.wire() != null) {
//			return appender.lastIndexAppended() + 1;
//		} else {
//			return tailer.toEnd().index();
//		}

//		if(totalEventInQueue == Long.MAX_VALUE) {
//			long lastIndex = tailer.toEnd().index();
//			if (lastIndex == 0) {
//				totalEventInQueue =  0;
//			}
//			totalEventInQueue = queue.countExcerpts(getFirstIndex(), lastIndex);
//		}
//		if(lauchingAtIndex > 0) {
//			return lauchingAtIndex;			
//		}
		return lauchedIndex == 0 ? totalEventInQueue : lauchedIndex;
	}

	public static void setPublisher(final IPublishData publisher) {
		dataPublisher = publisher;
	}

	public static final void start(String path, IPublishData publisher, HASwitcher processHA, ExcerptAppender appender,
			ExcerptTailer tailer, long lauchIndex) {

		ReplicationConfig.loadConfigFromDisk(path);
		if (!ReplicationConfig.isStandAlone()) {
			LOG.info("Start replication");
			ReplicationProcessor.appender = appender;
			ReplicationProcessor.tailer = tailer;
			ReplicationProcessor.setPublisher(publisher);
			ReplicationProcessor.processHA = processHA;
			timeWaitSlaveSync = Long.parseLong(ReplicationConfig.getConfigValue("timeWaitSlaveSync", "15000"));
			queue = (SingleChronicleQueue) appender.queue();
			firstQueueIndex = queue.firstIndex();
			totalEventInQueue = queue.entryCount();
			if (lauchIndex > 0 && lauchIndex < totalEventInQueue) {
				lastReplicatedIndex = lauchIndex;
				lauchedIndex = lauchIndex;
			} else {
				lastReplicatedIndex = totalEventInQueue;
				lauchedIndex = 0;
			}

			LOG.info("Start up Replicattion. Total event in queue {}, lauching at index {}", totalEventInQueue,
					lauchedIndex);
			startHA();
		}
	}

	public static final void start(IPublishData publisher, HASwitcher processHA, ExcerptAppender appender,
			ExcerptTailer tailer, long lauchIndex) {
		LOG.info("Start replication");
		ReplicationProcessor.appender = appender;
		ReplicationProcessor.tailer = tailer;
		ReplicationProcessor.setPublisher(publisher);
		ReplicationProcessor.processHA = processHA;
		timeWaitSlaveSync = Long.parseLong(ReplicationConfig.getConfigValue("timeWaitSlaveSync", "15000"));
		queue = (SingleChronicleQueue) appender.queue();
		firstQueueIndex = queue.firstIndex();
		totalEventInQueue = queue.entryCount();
		if (lauchIndex > 0 && lauchIndex < totalEventInQueue) {
			lastReplicatedIndex = lauchIndex;
			lauchedIndex = lauchIndex;
		} else {
			lastReplicatedIndex = totalEventInQueue;
			lauchedIndex = 0;
		}
		LOG.info("Start up Replicattion. Total event in queue {}, lauching at index {}", totalEventInQueue,
				lauchedIndex);
		startHA();
	}

	public static long lastReplicateIndex() {
		return lastReplicatedIndex;
	}

	public static final void stop() {
		stopHA();
	}

	public static final boolean isConfigMaster() {
		return ReplicationConfig.isConfigMaster();
	}

	public static final byte getStatus() {
		return status;
	}

	public static final boolean isMaster() {
		return status == 1;
	}

	public static byte hasEvent() {
		return hasEvent;
	}

	public static boolean isWritealbe() {
		return status == 1 && !peerIsMaster;
	}

	public static boolean canReplicateNow() {
		return connectedToSlave && hasEvent == 1;
//				&& appender.queue().firstIndex() != Long.MAX_VALUE;
	}

	public static final int processToBeMaster(boolean isForce) {
		if (status == 1) {
			LOG.info("Node is currently master");
			return 1;
		}
		LOG.info("Prepare to be master, totalProcessed {}, totalReceived {}", totalMsgProcess, totalMsgReceived);
		while (totalMsgProcess != totalMsgReceived) {
			LOG.info("totalProcessed {}, totalReceived {}", totalMsgProcess, totalMsgReceived);
		}
		int result = processHA.processToBeMaster(isForce);
		if (result != 0) {
			status = 1;
			if (peerIsMaster)
				peerIsMaster = false;
		}
		LOG.info("RESULT SWITCH TO MASTER. NODE IS MASTER: {}", status);
		return result;
	}

	public static final int processToBeSlave() {
		if (status == 0) {
			LOG.info("Node is currently slave");
			return 1;
		}
		LOG.info("Prepare to be slave");
		int result = processHA.processToBeSlave();
		if (result != 0) {
			status = 0;
			peerIsMaster = false;
		}
		LOG.info("RESULT SWITCH TO SLAVE. NODE IS MASTER: {}", status);
		return result;
	}

	public static void startHA() {
		if (ReplicationConfig.isConfigMaster()) {
			try {
				status = 1;
				HaTcpServer.startListening(ReplicationConfig.getConfigValue("connectionString"), true);
			} catch (IOException e) {
				LOG.error("Error when start HA", e);
			}
		} else {
			status = 0;
			HaTcpClient.connect(ReplicationConfig.getConfigValue("connectionString"),
					Integer.parseInt(ReplicationConfig.getConfigValue("retryNum")),
					Integer.parseInt(ReplicationConfig.getConfigValue("heartbeatInterval")),
					Integer.parseInt(ReplicationConfig.getConfigValue("heartbeatTimeout")),
					Integer.parseInt(ReplicationConfig.getConfigValue("timeReconnect")));
		}
	}

	public static void stopHA() {
		if (ReplicationConfig.isConfigMaster()) {
			HaTcpServer.stopListening();
		} else {
			HaTcpClient.stop();
		}
	}

	public static void increaseTotalReceive() {
		totalMsgReceived++;
	}

	public static void recalcTotalEventInQueue() {
		totalEventInQueue = queue.entryCount();
	}

	public static long getFirstIndex() {
		if (firstQueueIndex == Long.MAX_VALUE) {
			firstQueueIndex = queue.firstIndex();
		}
		return firstQueueIndex;
	}

}
