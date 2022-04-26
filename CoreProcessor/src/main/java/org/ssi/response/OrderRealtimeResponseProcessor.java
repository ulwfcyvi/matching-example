package org.ssi.response;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import net.openhft.chronicle.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssi.core.CoreExceptionHandler;
import org.ssi.io.MetadataManager;
import org.ssi.model.*;
import org.ssi.response.consumer.OrderResponseConsumer;
import org.ssi.response.model.OrderHistoryModel;
import org.ssi.service.MetadataService;
import org.ssi.util.BitUtil;
import org.ssi.util.DateUtil;
import org.ssi.util.MathUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
public class OrderRealtimeResponseProcessor{
	private static final Logger LOG = LoggerFactory.getLogger(OrderRealtimeResponseProcessor.class);
//	private static final String TOPIC = "/orders";
	private MetadataService metaService;
//	private QReader reader;
//	private Thread tailerThread;
	private OrderResponseConsumer orderRealtimeConsumer;

	private Disruptor<OrderHistoryModel> disruptorRealtime;
	private RingBuffer<OrderHistoryModel> ringBufferRealtime;
//	private ObjectPool<OrderHistoryModel> orderPool = new ObjectPool<>(200, OrderHistoryModel::new);
//	private Disruptor<OrderHistoryModel> disruptorDb;
//	private RingBuffer<OrderHistoryModel> ringBufferDb;
	private PublisherService orderPublisher;
	
	private boolean isRunning = false;

	private static volatile boolean isEndDate = false;
	private static volatile boolean triggerInsertReport = false;
	private static int dateRunReport = DateUtil.toInt();
	public static void setTriggerReport() {
		int now = DateUtil.toInt();
		if (dateRunReport == now) {
			triggerInsertReport = true;
			isEndDate = false;
		} else {
			dateRunReport = now;
			isEndDate = true;
			triggerInsertReport = false;
		}
	}

	public OrderRealtimeResponseProcessor() {
		LOG.info("INIT OrderResponseProcessor");
		metaService = new MetadataService();
		orderPublisher = new PublisherService(PublisherService.ORDER_OFFER_MODE);
		orderRealtimeConsumer = new OrderResponseConsumer(orderPublisher, metaService);

		initialize();
	}

	private void initialize() {
		initDisruptor();
//		initThreadAffinity();
        isRunning = true;

//        reader = new QReader(metaService.getEpocIndex(), TOPIC);
	}

	public void initDisruptor(){
		WaitStrategy waitStrategyRealtime = new BusySpinWaitStrategy();
//    	WaitStrategy waitStrategyDb = new BlockingWaitStrategy();

		disruptorRealtime = new Disruptor<>(
				OrderHistoryModel::new,
				16 * 1024 * 1024,
				Executors.defaultThreadFactory(),
				ProducerType.SINGLE,
				waitStrategyRealtime);

		disruptorRealtime.setDefaultExceptionHandler(new CoreExceptionHandler<OrderHistoryModel>());

		disruptorRealtime.handleEventsWith(orderRealtimeConsumer);

		ringBufferRealtime = disruptorRealtime.start();
	}

//	public void initThreadAffinity(){
//		tailerThread = new Thread(() -> {
//			AffinityLock lock = null;
//			try {
//
//				lock = Affinity.acquireLock();
//
//				Bytes buffer;
//
//				while (!Thread.currentThread().isInterrupted()) {
//					if ((buffer = reader.readBytes()) != null) {
//						onData(buffer);
//					}
//				}
//			} finally {
//				if (lock != null) {
//					lock.release();
//				}
//			}
//		});
//
//		tailerThread.start();
//	}


	public boolean isRunning() {
		return isRunning;
	}

//	public void start() throws InterruptedException {
//		Bytes buffer;
//
//		while (true) {
//			if ((buffer = reader.readBytes()) != null) {
//				onData(buffer);
//			}
//		}
//	}

//	public void onData(Bytes bytes) {
//		while (bytes.readRemaining() > 0) {
////			long lastRealtime = ringBufferRealtime.next();
//
//			OrderHistoryModel modelRealtime = orderPool.allocateOrNew();
//			eventDeserializer.deserialize(modelRealtime, bytes);
//
////			long lastDb = ringBufferDb.next();
////			OrderHistoryModel modelDb = ringBufferDb.get(lastDb);
////			modelDb.setData(modelRealtime);
////			LOG.info("Time receive from queue {}", System.nanoTime() - modelRealtime.clientOrderId);
//			orderRealtimeConsumer.onEvent(modelRealtime,0,true);
//			orderPool.returnPool(modelRealtime);
////			ringBufferRealtime.publish(lastRealtime);
////			ringBufferDb.publish(lastDb);
//		}
//	}

	public void response(BaseEvent event){
		int base = BitUtil.getHigh(event.symbol);
		long prevId = 0;

		prevId = mapToOrderHistory(event, base, prevId);

		BaseEvent next = event.next;

		while(next != null) {
			prevId = mapToOrderHistory(next, base, prevId);
			next = next.next;
		}

	}


	private long mapToOrderHistory(BaseEvent event, int base, long prevId) {
		long totalFilled = 0;
		double totalFilledCost = 0;

//		StringBuffer log = new StringBuffer();
		if (event.eventType == EventType.PLACE_ORDER) {
			// write order maker
			boolean isMarketCancel = false;
			MatchingResult result = event.matchingResult;
			long filled = 0;

			byte orderType = event.orderType;
			// case  OCO
			if (event.tradeOption == TradeOption.OCO) {
				if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
					orderType = OrderType.OCO_STOP_MARKET;
				} else {
					orderType = OrderType.OCO_LIMIT;
				}
			}

			long price = event.price;
			if (event.orderType == OrderType.MARKET || event.orderType == OrderType.STOP_MARKET || event.orderType == OrderType.OCO_STOP_MARKET) {
				price = 0;
			}

			long matchingPrice = 0;
			if (result != null) {
				while (result != null) {
					if (result.type != MatchingType.CANCEL) {
						byte orderTypeMaker = result.makerOrderType;
						if (result.makerTradeOption == TradeOption.OCO) {
							if (result.makerOrderType == OrderType.MARKET || result.makerOrderType == OrderType.STOP_MARKET || result.makerOrderType == OrderType.OCO_STOP_MARKET) {
								orderTypeMaker = OrderType.OCO_STOP_MARKET;
							} else {
								orderTypeMaker = OrderType.OCO_LIMIT;
							}
						}
						long lastRealtime = ringBufferRealtime.next();
						OrderHistoryModel model = ringBufferRealtime.get(lastRealtime);
						model.orderId = result.makerOrderId;
						model.userId = result.makerId;
						model.symbolId = event.symbol;
						model.updated = event.timestamp;
						model.orderType = orderTypeMaker;
						model.orderSide = event.orderSide == OrderSide.ASK ? OrderSide.BID : OrderSide.ASK;
						model.orderStatus = result.makerFilled == result.makerAmount ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
						model.price = result.price;
						model.filled = result.amount;
						model.totalFilled =result.makerFilled;
						model.amount = result.makerAmount;
						model.averagePrice = result.makerFilled == 0 ? 0 : MathUtils.ceil(result.makerFilledCost / result.makerFilled * 10);
						model.stopPrice = result.makerStopPrice;
						model.clientOrderId = event.clientOrderId;
						model.referId = 0;
						model.matchingPrice = result.price;// matchingPrice
						model.feeSide = FeeSide.MAKER;// feeSide

						ringBufferRealtime.publish(lastRealtime);

						filled = result.amount;
						totalFilledCost += (double) result.amount / 10 * result.price;
						totalFilled += result.amount;
						matchingPrice = result.price;
					} else {
						isMarketCancel = true;
					}

					writeTaker(event, base, prevId, isMarketCancel, price, matchingPrice, filled, totalFilled, totalFilledCost, orderType);

					result = result.next;
				}
			} else {
				writeTaker(event, base, prevId, isMarketCancel, price, matchingPrice, filled, totalFilled, totalFilledCost, orderType);
			}

		}

//		if (log.length() != 0) {
//			LOG.info(log.toString());
//		}
		return event.orderId;
	}

	private void writeTaker(BaseEvent event, int base, long prevId, boolean isMarketCancel, long price, long matchingPrice, long filled, long totalFilled, double totalFilledCost, byte orderType) {
		byte orderStatus = OrderStatus.REJECT;
		if (event.eventResult != EventResult.OK) {
			orderStatus = OrderStatus.REJECT;
			filled = 0;
		} else {
			if (isMarketCancel) {
				orderStatus = OrderStatus.CANCELLED;
				if (totalFilled == 0) {
					filled = 0;
				}
			} else {
				if (totalFilled == 0) {
					orderStatus = OrderStatus.OPEN;
					filled = 0;
				} else if (totalFilled == event.amount) {
					orderStatus = OrderStatus.FILLED;
				} else {
					orderStatus = OrderStatus.PARTIALLY_FILLED;
				}
			}
		}
		long average = totalFilled == 0 ? 0 : MathUtils.ceil((double) totalFilledCost / totalFilled * 10);
		long lastRealtime = ringBufferRealtime.next();
		OrderHistoryModel model = ringBufferRealtime.get(lastRealtime);
		model.orderId = event.orderId;
		model.userId = event.userId;
		model.symbolId = event.symbol;
		model.updated = event.timestamp;
		model.orderType = orderType;
		model.orderSide = event.orderSide;
		model.orderStatus = orderStatus;
		model.price = price;
		model.filled = filled;
		model.totalFilled =totalFilled;
		model.amount = event.amount;
		model.averagePrice = totalFilled == 0 ? 0 : average;
		model.stopPrice = event.stopPrice;
		model.clientOrderId = event.clientOrderId;
		model.referId = prevId;
		model.matchingPrice = matchingPrice;// matchingPrice
		model.feeSide = FeeSide.TAKER;// feeSide

		ringBufferRealtime.publish(lastRealtime);

	}

	public void stop() {
		orderRealtimeConsumer.takeSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.ORDER_REALTIME_SNAPSHOT));
		if (disruptorRealtime != null) {
	    	try {
				disruptorRealtime.shutdown(1, TimeUnit.SECONDS);
				disruptorRealtime.halt();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}

	}




}