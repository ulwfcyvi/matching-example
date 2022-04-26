package org.ssi.response.consumer;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssi.collection.Long2IntMap;
import org.ssi.collection.Long2ObjectMap;
import org.ssi.collection.ObjectPool;
import org.ssi.io.BufferedChannel;
import org.ssi.io.MetadataManager;
import org.ssi.model.EventType;
import org.ssi.model.OrderStatus;
import org.ssi.model.OrderType;
import org.ssi.response.PublisherService;
import org.ssi.response.model.OrderHistoryModel;
import org.ssi.service.MetadataService;
import org.ssi.util.SerializeHelper;
import org.ssi.util.SystemPropsUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class OrderResponseConsumer implements EventHandler<OrderHistoryModel>{
	private static final Logger LOG = LoggerFactory.getLogger(OrderResponseConsumer.class);
	private static final int MAX_POOL = 1<<18;
	private ObjectPool<OrderHistoryModel> orderPool = new ObjectPool<>(MAX_POOL, OrderHistoryModel::new);
	private Long2ObjectMap<OrderHistoryModel> openOrder = new Long2ObjectMap<>();
	private Long2IntMap userBroker = new Long2IntMap(MAX_POOL);
	private PublisherService publisherService;
	private MetadataService metaService;
	
	public OrderResponseConsumer(PublisherService publisherService, MetadataService metaService) {
		LOG.info("INIT OrdersConsumer");
		orderPool.fillPool(MAX_POOL);
		this.publisherService = publisherService;
		this.metaService = metaService;
//		ResultBalanceDb.loadAllBroker(userBroker);
		// load snapshot if needed
		if (SystemPropsUtil.getLoadSnapshot()) {
			long index = SystemPropsUtil.getEpocIndex();
			if (index == -1) {
				index = metaService.getEpocIndex() - 1;
			}
			if (index < 0) return;
			loadSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.ORDER_REALTIME_SNAPSHOT, index));
			LOG.info("Loading Order Realtime snapshot @epoch {}... done.", index);
		}
	}

	public void onEvent(OrderHistoryModel event, long sequence, boolean endOfBatch) {
//		LOG.info("MSG EVENT {}", event.toString());
		
		if (event.orderId == 0 || event.orderStatus == OrderStatus.REJECT) {
			if (event.filled == 0) {
				
				LOG.info("case filled = 0. reject order. OrderId = 0 {}", event);
				if(event.orderType == EventType.TAKE_SNAPSHOT) {
					takeSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.ORDER_REALTIME_SNAPSHOT));
				}else {
					OrderHistoryModel order = orderPool.allocateOrNew();
					order.setData(event);
					order.created = event.updated;
					order.orderStatus = OrderStatus.REJECT;
					publisherService.writeOrderHistoryModel(order);
					orderPool.returnPool(order);
				}
			} else {
				LOG.info("case change balance, add broker to user. OrderId = 0 {}", event);
				// case change balance, add broker to user
//				int brokerId = (int) event.filled;
				userBroker.put(event.userId, (int) event.filled);
			}
			return;
		}
		if (event.orderStatus == OrderStatus.FILLED || event.orderStatus == OrderStatus.CANCELLED) {
			// list open order remove order
			OrderHistoryModel order = openOrder.get(event.orderId);

			if (order != null) {
				openOrder.remove(event.orderId);
				order.setData(event);
				publisherService.writeOrderHistoryModel(order);
			} else {
				if (event.filled == 0 && (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET)) {
					return;
				}
				order = orderPool.allocateOrNew();
				order.setData(event);
				order.created = event.updated;
				order.brokerId = userBroker.get(event.userId);
				publisherService.writeOrderHistoryModel(order);
			}
		} else {
			OrderHistoryModel order = openOrder.get(event.orderId);

			if (order != null) {
				// if exist on onenorder --> add to list Change.
				order.setData(event);
				publisherService.writeOrderHistoryModel(order);
			} else {
				OrderHistoryModel newOrder = orderPool.allocateOrNew();
				newOrder.setData(event);
				newOrder.created = event.updated;
				newOrder.brokerId = userBroker.get(event.userId);
				openOrder.put(event.orderId, newOrder);
				
				publisherService.writeOrderHistoryModel(newOrder);
			}
		}
//		LOG.info("Time process of order realtime {}",System.nanoTime() - event.clientOrderId);
	}

	public void takeSnapshot(String filePath) {
		RandomAccessFile file = null;
		try {
			int size = 4096;

			ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			int nOrders = openOrder.size();
			channel.putInt(nOrders);
			Object[] values = openOrder.values();
			for (int i = 0; i < values.length; i++) {
				OrderHistoryModel order = (OrderHistoryModel) values[i];
				if (order != null) {
					order.serialize(channel);
				}
			}
			SerializeHelper.serializeLong2IntMap(channel, userBroker);
			
			channel.flush();

		} catch (Exception e) {
			LOG.error("ERROR", e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					LOG.error("ERROR", e);
				}
			}
		}
	}
	
	public void loadSnapshot(String filePath) {
		int size = 4096;
		RandomAccessFile file = null;
		try {

		    ByteBuffer buffer = ByteBuffer.allocate(size);
			file = new RandomAccessFile(filePath, "rw");

			BufferedChannel channel = new BufferedChannel(buffer, file.getChannel());

			int nOrders = channel.getInt();
			for (int i = 0; i < nOrders; i++) {
				OrderHistoryModel order = orderPool.allocateOrNew();
				order.deserialize(channel);
				openOrder.put(order.orderId, order);
			}
			SerializeHelper.deserializeLong2IntMap(channel, userBroker);
			channel.flush();
		} catch (Exception e) {
			LOG.error("ERROR", e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					LOG.error("ERROR", e);
				}
			}
		}
	}
}