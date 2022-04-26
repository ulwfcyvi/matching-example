package org.ssi.consumer;

import javax.annotation.PostConstruct;

import org.ssi.io.OBDataWriter;
import org.ssi.model.BaseEvent;
import org.ssi.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.EventHandler;

@Service
public class OrderBookConsumer implements EventHandler<BaseEvent>{
	private static final Logger log = LoggerFactory.getLogger(OrderBookConsumer.class);
	private OBDataWriter writer;
	
	@Autowired
	private MetadataService metaService;
	
	@PostConstruct
	public void initialize() {
		writer = new OBDataWriter();
		writer.initQueue(metaService.getEpocIndex(), "/orderbook");
	}	
	@Override
	public void onEvent(BaseEvent event, long sequence, boolean endOfBatch) throws Exception {
		writer.write(event, sequence, endOfBatch);
	}
}