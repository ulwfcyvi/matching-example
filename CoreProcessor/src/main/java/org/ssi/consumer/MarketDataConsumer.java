package org.ssi.consumer;

import javax.annotation.PostConstruct;

import org.ssi.io.MarketDataWriter;
import org.ssi.io.MetadataManager;
import org.ssi.model.BaseEvent;
import org.ssi.service.MetadataService;
import org.ssi.util.SystemPropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.EventHandler;

@Service
public class MarketDataConsumer implements EventHandler<BaseEvent>{
	
	private static final Logger LOG = LoggerFactory.getLogger(MarketDataConsumer.class);
	private MarketDataWriter writer;
	
	@Autowired
	private MetadataService metaService;
	
	@PostConstruct
	public void initialize() {
		writer = new MarketDataWriter(metaService.getManager());
		writer.initQueue(metaService.getEpocIndex(), "/marketdata");
		
		// load snapshot if needed
		if (SystemPropsUtil.getLoadSnapshot()) {
			long index = SystemPropsUtil.getEpocIndex();
			if (index == -1) {
				index = metaService.getEpocIndex() - 1;
			}
			if (index < 0) return;
			writer.loadSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.FEES_MAP_SNAPSHOT, index));
			LOG.info("Loading fees map snapshot @epoch {}... done.", index);
		}
	}	
	
	@Override
	public void onEvent(BaseEvent event, long sequence, boolean endOfBatch) throws Exception {
		writer.write(event, sequence, endOfBatch);
	}
	
	public void takeSnapshot() {
		writer.takeSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.FEES_MAP_SNAPSHOT));
	}
}