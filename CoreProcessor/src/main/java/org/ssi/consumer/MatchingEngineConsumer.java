package org.ssi.consumer;

import javax.annotation.PostConstruct;

import org.ssi.CoreGlobalValue;
import org.ssi.io.MetadataManager;
import org.ssi.matching.MatchingEngine;
import org.ssi.model.BaseEvent;
import org.ssi.service.MetadataService;
import org.ssi.util.SystemPropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.EventHandler;

@Service
public class MatchingEngineConsumer implements EventHandler<BaseEvent>{

	private static final Logger LOG = LoggerFactory.getLogger(MatchingEngineConsumer.class);
	@Autowired
	private MetadataService metaService;
	
	private MatchingEngine matchingEngine;
	
	@PostConstruct
	public void initialize() throws Exception {
		matchingEngine = new MatchingEngine(CoreGlobalValue.MAX_OPEN_ORDER_NUM, CoreGlobalValue.MAX_SYMBOL_NUM, null);
		if (SystemPropsUtil.getLoadSnapshot()) {
			long index = SystemPropsUtil.getEpocIndex();
			if (index == -1) {
				index = metaService.getEpocIndex() - 1;
			}
			if (index < 0) return;
			matchingEngine.loadSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.MATCHING_ENGINE_SNAPSHOT, index));
			LOG.info("Loading matching engine snapshot @epoch {}... done.", index);
		}
	}	

	@Override
	public void onEvent(BaseEvent event, long sequence, boolean endOfBatch) throws Exception {
		//LOG.info("Matching sequence={}", sequence);
		matchingEngine.handleEvent(event);
	}
	
	public MatchingEngine matchingEngine() {
		return matchingEngine;
	}
}