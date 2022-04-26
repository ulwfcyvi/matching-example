package org.ssi.consumer;

import org.ssi.model.BaseEvent;
import org.ssi.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.EventHandler;

@Service
public class BalanceConsumer implements EventHandler<BaseEvent>{
	private static final Logger LOG = LoggerFactory.getLogger(BalanceConsumer.class);
	@Autowired
	private BalanceService balanceService;
//	public long lastSequence = -1;
	@Override
	public void onEvent(BaseEvent event, long sequence, boolean endOfBatch) throws Exception {
//		lastSequence = sequence;
		balanceService.processBalance(event, sequence, endOfBatch);
	}
}