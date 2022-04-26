package org.ssi.core;

import java.io.IOException;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ssi.consumer.BalanceConsumer;
import org.ssi.consumer.MarketDataConsumer;
import org.ssi.consumer.MatchingEngineConsumer;
import org.ssi.consumer.OrderBookConsumer;
import org.ssi.consumer.OrdersConsumer;
import org.ssi.consumer.RiskCheckingConsumer;
import org.ssi.io.MetadataManager;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventType;
import org.ssi.request.BaseEventPublisher;
import org.ssi.service.BalanceService;
import org.ssi.service.MetadataService;
import org.ssi.util.WaitStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

@Service
public class BaseEventProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BaseEventProcessor.class);

	private final int THREAD_POOL_SIZE = 6;


	@Autowired
	private RiskCheckingConsumer riskChecker;

	@Autowired
	private MatchingEngineConsumer matchingEngine;

	@Autowired
	private BalanceConsumer balanceConsumer;

	@Autowired
	private MarketDataConsumer marketDataConsumer;

	@Autowired
	private OrderBookConsumer orderbookConsumer;

	@Autowired
	private OrdersConsumer ordersConsumer;

	@Autowired
	private BaseEventPublisher baseEventPublisher;

	@Autowired
	private MetadataService metaService;
	
	@Autowired
	private BalanceService balanceService;
	
	private Disruptor<BaseEvent> disruptor;
	private RingBuffer<BaseEvent> ringBuffer;

	private RiskProcessor riskProcessor;
	private BalanceProcessor balanceProcessor;

	private boolean isRunning = false;

	@Value("${app.waitstrategy}")
	private String waitStrategy;
	
	// Initialize after DI
	@PostConstruct
	public void initialize() {
//		ReplicationConfig.loadConfig();
		LOG.info("Using {} strategy", waitStrategy);
//		ThreadFactory threadFactory = new AffinityThreadFactory("TF", AffinityStrategies.DIFFERENT_CORE, AffinityStrategies.DIFFERENT_SOCKET, AffinityStrategies.ANY);
		disruptor = new Disruptor<>(BaseEvent::new, 16 * 1024 * 1024, Executors.defaultThreadFactory(),
				ProducerType.SINGLE, WaitStrategyFactory.createWaitStrategy(waitStrategy));

		disruptor.setDefaultExceptionHandler(new CoreExceptionHandler<BaseEvent>());

        disruptor.handleEventsWith((rb, bs) -> {
            riskProcessor = new RiskProcessor(rb, rb.newBarrier(bs), riskChecker);
            return riskProcessor;
        });
        
        disruptor.after(riskProcessor).handleEventsWith(matchingEngine);
//        disruptor.handleEventsWith(riskChecker);
//        disruptor.after(riskChecker).handleEventsWith(matchingEngine);
//        disruptor.after(matchingEngine).then(marketDataConsumer, ordersConsumer, orderbookConsumer, dummyConsumer);
        disruptor.after(matchingEngine).then(marketDataConsumer,ordersConsumer,orderbookConsumer);
        disruptor.after(matchingEngine).then((rb, bs) -> {
	    	balanceProcessor = new BalanceProcessor(rb, rb.newBarrier(bs), balanceConsumer);
	        riskProcessor.setBP(balanceProcessor);
	        return balanceProcessor;
	    });
                
		ringBuffer = disruptor.start();
		isRunning = true;		
		baseEventPublisher.setRingbuffer(ringBuffer);
//		ReplicatingService.startReplication(this, metaService);
//		if (!SystemPropsUtil.getLoadSnapshot() ) {
//			LOG.info("Setting up test data...");
			setupSymbols();
//			setupUsers();
//		}
		

	}

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() throws IOException {

		// stop the event request first
		if (baseEventPublisher != null) {
			baseEventPublisher.stopListening();
		}
		
		metaService.finish(System.currentTimeMillis(), baseEventPublisher.totalEvents());
		balanceService.createSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.BAlANCE_SNAPSHOT));
		matchingEngine.matchingEngine().saveSnapshot(metaService.getManager().getSnapshotFolder(MetadataManager.MATCHING_ENGINE_SNAPSHOT));
		ordersConsumer.takeSnapshot();
		marketDataConsumer.takeSnapshot();
//		ReplicatingService.takeSnapshot();
		
//		saveSnapshot();

	}

    @PreDestroy
    public void preDestroy() {
//        saveSnapshot();
    }
	public void start() {
		LOG.info("Starting CoreProcessApp...");		
		baseEventPublisher.startListening();
	}

	private final int SYMBOL_ID = 65538; // BTCUSD 1-2
	private final int SYMBOL_ID2 = 196610; // ETHUSD 3-2
	private final int SYMBOL_ID3 = 262146; // EOSUSD 4-2
	private final int SYMBOL_ID4 = 196609; // ETHBTC 3-1
	private final int SYMBOL_ID5 = 327681; // XRPBTC 5-1
	private final int SYMBOL_ID6 = 393217; // LTCBTC 6-1
	private final int SYMBOL_ID7 = 458753; // ETCBTC 7-1
	private final int SYMBOL_ID8 = 524289; // XTZBTC 8-1
	private final int SYMBOL_ID9 = 589825; // ZRXBTC 9-1
	private final int SYMBOL_ID10 = 655361; // ENJBTC 10-1
	
	private final int BASE_ID = 1;
	private final int COUNTER_ID = 2;
	private final int NUM_USERS = 200;

	public void setupSymbols() {
		long sequenceId = ringBuffer.next();
		BaseEvent event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID; // BTCUSD
		event.price = BASE_ID; // base ID
		event.amount = COUNTER_ID; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 9; // base decimal
		event.orderType = 8;// counter decimal
		event.tradeType = 2; // symbol decimal
		ringBuffer.publish(sequenceId);

		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID2; // ETHUSD
		event.price = 3; // base ID
		event.amount = 2; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 9; // base decimal
		event.orderType = 8;// counter decimal
		event.tradeType = 2; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID3; // EOSUSD
		event.price = 4; // base ID
		event.amount = 2; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 8;// counter decimal
		event.tradeType = 4; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID4; // ETHBTC
		event.price = 3; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 9; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 6; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID5; // XRPBTC
		event.price = 5; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 8; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID6; // LTCBTC
		event.price = 6; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 6; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID7; // ETCBTC
		event.price = 7; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 7; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID8; // XTZBTC
		event.price = 8; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 7; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID9; // ZRXBTC
		event.price = 9; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 8; // symbol decimal
		ringBuffer.publish(sequenceId);
		
		sequenceId = ringBuffer.next();
		event = ringBuffer.get(sequenceId);
		event.eventType = EventType.ADD_SYMBOL;
		event.symbol = SYMBOL_ID10; // ENJBTC
		event.price = 10; // base ID
		event.amount = 1; // counter ID
		event.eventResult = 0;
		event.matchingResult = null;
		
		event.orderSide = 8; // base decimal
		event.orderType = 9;// counter decimal
		event.tradeType = 8; // symbol decimal
		ringBuffer.publish(sequenceId);
	}
	
	public void setupUsers() {
		// add new symbol

		long sequenceId;
		BaseEvent event;
		// add users

		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.eventType = EventType.ADD_NEW_USER;
			event.userId = i;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = BASE_ID;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 10_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}

		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = COUNTER_ID;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 100_000_000_000_000_000L;
			} else {
				event.amount = 1_000_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 3;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}

		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 4;
			event.stopPrice = 2;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 5;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 6;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 7;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 8;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 9;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
		
		for (int i = 1; i <= NUM_USERS; i++) {
			sequenceId = ringBuffer.next();
			event = ringBuffer.get(sequenceId);
			event.userId = i;
			event.eventType = EventType.CHANGE_BALANCE;
			event.symbol = 10;
			if (i == 7 || i == 58 || i ==181) {
				event.amount = 1_000_000_000_000_000L;
			} else {
				event.amount = 100_000_000_000L;
			}
			event.stopPrice = 2;
			event.eventResult = 0;
			event.matchingResult = null;
			ringBuffer.publish(sequenceId);
		}
	}
	public void saveSnapshot() {
		long sequenceId = ringBuffer.next();
		BaseEvent event = ringBuffer.get(sequenceId);
		event.eventType = EventType.TAKE_SNAPSHOT;
		ringBuffer.publish(sequenceId);
	}
	
	public BaseEventPublisher getBaseEventPublisher() {
		return baseEventPublisher;
	}
	
	public RingBuffer<BaseEvent> getRingBuffer() {
		return ringBuffer;
	}

}
