package org.ssi.consumer;

import javax.annotation.PostConstruct;

import org.ssi.io.MetadataManager;
import org.ssi.io.OrdersDataWriter;
import org.ssi.model.BaseEvent;
import org.ssi.response.OrderRealtimeResponseProcessor;
import org.ssi.service.MetadataService;
import org.ssi.util.BitUtil;
import org.ssi.util.SystemPropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.EventHandler;

@Service
public class OrdersConsumer implements EventHandler<BaseEvent>{
	private static final Logger LOG = LoggerFactory.getLogger(OrdersConsumer.class);
	
	static final long[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
	private OrdersDataWriter writer;
	public static long totalEvent;
	public  OrderRealtimeResponseProcessor responseProcessor;
	@Autowired
	private MetadataService metaService;
	
	@PostConstruct
	public void initialize() {
		writer = new OrdersDataWriter();
		writer.initQueue(metaService.getEpocIndex(), "/orders");
		
		// load snapshot if needed
		if (SystemPropsUtil.getLoadSnapshot()) {
			long index = SystemPropsUtil.getEpocIndex();
			if (index == -1) {
				index = metaService.getEpocIndex() - 1;
			}
			if (index < 0) return;
			writer.loadSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BASE_DECIMALS_SNAPSHOT, index));
			LOG.info("Loading base decimals snapshot @epoch {}... done.", index);
		}
		responseProcessor = new OrderRealtimeResponseProcessor();
	}
	@Override
	public void onEvent(BaseEvent event, long sequence, boolean endOfBatch) throws Exception {
//		if (event.eventType == EventType.ADD_SYMBOL) {
//			addSymbol(event, sequence, endOfBatch);
//		}
		responseProcessor.response(event);
		writer.write(event, sequence, endOfBatch);
//		if(event.clientOrderId>0 && event.orderId > 0){
//			totalEvent++;
//			LOG.info("Time process {}, totalEvent {}", (System.nanoTime() - event.clientOrderId)/1000, totalEvent);
//		}
//		LOG.info("Process of order realtime {}, endOfBatch {} ", System.nanoTime() - event.clientOrderId,endOfBatch);
	}

	private void addSymbol(BaseEvent event, long sequence, boolean endOfBatch) {
		int base = BitUtil.getHigh(event.symbol);
		int counter = BitUtil.getLow(event.symbol);
		byte baseDecimal = event.orderSide;
		byte counterDecimal = event.orderType;
//		byte priceDecimal = event.tradeType;
		writer.baseDecimal[base] = POWERS_OF_10[baseDecimal];
		writer.baseDecimal[counter] = POWERS_OF_10[counterDecimal];
	}
	
	public void takeSnapshot() {
		writer.takeSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BASE_DECIMALS_SNAPSHOT));
	}
}