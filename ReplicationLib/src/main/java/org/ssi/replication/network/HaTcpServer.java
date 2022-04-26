package org.ssi.replication.network;

import static net.openhft.chronicle.bytes.Bytes.elasticByteBuffer;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

import org.ssi.replication.process.ReplicatingEventThread;
import org.ssi.replication.process.ReplicationProcessor;
import org.ssi.replication.util.ConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.core.threads.EventLoop;
import net.openhft.chronicle.network.AcceptorEventHandler;
import net.openhft.chronicle.network.NetworkContext;
import net.openhft.chronicle.network.TcpEventHandler;
import net.openhft.chronicle.network.VanillaNetworkContext;
import net.openhft.chronicle.network.WireTcpHandler;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.chronicle.wire.WireType;

public class HaTcpServer {
	private static final Logger LOG = LoggerFactory.getLogger(HaTcpServer.class);
	private static final EventLoop eventLoop = new EventGroup(true);

	private static SocketChannel socketChannel;
	private static AcceptorEventHandler acceptorHanlder;
//	private NetworkContext<?> networkContext;
	
	private static final WireOut outWire = WireType.RAW.apply(elasticByteBuffer());

	public static void requestTobeMaster(WireOut outWire) {
		int result = ReplicationProcessor.processToBeSlave();
		if (result == 1) {
			outWire.writeBytes(Messages.DATA_BYTES_REQUEST_TO_MASTER_REPLY_OK);
		} else {
			outWire.writeBytes(Messages.DATA_BYTES_REQUEST_TO_MASTER_REPLY_FAIL);
		}
	}

	public static void requestTobeSlave(WireOut outWire) {
		int result = ReplicationProcessor.processToBeMaster(false);
		if (result == 1) {
			outWire.writeBytes(Messages.DATA_BYTES_REQUEST_TO_SLAVE_REPLY_OK);
		} else {
			outWire.writeBytes(Messages.DATA_BYTES_REQUEST_TO_SLAVE_REPLY_FAIL);
		}

	}

	public static void startListening(String connectionString, boolean runReplicate) throws IOException {
		LOG.info("Start server listen on {}", connectionString);
		eventLoop.start();
		acceptorHanlder = new AcceptorEventHandler(connectionString,
				simpleTcpEventHandlerFactory(WireType.RAW), VanillaNetworkContext::new);
//		acceptorHanlder = new AcceptorEventHandler(connectionString,
//				simpleTcpEventHandlerFactory(WireType.RAW,messageHandler), VanillaNetworkContext::new);

		eventLoop.addHandler(acceptorHanlder);
		if (runReplicate) {			
			ReplicatingEventThread.start();
		}
	}

	public static void startListening(String connectionString, @SuppressWarnings("rawtypes") Class<? extends WireTcpHandler> msgHandlerClass,
			boolean runReplicate) throws IOException {
		LOG.info("Start server listen on {}", connectionString);
		eventLoop.start();
		acceptorHanlder = new AcceptorEventHandler(connectionString,
				simpleTcpEventHandlerFactory(WireType.RAW, msgHandlerClass), VanillaNetworkContext::new);

		eventLoop.addHandler(acceptorHanlder);
		if (runReplicate) {
			ReplicatingEventThread.start();
		}
	}

	public static void stopListening() {
		ReplicatingEventThread.stop();
		eventLoop.stop();
		acceptorHanlder.close();
	}

	public static <T extends NetworkContext<?>> Function<T, TcpEventHandler> simpleTcpEventHandlerFactory(
			final WireType wireType) {
		return (networkContext) -> {
			final TcpEventHandler handler = new TcpEventHandler(networkContext);
			networkContext.wireType(wireType);
			ReplicatedServerHandler messageHandler = new ReplicatedServerHandler(networkContext);
			handler.tcpHandler(messageHandler); // new WireTypeSniffingTcpHandler<>(handler, defaultHandler)

			return handler;
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends NetworkContext<?>> Function<T, TcpEventHandler> simpleTcpEventHandlerFactory(
			final WireType wireType, Class< ? extends WireTcpHandler> serverHandlerClass) {
		return (networkContext) -> {
			final TcpEventHandler handler = new TcpEventHandler(networkContext);
			networkContext.wireType(wireType);
			WireTcpHandler messageHandler;
			try {
				messageHandler = serverHandlerClass.newInstance();
				messageHandler.nc(networkContext);
				handler.tcpHandler(messageHandler);
				return handler;
			} catch (InstantiationException e) {
				LOG.error("Error {}", e);
			} catch (IllegalAccessException e) {
				LOG.error("Error {}", e);
			}		

			return null;
		};
	}

	public static boolean sendMessage(final WriteBytesMarshallable data) {
		outWire.writeBytes(data);
		boolean result = ConnectionUtils.writeSocket(outWire, socketChannel);
		outWire.clear();
		return result;
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		HaTcpServer.startListening("127.0.0.1:50000", true);
		for (;;) {
//			server.sendGrantToBeMaster();
			Thread.sleep(6000);
		}
	}

	public static void setSocketChannel(SocketChannel channel) {
		socketChannel = channel;
	}

	public boolean isConnectedClient() {
		return socketChannel != null && socketChannel.isConnected();
	}
}
