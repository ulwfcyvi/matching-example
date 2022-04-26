package org.ssi.replication.network;

import static net.openhft.chronicle.bytes.Bytes.elasticByteBuffer;
import static net.openhft.chronicle.network.connection.SocketAddressSupplier.uri;

import org.ssi.replication.process.ReplicationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.core.threads.HandlerPriority;
import net.openhft.chronicle.network.connection.FatalFailureConnectionStrategy;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.chronicle.wire.WireType;

public class HaTcpClient{
	
	private static final Logger LOG = LoggerFactory.getLogger(HaTcpClient.class);
	private static final EventGroup eventGroup = new EventGroup(true);
	private static TCPClient client;
	
	private static final WireOut outWire = WireType.RAW.apply(elasticByteBuffer());
	
	public static void connect(String connectionString, int retryNum, int heartbeatInterval, int heartbeatTimeout, int timeReconnect) {
		eventGroup.start();
		System.setProperty("heartbeat.ping.period", Integer.toString(heartbeatInterval));
		System.setProperty("heartbeat.timeout", String.valueOf(Integer.toString(heartbeatTimeout)));
		System.setProperty("connection.reconnect.period", String.valueOf(Integer.toString(timeReconnect)));
		
		client = new TCPClient(eventGroup, WireType.RAW, "/", uri(connectionString),
                new ReplicatedConnectionMonitor(), HandlerPriority.HIGH, new FatalFailureConnectionStrategy(retryNum));
	}
	
	public static void connect(String connectionString, int retryNum, int heartbeatInterval, int heartbeatTimeout, int timeReconnect, ConnectionMonitor connectionMonitor) {
		eventGroup.start();
		System.setProperty("heartbeat.ping.period", Integer.toString(heartbeatInterval));
		System.setProperty("heartbeat.timeout", String.valueOf(Integer.toString(heartbeatTimeout)));
		System.setProperty("connection.reconnect.period", String.valueOf(Integer.toString(timeReconnect)));
		
		client = new TCPClient(eventGroup, WireType.RAW, "/", uri(connectionString),
				connectionMonitor, HandlerPriority.HIGH, new FatalFailureConnectionStrategy(retryNum));
	}
	
	public static void stop() {
		if(client != null) {
			client.close();
		}		
	}
	
	public static void requestTobeMaster() {
		if(ReplicationProcessor.getStatus() == 1) {
			LOG.info("Node currently is master");
			return;
		}
		sendMessage(Messages.MSG_BYTES_REQUEST_TO_MASTER);
	}
	

	public static void requestTobeSlave() {
		if(ReplicationProcessor.processToBeSlave() == 1) {
			sendMessage(Messages.MSG_BYTES_REQUEST_TO_SLAVE);
		}
	}
	
	public static void sendMessage(final WireOut outWire) {
         client.writeSocket(outWire, true);
    }
	
	public static void sendMessage(final WriteBytesMarshallable data) {		
		outWire.writeBytes(data);
        client.writeSocket(outWire, true);
        outWire.clear();
	}
	
//	public static void sendMessage2(WireOut wire) {		
//        client.writeSocket(wire, true);
//	}
	
	public static TCPClient getClient() {
		return client;
	}
	
	public static void main(String args[]) {
		HaTcpClient.connect("127.0.0.1:50000", 3, 1, 12000,5000);
		
		for(;;) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	

}
