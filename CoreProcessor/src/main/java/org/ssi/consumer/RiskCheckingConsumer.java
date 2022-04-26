package org.ssi.consumer;

import org.ssi.model.BaseEvent;
import org.ssi.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

@Service
public class RiskCheckingConsumer implements EventHandler<BaseEvent>, LifecycleAware{
	
	private static final Logger LOG = LoggerFactory.getLogger(RiskCheckingConsumer.class);

	@Autowired
	private BalanceService balanceService;
	
	@Override
	public void onEvent(BaseEvent event, long sequence, boolean endOfBatch) throws Exception {
		//LOG.info("Risk sequence={}", sequence);
		balanceService.check(event, sequence, endOfBatch);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		System.out.println("Starting up balance");
	}

	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub
		System.out.println("Shutting down balance");
//		balanceService.createSnapshot();
	}
}