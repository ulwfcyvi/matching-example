package org.ssi.replication.network;

import org.jetbrains.annotations.NotNull;
import org.ssi.replication.process.ReplicationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.network.NetworkContext;
import net.openhft.chronicle.network.WireTcpHandler;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.chronicle.wire.WireType;

@SuppressWarnings("rawtypes")
public class ReplicatedServerHandler extends WireTcpHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ReplicatedServerHandler.class);
	public ReplicatedServerHandler() {		
		super();			
		this.wireType(WireType.RAW);
	}
	
	@SuppressWarnings("unchecked")
	public ReplicatedServerHandler(NetworkContext<?> context) {
		super();	
		this.wireType(WireType.RAW);
		nc(context);
	}

	@Override
	protected void onRead(@NotNull DocumentContext in, @NotNull WireOut outWire) {
		Wire inWire = in.wire();
		try {
			HaTcpServer.setSocketChannel(nc().socketChannel());
			if (in.isData()) {
				Bytes<?> bytesIn = inWire.bytes();
				byte msgType = bytesIn.readByte();
				switch (msgType) {
				case MsgType.REPLICATED:
					LOG.debug("Receive REPLICATED");
					ReplicationProcessor.masterProcessReplicateEvent(bytesIn, outWire);
					break;
				case MsgType.SYNC_REPLICATED:
					long index = bytesIn.readLong();
					LOG.debug("Receive SYNC_REPLICATED index {}", index);
					ReplicationProcessor.masterProcessSyncReplicate(index, bytesIn.readByte(), outWire);
					break;
				case MsgType.HEARTBEAT:
					LOG.info("Receive HEARTBEAT. Send reply");
					outWire.writeBytes(Messages.HEARTBEAT_REPLY_BYTES);
//					if (ReplicationProcessor.isConsistency() == 1) {
//						ReplicationProcessor.masterSyncReplicate(outWire);
//					}
					break;
				case MsgType.REQUEST_REPLICATED:
					LOG.info("Receive REQUEST_REPLICATED ");
					index = bytesIn.readLong();
					ReplicationProcessor.masterSendReplicateEventBatch(index, outWire);
					break;
				case MsgType.REQUEST_TO_MASTER:
					LOG.info("Receive REQUEST_TO_MASTER");
					HaTcpServer.requestTobeMaster(outWire);
					break;
				case MsgType.REQUEST_TO_SLAVE:
					LOG.info("Receive REQUEST_TO_SLAVE");
					HaTcpServer.requestTobeSlave(outWire);
					break;

				case MsgType.GRANT_TO_MASTER_REPLY:
					LOG.info("Receive GRANT_TO_MASTER_REPLY");
					byte result = bytesIn.readByte();
					if (result == 1) {
						ReplicationProcessor.processToBeMaster(true);
					}
					break;

				default:
					LOG.debug("Not support this message type: {}", msgType);
				}
				bytesIn.clear();
				inWire.clear();

			} else if (in.isMetaData()) {
				outWire.writeDocument(true,
						meta -> meta.write(() -> "tid").int64(in.wire().read(() -> "tid").int64()));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	protected void onInitialize() {

	}
}