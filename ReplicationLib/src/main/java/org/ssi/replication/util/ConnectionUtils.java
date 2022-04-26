package org.ssi.replication.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.WireOut;

public class ConnectionUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtils.class);

	public static boolean writeSocket(WireOut outWire, final SocketChannel clientChannel) {
		if (clientChannel.isOpen() && clientChannel.isConnected()) {
			final Bytes<?> bytes = outWire.bytes();
			final ByteBuffer outBuffer = (ByteBuffer) bytes.underlyingObject();
			outBuffer.limit((int) bytes.writePosition());
			outBuffer.position(0);
			boolean isOutBufferFull = false;
			try {

				int prevRemaining = outBuffer.remaining();
				while (outBuffer.remaining() > 0) {

					int len = clientChannel.write(outBuffer);
					if (len == -1)
						throw new IORuntimeException("Disconnection to client");

					if (prevRemaining != outBuffer.remaining()) {
						isOutBufferFull = false;
						prevRemaining = outBuffer.remaining();

					} else {
						if (!isOutBufferFull && Jvm.isDebug() && LOG.isDebugEnabled())
							LOG.error("----> TCP write buffer is FULL! " + outBuffer.remaining() + " bytes remaining.");
						isOutBufferFull = true;
//							long writeTime = Time.currentTimeMillis() - start;

						// take a very long time to send all the data from the server to the client
						// we don't want this to fail as it will cause a disconnection !
//							if (writeTime > TimeUnit.MINUTES.toMillis(15)) {
//								throw new IORuntimeException("Took " + writeTime + " ms "
//										+ "to perform a write, remaining= " + outBuffer.remaining());
//							}
					}
				}
				return true;
			} catch (IOException e) {
//				closeSocket();
				LOG.error("Error send msg ", e);
				return false;
			} finally {
				outBuffer.clear();
				bytes.clear();
			}

		} else {
			LOG.info("Connection is closed");
			return false;
		}
	}

	public static boolean writeSocket(ByteBuffer outBuffer, final SocketChannel clientChannel) {
		if (clientChannel.isOpen() && clientChannel.isConnected()) {
			boolean isOutBufferFull = false;
			try {

				int prevRemaining = outBuffer.remaining();
				while (outBuffer.remaining() > 0) {

					int len = clientChannel.write(outBuffer);
					if (len == -1)
						throw new IORuntimeException("Disconnection to client");

					if (prevRemaining != outBuffer.remaining()) {
						isOutBufferFull = false;
						prevRemaining = outBuffer.remaining();

					} else {
						if (!isOutBufferFull && Jvm.isDebug() && LOG.isDebugEnabled())
							LOG.error("----> TCP write buffer is FULL! " + outBuffer.remaining() + " bytes remaining.");
						isOutBufferFull = true;
					}
				}

				return true;
			} catch (IOException e) {
				LOG.error("Error send msg ", e);
				return false;
			} finally {
				outBuffer.clear();
			}
		} else {
			LOG.info("Connection is closed");
			return false;
		}
	}

}
