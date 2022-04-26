package org.ssi.replication.network;

import static java.lang.Integer.getInteger;
import static java.lang.ThreadLocal.withInitial;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.openhft.chronicle.bytes.Bytes.elasticByteBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ssi.replication.util.TraceLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.ConnectionDroppedException;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.threads.EventHandler;
import net.openhft.chronicle.core.threads.EventLoop;
import net.openhft.chronicle.core.threads.HandlerPriority;
import net.openhft.chronicle.core.threads.InvalidEventHandlerException;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.network.ConnectionStrategy;
import net.openhft.chronicle.network.connection.CoreFields;
import net.openhft.chronicle.network.connection.SocketAddressSupplier;
import net.openhft.chronicle.network.connection.TryLock;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.NamedThreadFactory;
import net.openhft.chronicle.threads.Pauser;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.chronicle.wire.WireType;

public class TCPClient implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(TCPClient.class);

    public static final int TCP_BUFFER = getTcpBufferSize();
    public static final int SAFE_TCP_SIZE = TCP_BUFFER * 3 / 4;
    private static final int HEATBEAT_PING_PERIOD = getInteger("heartbeat.ping.period", 5_000);
    private static final int HEATBEAT_TIMEOUT_PERIOD = getInteger("heartbeat.timeout", 15_000);
    
    private static final int TIME_RECONNECT = getInteger("connection.reconnect.period", 3_000);
    public static final int LENGTH_OF_MSG_SIZE = 4;

	private static final long TIMEOUT_WRITE_SOCKET = TimeUnit.MINUTES.toMillis(15);
    final long timeoutMs;
    @NotNull
    private final String name;
    private final int tcpBufferSize;
    private final Wire outWire;
    @NotNull
    private final SocketAddressSupplier socketAddressSupplier;
//    private final Set<Long> preventSubscribeUponReconnect = new ConcurrentSkipListSet<>();
    private final ReentrantLock outBytesLock = TraceLock.create();
    private final Condition condition = outBytesLock.newCondition();
    @NotNull
    private final AtomicLong transactionID = new AtomicLong(0);    
    @NotNull
    private final TcpSocketConsumer tcpSocketConsumer;
    @NotNull
    private final EventLoop eventLoop;
    @NotNull
    private final WireType wireType;
    @Nullable
    private final ConnectionMonitor clientConnectionMonitor;
    private final ConnectionStrategy connectionStrategy;
    @NotNull
    private Pauser pauser = new LongPauser(100, 100, 500, 20_000, TimeUnit.MICROSECONDS);
    private long largestChunkSoFar = 0;
    @Nullable
    private volatile SocketChannel clientChannel;
    private volatile boolean closed;
    @NotNull
    private CountDownLatch receivedClosedAcknowledgement = new CountDownLatch(1);
    private long limitOfLast = 0;
    private HandlerPriority priority;
    
    
    public TCPClient(@NotNull final EventLoop eventLoop,
                         @NotNull final WireType wireType,
                         @NotNull final String name,
                         @NotNull final SocketAddressSupplier socketAddressSupplier,
                         @Nullable ConnectionMonitor clientConnectionMonitor,
                         @NotNull final HandlerPriority monitor,
                         @NotNull final ConnectionStrategy connectionStrategy) {
        assert !name.trim().isEmpty();
        this.connectionStrategy = connectionStrategy;
        this.priority = monitor;
        this.socketAddressSupplier = socketAddressSupplier;
        this.eventLoop = eventLoop;
        this.tcpBufferSize = Integer.getInteger("tcp.client.buffer.size", TCP_BUFFER);
        this.outWire = wireType.apply(elasticByteBuffer());
        this.name = name.trim();
        this.timeoutMs = Integer.getInteger("tcp.client.timeout", 10_000);
        this.wireType = wireType;

        this.clientConnectionMonitor = clientConnectionMonitor;
//        eventLoop.addHandler(new PauserMonitor(pauser, "async-read", 30));

        this.tcpSocketConsumer = new TcpSocketConsumer();
    }

    private static int getTcpBufferSize() {
        String sizeStr = System.getProperty("TcpEventHandler.tcpBufferSize");
        if (sizeStr != null && !sizeStr.isEmpty())
            try {
                int size = Integer.parseInt(sizeStr);
                if (size >= 64 << 10)
                    return size;
            } catch (Exception e) {
                Jvm.warn().on(TCPClient.class, "Unable to parse tcpBufferSize=" + sizeStr, e);
            }
        try {
            try (ServerSocket ss = new ServerSocket(0)) {
                try (Socket s = new Socket("localhost", ss.getLocalPort())) {
                    s.setReceiveBufferSize(4 << 20);
                    s.setSendBufferSize(4 << 20);
                    int size = Math.min(s.getReceiveBufferSize(), s.getSendBufferSize());
                    (size >= 128 << 10 ? Jvm.debug() : Jvm.warn())
                            .on(TCPClient.class, "tcpBufferSize = " + size / 1024.0 + " KiB");
                    return size;
                }
            }
        } catch (Exception e) {
            throw new IORuntimeException(e); // problem with networking subsystem.
        }
    }
   
   
    private static boolean checkWritesOnReadThread(@NotNull TcpSocketConsumer tcpSocketConsumer) {
        assert Thread.currentThread() != tcpSocketConsumer.readThread : "if writes and reads are on the same thread this can lead to deadlocks with the server, if the server buffer becomes full";
        return true;
    }
    
    void clear(@NotNull final Wire wire) {
        assert wire.startUse();
        try {
            wire.clear();
        } finally {
            assert wire.endUse();
        }
    }    

    public SocketChannel getClientChannel() {
		return clientChannel;
	}

	@Nullable
    SocketChannel openSocketChannel(InetSocketAddress socketAddress) throws IOException {
        final SocketChannel result = SocketChannel.open();
        @Nullable Selector selector = null;
        boolean failed = true;
        try {
            result.configureBlocking(false);
            Socket socket = result.socket();
            socket.setTcpNoDelay(true);
            socket.setReceiveBufferSize(tcpBufferSize);
            socket.setSendBufferSize(tcpBufferSize);
            socket.setSoTimeout(0);
            socket.setSoLinger(false, 0);
            result.connect(socketAddress);

            selector = Selector.open();
            result.register(selector, SelectionKey.OP_CONNECT);

            int select = selector.select(2500);
            if (select == 0) {
                Jvm.warn().on(getClass(), "Timed out attempting to connect to " + socketAddress);
                return null;
            } else {
                try {
                    if (!result.finishConnect())
                        return null;

                } catch (IOException e) {
                    if (Jvm.isDebugEnabled(getClass()))
                        Jvm.debug().on(getClass(), "Failed to connect to " + socketAddress + " " + e);
                    return null;
                }
            }
            failed = false;
            return result;

        } finally {
            Closeable.closeQuietly(selector);
            if (failed)
                Closeable.closeQuietly(result);
        }
    }
  
    private void onDisconnected() {

        if (LOG.isDebugEnabled())
        	LOG.debug("disconnected to remoteAddress=" + socketAddressSupplier);
        tcpSocketConsumer.onConnectionClosed();

        if (clientConnectionMonitor != null) {
            @Nullable final SocketAddress socketAddress = socketAddressSupplier.get();
            if (socketAddress != null)
                clientConnectionMonitor.onDisconnected(clientChannel, socketAddress);
        }
    }

    private void onConnected() {

        if (LOG.isDebugEnabled())
        	LOG.debug("connected to remoteAddress=" + socketAddressSupplier);

        if (clientConnectionMonitor != null) {
            @Nullable final SocketAddress socketAddress = socketAddressSupplier.get();
            if (socketAddress != null)
                clientConnectionMonitor.onConnected(clientChannel, socketAddress);
        }
    }

    @NotNull
    public ReentrantLock outBytesLock() {
        return outBytesLock;
    } 
    
    synchronized void closeSocket() {
    	@Nullable SocketChannel clientChannel = this.clientChannel;
        if (clientChannel != null) {
        	LOG.info("Close connection");
            try {
            	clientChannel.socket().shutdownInput();
            } catch (ClosedChannelException ignored) {

            } catch (IOException e) {
            	if (LOG.isDebugEnabled())
            		LOG.debug(e.getMessage());
            }

            try {
                clientChannel.socket().shutdownOutput();
            } catch (ClosedChannelException ignored) {

            } catch (IOException e) {
            	if (LOG.isDebugEnabled())
            		LOG.debug(e.getMessage());
            }

            Closeable.closeQuietly(clientChannel);

            this.clientChannel = null;            

//            @NotNull final TcpSocketConsumer tcpSocketConsumer = this.tcpSocketConsumer;
//            tcpSocketConsumer.tid = 0;
//            tcpSocketConsumer.omap.clear(); //longtb

            onDisconnected();
        }
    }

    public boolean isOpen() {
        return clientChannel != null;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void notifyClosing() {
        close();
    }

   
    @Override
    public void close() {
        if (closed)
            return;
        closed = true;
        tcpSocketConsumer.prepareToShutdown();
        
        if (LOG.isDebugEnabled())
            Jvm.debug().on(getClass(), "closing connection to " + socketAddressSupplier);
        tcpSocketConsumer.stop();

        while (clientChannel != null) {
            if (LOG.isDebugEnabled()) {
                Jvm.debug().on(getClass(), "waiting for disconnect to " + socketAddressSupplier);
                Jvm.pause(50);
            }
        }

        outWire.bytes().release();
    }
    
    public long nextUniqueTransaction(long timeMs) {
        long id = timeMs;
        for (; ; ) {
            long old = transactionID.get();
            if (old >= id)
                id = old + 1;
            if (transactionID.compareAndSet(old, id))
                break;
        }
        return id;
    }

    
    public void writeSocket(@NotNull final WireOut wire, boolean reconnectOnFailure) {
        assert outBytesLock().isHeldByCurrentThread();

        try {
            assert wire.startUse();
            @Nullable SocketChannel clientChannel = this.clientChannel;

            // wait for the channel to be non null
            if (clientChannel == null) {
                if (!reconnectOnFailure) {
                    return;
                }
                final byte[] bytes = wire.bytes().toByteArray();
                assert wire.endUse();
                condition.await(20, TimeUnit.SECONDS);
                assert wire.startUse();
                wire.clear();
                wire.bytes().write(bytes);
            }

            writeSocket1(wire, this.clientChannel);
        } catch (ClosedChannelException e) {
            closeSocket();
            Jvm.pause(200);
            if (reconnectOnFailure)
                throw new ConnectionDroppedException(e);

        } catch (IOException e) {
            if (!"Broken pipe".equals(e.getMessage()))
                Jvm.warn().on(getClass(), e);
            closeSocket();
            Jvm.pause(200);
            throw new ConnectionDroppedException(e);

        } catch (ConnectionDroppedException e) {
            closeSocket();
            Jvm.pause(200);
            throw e;

        } catch (Exception e) {
            Jvm.warn().on(getClass(), e);
            closeSocket();
            Jvm.pause(200);
            throw new ConnectionDroppedException(e);

        } finally {
            assert wire.endUse();
        }
    }
    
    private void writeSocket1(@NotNull WireOut outWire, @Nullable SocketChannel clientChannel) throws
            IOException {

        if (clientChannel == null) {
            LOG.error("Connection Dropped");
            throw new ConnectionDroppedException("Connection Dropped");
        }

        assert outBytesLock.isHeldByCurrentThread();

        long start = Time.currentTimeMillis();
        assert outWire.startUse();
        try {
            @NotNull final Bytes<?> bytes = outWire.bytes();
            @Nullable final ByteBuffer outBuffer = (ByteBuffer) bytes.underlyingObject();
            outBuffer.limit((int) bytes.writePosition());
            outBuffer.position(0);

            // this check ensure that a put does not occur while currently re-subscribing
            assert outBytesLock().isHeldByCurrentThread();

            boolean isOutBufferFull = false;
//            logToStandardOutMessageSent(outWire, outBuffer);
            updateLargestChunkSoFarSize(outBuffer);

            try {

                int prevRemaining = outBuffer.remaining();
                while (outBuffer.remaining() > 0) {

                    if (clientChannel != this.clientChannel)
                        throw new ConnectionDroppedException("Connection has Changed");

                    int len = clientChannel.write(outBuffer);
                    if (len == -1)
                        throw new IORuntimeException("Disconnection to server=" +
                                socketAddressSupplier + ", name=" + name);

//                    if (LOG.isDebugEnabled())
//                        Jvm.debug().on(getClass(), "W:" + len + ",socket=" + socketAddressSupplier.get());

                    // reset the timer if we wrote something.
                    if (prevRemaining != outBuffer.remaining()) {
                        
                        isOutBufferFull = false;                     
                        prevRemaining = outBuffer.remaining();
//                        start = Time.currentTimeMillis();
//                        @NotNull final TcpSocketConsumer tcpSocketConsumer = this.tcpSocketConsumer;
//                        if (tcpSocketConsumer != null) {
//                            this.tcpSocketConsumer.lastTimeMessageReceivedOrSent = start;
//                        }
                    } else {
                        if (!isOutBufferFull && Jvm.isDebug() && LOG.isDebugEnabled())
                            Jvm.debug().on(getClass(), "----> TCP write buffer is FULL! " + outBuffer.remaining() + " bytes" +
                                    " remaining.");
                        isOutBufferFull = true;

                        long writeTime = Time.currentTimeMillis() - start;

                        // the reason that this is so large is that results from a bootstrap can
                        // take a very long time to send all the data from the server to the client
                        // we don't want this to fail as it will cause a disconnection !
                        if (writeTime > TIMEOUT_WRITE_SOCKET) { 
                            closeSocket();

                            throw new IORuntimeException("Took " + writeTime + " ms " +
                                    "to perform a write, remaining= " + outBuffer.remaining());
                        }

                        // its important to yield, if the read buffer gets full
                        // we wont be able to write, lets give some time to the read thread !
                        Thread.yield();
                    }
                }
            } catch (IOException e) {
                closeSocket();
                throw e;
            }

            outBuffer.clear();
            bytes.clear();
        } finally {
            assert outWire.endUse();
        }
    }
    
   
    private void updateLargestChunkSoFarSize(@NotNull ByteBuffer outBuffer) {
        int sizeOfThisChunk = (int) (outBuffer.limit() - limitOfLast);
        if (largestChunkSoFar < sizeOfThisChunk)
            largestChunkSoFar = sizeOfThisChunk;

        limitOfLast = outBuffer.limit();
    }

    public Wire outWire() {
        assert outBytesLock().isHeldByCurrentThread();
        return outWire;
    }

    public boolean isOutBytesLocked() {
        return outBytesLock.isLocked();
    }

//    private void reflectServerHeartbeatMessage(@NotNull ValueIn valueIn) {
//
//        if (!outBytesLock().tryLock()) {
//            if (Jvm.isDebug() && LOG.isDebugEnabled())
//                Jvm.debug().on(getClass(), "skipped sending back heartbeat, because lock is held !" +
//                        outBytesLock);
//            return;
//        }
//
//        try {
//
//            // time stamp sent from the server, this is so that the server can calculate the round
//            // trip time
//            long timestamp = valueIn.int64();
//            System.out.println("reflectServerHeartbeatMessage Send to server   writeMetaDataForKnownTID");
//            TCPClient.this.writeMetaDataForKnownTID(0, outWire, null, 0);
//            System.out.println("reflectServerHeartbeatMessage Send to server   heartbeatReply and timestamp");
//            TCPClient.this.outWire.writeDocument(false, w ->
//                    // send back the time stamp that was sent from the server            		
//                    w.writeEventName(EventId.heartbeatReply).int64(timestamp));
//            writeSocket(outWire(), false);
//
//        } finally {
//            outBytesLock().unlock();
//            assert !outBytesLock.isHeldByCurrentThread();
//        }
//    }

    public long writeMetaDataStartTime(long startTime, @NotNull Wire wire, String csp, long cid) {
        assert outBytesLock().isHeldByCurrentThread();
        long tid = nextUniqueTransaction(startTime);
        writeMetaDataForKnownTID(tid, wire, csp, cid);
        return tid;
    }

    public void writeMetaDataForKnownTID(long tid, @NotNull Wire wire, @Nullable String csp,
                                         long cid) {
        assert outBytesLock().isHeldByCurrentThread();
        wire.writeDocument(true, wireOut -> {
            if (cid == 0)
                wireOut.writeEventName(CoreFields.csp).text(csp);
            else
                wireOut.writeEventName(CoreFields.cid).int64(cid);
            wireOut.writeEventName(CoreFields.tid).int64(tid);
        });
    }

   
    public void writeAsyncHeader(@NotNull Wire wire, String csp, long cid) {
        assert outBytesLock().isHeldByCurrentThread();

        wire.writeDocument(true, wireOut -> {
            if (cid == 0)
                wireOut.writeEventName(CoreFields.csp).text(csp);
            else
                wireOut.writeEventName(CoreFields.cid).int64(cid);
        });
    }

    public boolean lock(@NotNull Task r) {
        return lock(r, TryLock.LOCK);
    }

    private boolean lock(@NotNull Task r, @NotNull TryLock tryLock) {
        return lock2(r, false, tryLock);
    }

    public boolean lock2(@NotNull Task r, boolean reconnectOnFailure, @NotNull TryLock tryLock) {
        assert !outBytesLock.isHeldByCurrentThread();
        try {
            if (clientChannel == null && !reconnectOnFailure)
                return TryLock.LOCK != tryLock;

            @NotNull final ReentrantLock lock = outBytesLock();
            if (TryLock.LOCK == tryLock) {
                try {
                    //   if (lock.isLocked())
                    //     LOG.info("Lock for thread=" + Thread.currentThread() + " was held by " +
                    // lock);
                    lock.lock();

                } catch (Throwable e) {
                    lock.unlock();
                    throw e;
                }
            } else {
                if (!lock.tryLock()) {
                    if (tryLock.equals(TryLock.TRY_LOCK_WARN))
                        if (Jvm.isDebugEnabled(getClass()))
                            Jvm.debug().on(getClass(), "FAILED TO OBTAIN LOCK thread=" + Thread.currentThread() + " on " +
                                    lock, new IllegalStateException());
                    return false;
                }
            }

            try {
                if (clientChannel == null && reconnectOnFailure)
                    checkConnection();

                r.run();

                assert checkWritesOnReadThread(tcpSocketConsumer);

                writeSocket(outWire(), reconnectOnFailure);

            } catch (ConnectionDroppedException e) {
                if (Jvm.isDebug())
                    Jvm.debug().on(getClass(), e);
                throw e;

            } catch (Exception e) {
                Jvm.warn().on(getClass(), e);
                throw e;

            } finally {
                lock.unlock();
            }
            return true;
        } finally {
            assert !outBytesLock.isHeldByCurrentThread();
        }
    }

    /**
     * blocks until there is a connection
     */
    public void checkConnection() {
        long start = Time.currentTimeMillis();

        while (clientChannel == null) {

            tcpSocketConsumer.checkNotShutdown();

            if (start + timeoutMs > Time.currentTimeMillis())
                try {
                    condition.await(1, TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) {
                    throw new IORuntimeException("Interrupted");
                }
            else
                throw new IORuntimeException("Not connected to " + socketAddressSupplier);
        }

        if (clientChannel == null)
            throw new IORuntimeException("Not connected to " + socketAddressSupplier);
    }

    /**
     * The purpose of this method is only to simulate a network outage
     */
    public void forceDisconnect() {
        Closeable.closeQuietly(clientChannel);
    }

    public boolean isOutBytesEmpty() {
        return outWire.bytes().readRemaining() == 0;
    }

    @FunctionalInterface
    public interface Task {
        void run();
    }
       
    @NotNull
    @Override
    public String toString() {
        return "TCPClient{" +
                "name=" + name +
                "remoteAddressSupplier=" + socketAddressSupplier + '}';
    }

    
    
    
    class TcpSocketConsumer implements EventHandler {
//        @NotNull
//        private final Map<Long, Object> map = new ConcurrentHashMap<>();
//        private final Map<Long, Object> omap = new ConcurrentHashMap<>();
        @NotNull
        private final ExecutorService service;
        @NotNull
        private final ThreadLocal<Wire> syncInWireThreadLocal = withInitial(() -> {
            Wire wire = wireType.apply(elasticByteBuffer());
            assert wire.startUse();
            return wire;
        });
        long lastheartbeatSentTime = 0;
//        private long tid;
//        private Bytes<ByteBuffer> serverHeartBeatHandler = Bytes.elasticByteBuffer();
        private volatile long lastTimeMessageReceivedOrSent = Time.currentTimeMillis();
        private volatile boolean isShutdown;
        @Nullable
        private volatile Throwable shutdownHere = null;
        private volatile boolean prepareToShutdown;
        private Thread readThread;

        TcpSocketConsumer() {            
            service = newCachedThreadPool(
                    new NamedThreadFactory("TCPClient-Reads-" + socketAddressSupplier, true));
            start();
        }

        /**
         * uses a single read thread, to process messages to waiting threads based on their {@code
         * tid}
         */
        @NotNull
        private void start() {
            checkNotShutdown();

            assert shutdownHere == null;
            assert !isShutdown;
            service.submit(() -> {
                readThread = Thread.currentThread();
                try {
                    running();

                } catch (ConnectionDroppedException e) {
                    if (Jvm.isDebug() && !prepareToShutdown)
                        Jvm.debug().on(getClass(), e);

                } catch (Throwable e) {
                    if (!prepareToShutdown)
                        LOG.error("Error when start readThread", e);
                }
            });

        }


        private void running() {
//            try {  
        		final Bytes<?> bytes = Bytes.elasticByteBuffer();
//                final Wire inWire = wireType.apply(elasticByteBuffer());
//                assert inWire != null;
//                assert inWire.startUse();

                while (!isShuttingdown()) {

                    checkConnectionState();

                    try {
//                        @NotNull final Bytes<?> bytes = inWire.bytes();
//                        read(inWire, LENGTH_OF_MSG_SIZE);
                    	
                    	read(bytes, LENGTH_OF_MSG_SIZE);
                        final int header = bytes.readVolatileInt(0);
                        final int messageSize = size2(header);
//                        read(inWire, messageSize);
                        read(bytes, messageSize);
                        clientConnectionMonitor.onReceiveMessage(bytes, clientChannel);
                    } catch (Throwable e) {                    	
                        if (isShuttingdown()) {
                            break;
                        } else {
                        	LOG.error("Reconnecting due to exception: ", e);
                            closeSocket();

                            long pauseMs = connectionStrategy == null ? 300 : connectionStrategy.pauseMillisBeforeReconnect();
                            Jvm.pause(pauseMs);

                        }
                    } finally {
//                        clear(inWire);
                        bytes.clear();
                    }
                }

//            } catch (Throwable e) {
//                if (!isShuttingdown())
//                	LOG.error("Got Throwable", e);
//	               
//            } finally {
//                closeSocket();                
//            }
        }

   
//        private boolean processData(final long tid,
//                                    final boolean isReady,
//                                    final int header,
//                                    final int messageSize,
//                                    @NotNull Wire inWire) throws IOException {
//            assert tid != -1;
//            boolean isLastMessageForThisTid = false;
//            long startTime = 0;
//            @Nullable Object o = null;
//        	LOG.info("Receive msg tid = " + String.valueOf(tid));
//
//            // tid == 0 for system messages
//            if (tid != 0) {
//
//                @Nullable final SocketChannel c = clientChannel;
//
//                // this can occur if we received a shutdown
//                if (c == null)
//                    return false;
//
//                // this loop if to handle the rare case where we receive the tid before its been registered by this class
//                for (; !isShuttingdown() && c.isOpen(); ) {
//
//                    o = map.get(tid);
//
//                    // we only remove the subscription so they are AsyncSubscription, as the AsyncSubscription
//                    // can not be remove from the map as they are required when you resubscribe when we loose connectivity
//                    if (o == null) {
//                        o = omap.get(tid);
//                        if (o != null) {
//                            blockingRead(inWire, messageSize);//
////                            logToStandardOutMessageReceivedInERROR(inWire);
//                            throw new AssertionError("Found tid=" + tid + " in the old map.");
//                        }
//                    } else {
//                        if (isReady && (o instanceof Bytes || o instanceof
//                                AsyncSubscription)) {
//                            omap.put(tid, map.remove(tid));
//                            isLastMessageForThisTid = true;
//                        }
//                        break;
//                    }
//
//                    // this can occur if the server returns the response before we have started to
//                    // listen to it
//
//                    if (startTime == 0)
//                        startTime = Time.currentTimeMillis();
//                    else
//                        Jvm.pause(1);
//
//                    if (Time.currentTimeMillis() - startTime > 3_000) {
//
//                        blockingRead(inWire, messageSize);
////                        logToStandardOutMessageReceived(inWire);
//
//                        if (Jvm.isDebugEnabled(getClass()))
//                            Jvm.debug().on(getClass(), "unable to respond to tid=" + tid + ", given that we have " +
//                                    "received a message we a tid which is unknown, this can occur sometime if " +
//                                    "the subscription has just become unregistered ( an the server has not yet processed the unregister event ) ");
//                        return isLastMessageForThisTid;
//                    }
//                }
//
//                // this can occur if we received a shutdown
//                if (o == null)
//                    return isLastMessageForThisTid;
//
//            }
//
//            // heartbeat message sent from the server
//            if (tid == 0) {
////                processServerSystemMessage(header, messageSize);
//                return isLastMessageForThisTid;
//            }
//
//            // for async
//            if (o instanceof AsyncSubscription) {
//            	LOG.debug("o instanceof AsyncSubscription");
//                blockingRead(inWire, messageSize);
////                logToStandardOutMessageReceived(inWire);
//                @NotNull AsyncSubscription asyncSubscription = (AsyncSubscription) o;
//
//                try {
//                    asyncSubscription.onConsumer(inWire);
//
//                } catch (Exception e) {
//                    if (LOG.isDebugEnabled())
//                        Jvm.debug().on(getClass(), "Removing " + tid + " " + o, e);
//                    omap.remove(tid);
//                }
//            }
//
//            // for sync
//            if (o instanceof Bytes) {
//                final Bytes bytes = (Bytes) o;
//                // for sync
//                //noinspection SynchronizationOnLocalVariableOrMethodParameter
//                synchronized (bytes) {
//                    bytes.clear();
//                    bytes.ensureCapacity(SIZE_OF_SIZE + messageSize);
//                    @Nullable final ByteBuffer byteBuffer = (ByteBuffer) bytes.underlyingObject();
//                    byteBuffer.clear();
//                    // we have to first write the header back to the bytes so that is can be
//                    // viewed as a document
//                    bytes.writeInt(0, header);
//                    byteBuffer.position(SIZE_OF_SIZE);
//                    byteBuffer.limit(SIZE_OF_SIZE + messageSize);
//                    readBuffer(byteBuffer);
//                    bytes.readLimit(byteBuffer.position());
//                    bytes.notifyAll();
//                }
//            }
//            return isLastMessageForThisTid;
//        }

 
//        private void read(@NotNull final WireIn wire, final long numberOfBytes)
//                throws IOException {
//
//            @NotNull final Bytes<?> bytes = wire.bytes();
//            bytes.ensureCapacity(bytes.writePosition() + numberOfBytes);
//
//            @NotNull final ByteBuffer buffer = (ByteBuffer) bytes.underlyingObject();
//            final int start = (int) bytes.writePosition();
//            //noinspection ConstantConditions
//            buffer.position(start);
//
//            buffer.limit((int) (start + numberOfBytes));
//            readBuffer(buffer);
//            bytes.readLimit(buffer.position());
//        }
        
        private void read(@NotNull final Bytes<?> bytes, final long numberOfBytes)
                throws IOException {
            
            bytes.ensureCapacity(bytes.writePosition() + numberOfBytes);

            @NotNull final ByteBuffer buffer = (ByteBuffer) bytes.underlyingObject();
            final int start = (int) bytes.writePosition();
            //noinspection ConstantConditions
            buffer.position(start);

            buffer.limit((int) (start + numberOfBytes));
            readBuffer(buffer);
            bytes.readLimit(buffer.position());
        }       
        
        private void readBuffer(@NotNull final ByteBuffer buffer) throws IOException {

            //  long start = System.currentTimeMillis();
//            boolean emptyRead = true;
            while (buffer.remaining() > 0) {
                @Nullable final SocketChannel clientChannel = TCPClient.this.clientChannel;
                if (clientChannel == null)
                    throw new IOException("Disconnection to server=" + socketAddressSupplier +
                            " channel is closed, name=" + name);
                int numberOfBytesRead = clientChannel.read(buffer);

                if (numberOfBytesRead > 0) {
//                    updateTimeMessageReceived();
                	lastTimeMessageReceivedOrSent = Time.currentTimeMillis();
                    pauser.reset();

                } else if (numberOfBytesRead == 0 && isOpen()) {
                    // if we have not received a message from the server after the HEATBEAT_TIMEOUT_PERIOD
                    // we will drop and then re-establish the connection.
                    long millisecondsSinceLastMessageReceived = System.currentTimeMillis() - lastTimeMessageReceivedOrSent;
//                    LOG.debug("millisecondsSinceLastMessageReceived " + String.valueOf(millisecondsSinceLastMessageReceived));
                    
                    if (millisecondsSinceLastMessageReceived - HEATBEAT_TIMEOUT_PERIOD > 0) {
                    	clientConnectionMonitor.onHeartBeatTimeout(null);                    	
                        throw new ConnectionDroppedException("reconnecting due to heartbeat timeout, time since " +
                                "last message=" + millisecondsSinceLastMessageReceived + "ms " +
                                "dropping connection " + socketAddressSupplier);
                    }
                    
                    pauser.pause();                    

                } else if (numberOfBytesRead == -1){
                	throw new ConnectionDroppedException("Disconnection to server=" + socketAddressSupplier +
                            " read=-1 "
                            + ", name=" + name);
                }else {
                    throw new ConnectionDroppedException(name + " is shutdown, was connected to "
                            + socketAddressSupplier);
                }

                if (isShutdown)
                    throw new ConnectionDroppedException(name + " is shutdown, was connected to " +
                            "" + socketAddressSupplier);                
            }
        }

//        private void updateTimeMessageReceived() {        	
//            lastTimeMessageReceivedOrSent = Time.currentTimeMillis();
//        }
        

        private void checkConnectionState() {
            if (clientChannel != null)
                return;

            attemptConnect();
        }

        private void attemptConnect() {
        	LOG.info("Attempt to connect to server");
//            keepSubscriptionsAndClearEverythingElse(); longtb
            socketAddressSupplier.resetToPrimary();

            for (int i = 0; ; i++) {
                checkNotShutdown();

                if (LOG.isDebugEnabled())
                	LOG.debug("attemptConnect remoteAddress=" + socketAddressSupplier);
                else if (i >= socketAddressSupplier.all().size() && !isShuttingdown())
                    LOG.info("attemptConnect remoteAddress=" + socketAddressSupplier);

                @Nullable SocketChannel socketChannel = null;
                try {
                    if (isShuttingdown())
                        continue;

                    socketChannel = connectionStrategy.connect(name, socketAddressSupplier, false, clientConnectionMonitor);

                    if (isShuttingdown())
                        continue;

                    if (socketChannel == null) {
                        Jvm.pause(TIME_RECONNECT);
                        continue;
                    }

                   
                    if (!outBytesLock().tryLock(20, TimeUnit.SECONDS))
                        throw new IORuntimeException("failed to obtain the outBytesLock " + outBytesLock);

                    try {
                        clear(outWire);
                        // resets the heartbeat timer
//                        updateTimeMessageReceived();
                        lastTimeMessageReceivedOrSent = Time.currentTimeMillis();
                        synchronized (this) {
                            LOG.info("<===> Connected to {}", socketChannel);
                            clientChannel = socketChannel;
                        }

                        // the hand-shaking is assigned before setting the clientChannel
//                        doHandShaking(socketChannel);

                        eventLoop.addHandler(this);
//                        onReconnect();
                        condition.signalAll();
                        onConnected();
                    } finally {
                        outBytesLock().unlock();
                        assert !outBytesLock.isHeldByCurrentThread();
                    }

                    return;
                } catch (Exception e) {
                    if (isShutdown || prepareToShutdown) {
                        closeSocket();
                        throw new IORuntimeException("shutting down");
                    } else {
                    	LOG.error("failed to connect remoteAddress=" + socketAddressSupplier
                                + " so will reconnect ", e);
                        closeSocket();
                    }

                    Jvm.pause(1_000);
                }
            }
        }

  
//        private void onReconnect() {

//            preventSubscribeUponReconnect.forEach(this::unsubscribe);
//            map.values().forEach(v -> {
//                if (v instanceof AsyncSubscription) {
//                    if (!(v instanceof AsyncSubscription))
//                        ((AsyncSubscription) v).applySubscribe();
//                }
//            });

//        }

        void onConnectionClosed() {
//            map.values().forEach(v -> {
//                if (v instanceof Bytes)
//                    synchronized (v) {
//                        v.notifyAll();
//                    }
//                if (v instanceof AsyncSubscription) {
//                    ((AsyncSubscription) v).onClose();
//                } else if (v instanceof Bytes) {
//                    synchronized (v) {
//                        v.notifyAll();
//                    }
//                }
//            });
        }

 
        @Override
        public boolean action() throws InvalidEventHandlerException {

            if (clientChannel == null)
                throw new InvalidEventHandlerException();

            // a heartbeat only gets sent out if we have not received any data in the last
            // HEATBEAT_PING_PERIOD milliseconds
            long currentTime = Time.currentTimeMillis();
            long millisecondsSinceLastMessageReceived = currentTime - lastTimeMessageReceivedOrSent;
            long millisecondsSinceLastHeatbeatSend = currentTime - lastheartbeatSentTime;
            if (millisecondsSinceLastMessageReceived >= HEATBEAT_PING_PERIOD &&
                    millisecondsSinceLastHeatbeatSend >= HEATBEAT_PING_PERIOD) {
                lastheartbeatSentTime = Time.currentTimeMillis();
                sendHeartbeat();
            }

            return true;
        }
        
        /**
         * sends a heartbeat from the client to the server and logs the round trip time
         */
        private void sendHeartbeat() {
            TCPClient.this.lock(this::sendHeartbeat0, TryLock.TRY_LOCK_IGNORE);
        }
        
        private void sendHeartbeat0() {
            assert outWire.startUse();
            try {
                if (outWire.bytes().writePosition() > 100)
                    return;
                
//                subscribe(new AsyncSubscription(TCPClient.this, name) {
//                    @Override
//                    public void onSubscribe(@NotNull WireOut wireOut) {
//                        LOG.info("Sending heartbeat");
//                        wireOut.write(()->"type").int16(MsgType.HEARTBEAT);
//                        wireOut.write(()->"time").int64(Time.currentTimeMillis());
//                    }
//
//                }, true);
//                outWire.writeDocument(false, Messages.HEARTBEAT
                outWire.writeBytes(Messages.HEARTBEAT_BYTES
//                		wireOut -> {
//               	 wireOut.write(()->"type").int16(MsgType.HEARTBEAT);
//                  wireOut.write(()->"time").int64(Time.currentTimeMillis());
//               }
            );
               
               writeSocket1(outWire, clientChannel);
            } catch (IOException e) {
            	LOG.error(e.getMessage());            	
			} finally {
                assert outWire.endUse();
            }
        }
                
        
 
        void stop() {

            if (isShutdown)
                return;

            if (shutdownHere == null)
                shutdownHere = new StackTrace(Thread.currentThread() + " Shutdown here");

            isShutdown = true;
            service.shutdown();
            try {
                service.awaitTermination(100, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            } finally {
                service.shutdownNow();
            }
        }

        @NotNull
        @Override
        public HandlerPriority priority() {
            return priority;
        }


//        private void registerSubscribe(long tid, Object bytes) {
//            // this check ensure that a put does not occur while currently re-subscribing
//            outBytesLock().isHeldByCurrentThread();
//            //    if (bytes instanceof AbstractAsyncSubscription && !(bytes instanceof
//            //          AsyncSubscription))
//
//            final Object prev = map.put(tid, bytes);
//            assert prev == null;
//        }

//        void subscribe(@NotNull final AsyncSubscription asyncSubscription, boolean tryLock) {
//            // we add a synchronize to ensure that the asyncSubscription is added before map before
//            // the clientChannel is assigned
//            synchronized (this) {
//                if (clientChannel == null) {
//
//                    // this check ensure that a put does not occur while currently re-subscribing
//                    outBytesLock().isHeldByCurrentThread();
//
//                    registerSubscribe(asyncSubscription.tid(), asyncSubscription);
//                    if (LOG.isDebugEnabled())
//                        Jvm.debug().on(getClass(), "deferred subscription tid=" + asyncSubscription.tid() + "," +
//                                "asyncSubscription=" + asyncSubscription);
//
//                    // not currently connected
//                    return;
//                }
//            }
//
//            // we have lock here to prevent a race with the resubscribe upon a reconnection
//            @NotNull final ReentrantLock lock = outBytesLock();
//            if (tryLock) {
//                if (!lock.tryLock())
//                    return;
//            } else {
//                try {
//                    // do a quick lock so you can see if it could not get the lock the first time.
//                    if (!lock.tryLock()) {
//                        while (!lock.tryLock(1, SECONDS)) {
//                            if (isShuttingdown())
//                                throw new IllegalStateException("Shutting down");
//                            LOG.info("Waiting for lock " + Jvm.lockWithStack(lock));
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    throw new IllegalStateException(e);
//                }
//            }
//            try {
//                registerSubscribe(asyncSubscription.tid(), asyncSubscription);
//
//                asyncSubscription.applySubscribe();
//
//            } catch (Exception e) {
//                Jvm.warn().on(getClass(), e);
//
//            } finally {
//                lock.unlock();
//            }
//        }

//        public void unsubscribe(long tid) {
//            map.remove(tid);
//        }


        public void checkNotShutdown() {
            if (isShutdown)
                throw new IORuntimeException("Called after shutdown", shutdownHere);
        }

        boolean isShutdown() {
            return isShutdown;
        }

        boolean isShuttingdown() {
            return isShutdown || prepareToShutdown;
        }

//        private long size(int header) {
//            final long messageSize = Wires.lengthOf(header);
//            assert messageSize > 0 : "Invalid message size " + messageSize;
//            assert messageSize < 1 << 30 : "Invalid message size " + messageSize;
//            return messageSize;
//        }
        
        private int size2(int header) {
            assert header > 0 : "Invalid message size " + header;
            assert header < 1 << 30 : "Invalid message size " + header;
            return header;
        }


//        private void keepSubscriptionsAndClearEverythingElse() {

////            tid = 0;
//            omap.clear();
//
//            @NotNull final Set<Long> keys = new HashSet<>(map.keySet());
//
//            keys.forEach(k -> {
//                final Object o = map.get(k);
//                if (o instanceof Bytes || o instanceof AsyncSubscription)
//                    map.remove(k);
//            });
//        }

        void prepareToShutdown() {
            this.prepareToShutdown = true;
            try {
                service.awaitTermination(100, MILLISECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            service.shutdown();
        }
    }
}
