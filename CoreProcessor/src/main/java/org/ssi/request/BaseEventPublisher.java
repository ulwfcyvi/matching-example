package org.ssi.request;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.agrona.DirectBuffer;
import org.agrona.LangUtil;
import org.agrona.concurrent.AgentTerminationException;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SigInt;
import org.ssi.config.BaseEventConfig;
import org.ssi.model.BaseEvent;
import org.ssi.model.EventResult;
import org.ssi.sbe.BaseEventEnDeCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.RingBuffer;

import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.exceptions.DriverTimeoutException;
import io.aeron.logbuffer.FragmentHandler;

@Component
public class BaseEventPublisher{
	private static final Logger LOG = LoggerFactory.getLogger(BaseEventPublisher.class);

	private RingBuffer<BaseEvent> ringBuffer;

	private final BaseEventEnDeCoder BASE_EVENT_ENDE_CODER = new BaseEventEnDeCoder();

	private long nEvents = 0;
	
	public void setRingbuffer(RingBuffer<BaseEvent> rb) {
		ringBuffer = rb;
	}

//	private final EventTranslatorOneArg<BaseEvent, byte[]> TRANSLATOR = new EventTranslatorOneArg<BaseEvent, byte[]>() {
//		public void translateTo(BaseEvent event, long sequence, byte[] bytes) {
//			BASE_EVENT_ENDE_CODER.decodeByte(bytes, event);
//			nEvents++;
//			// event came from ring buffer must be reset before using
//			event.next = null;
//			event.eventResult = EventResult.OK;
//			event.matchingResult = null;
//			event.timestamp = System.currentTimeMillis();
//			event.ignoreCheckBalance = false;
////			LOG.debug("Sequence={}, BASE_EVENT_ENDE_CODER {}", sequence, event.toString());
//		}
//	};

	private final EventTranslatorTwoArg<BaseEvent, DirectBuffer, Integer> TRANSLATOR = new EventTranslatorTwoArg<BaseEvent, DirectBuffer, Integer>() {
		@Override
		public void translateTo(BaseEvent event, long sequence, DirectBuffer buffer, Integer offset) {
			BASE_EVENT_ENDE_CODER.decodeByte(buffer, offset, event);
			nEvents++;
			// event came from ring buffer must be reset before using
			event.next = null;
			event.eventResult = EventResult.OK;
			event.matchingResult = null;
//			event.timestamp = System.currentTimeMillis();
			event.ignoreCheckBalance = false;
//			LOG.info("Time receive event====> {}", System.nanoTime() - event.clientOrderId);
		}
	};
	
	private final AtomicBoolean BASE_EVENT_RUNNING = new AtomicBoolean(true);
	private final AtomicBoolean IS_CONSUME_BASE_EVENT = new AtomicBoolean(true);
	private final IdleStrategy BASE_EVENT_IDLESTRATEGY = new BusySpinIdleStrategy();
	private static final long LINGERING_PERIOD = 500;
	private static final int RETRY_TIMES = 15;
	private static boolean IS_STOPPED = false;
	private static int fragmentLimit = 1024;
	
	private Aeron BASE_EVENT_AERON;
	
	@PostConstruct
	private void initialize() throws Exception {
		int counter = 0;
		boolean keepTrying = true;
		fragmentLimit = BaseEventConfig.getBaseEventFragmentLimit();
		while (counter < RETRY_TIMES && keepTrying) {
				connectToAeron();
				keepTrying = false;

			counter++;
		}
		if (counter == RETRY_TIMES) {
			throw new Exception("Aeron driver is not running!");
		}
	}

	private void connectToAeron() {
		BASE_EVENT_AERON = Aeron.connect(new Aeron.Context().errorHandler(arg0 -> {
			try {
				errorHandler(arg0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}));
		IS_STOPPED = false;
	}
	
	private void errorHandler(Throwable throwable) throws InterruptedException {
		throwable.printStackTrace();
		if (throwable instanceof AgentTerminationException) {
			LOG.warn("Cannot connect to aeron, retrying after {} milliseconds...", LINGERING_PERIOD);
			Thread.sleep(LINGERING_PERIOD);
			IS_STOPPED = true;
			connectToAeron();
		} else if (throwable instanceof DriverTimeoutException) {
			if (IS_STOPPED) {
				LOG.warn("Cannot connect to aeron, retrying after {} milliseconds...", LINGERING_PERIOD);
				Thread.sleep(LINGERING_PERIOD);
				connectToAeron();
			}
		}
	}
	
	private final FragmentHandler BASE_EVENT_FRAGMENT_HANDLER = (buffer, offset, length, header) -> {
//		final byte[] data = new byte[length];
//		buffer.getBytes(offset, data);
//		if(IS_CONSUME_BASE_EVENT.get()) {
			ringBuffer.publishEvent(TRANSLATOR, buffer, offset);
//		}
	};

	public final Consumer<Subscription> BASE_EVENT_SUBSCRIPTION_CONSUMER = (subscription) -> {

			while (BASE_EVENT_RUNNING.get()) {
//				LOG.info("RUN");
				final int fragmentsRead = subscription.poll(BASE_EVENT_FRAGMENT_HANDLER, fragmentLimit);
				BASE_EVENT_IDLESTRATEGY.idle(fragmentsRead);
			}

	};

	public void startListening() {
		SigInt.register(() -> BASE_EVENT_RUNNING.set(false));
		Subscription subscription = BASE_EVENT_AERON.addSubscription(BaseEventConfig.getBaseEventChannelSub(),
				BaseEventConfig.getBaseEventStreamIdSub());
		BASE_EVENT_SUBSCRIPTION_CONSUMER.accept(subscription);
	}

	public void stopListening() {
		BASE_EVENT_RUNNING.set(false);
	}
	
	public void startConsumeEvent() {		
		IS_CONSUME_BASE_EVENT.set(true);
	}
	
	public void stopConsumeEvent() {		
		IS_CONSUME_BASE_EVENT.set(false);
	}
		
	public long totalEvents() {
		return nEvents;
	}
	
	public void resetCounter() {
		nEvents = 0;
	}	
}
