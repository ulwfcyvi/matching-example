package org.ssi.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.ssi.model.BaseEvent;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.TimeoutException;

public final class RiskProcessor implements EventProcessor {
//	private static final Logger log = LoggerFactory.getLogger(RiskProcessor.class);
    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private ExceptionHandler exceptionHandler = new FatalExceptionHandler();
    private final AtomicInteger running = new AtomicInteger(IDLE);
    private final DataProvider<BaseEvent> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final EventHandler<BaseEvent> eventHandler;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    
    private BalanceProcessor balanceProcessor;

    public BalanceProcessor get() {
    	return this.balanceProcessor;
    }
    public void setBP(BalanceProcessor bp) {
    	balanceProcessor = bp;
    }
    
    public RiskProcessor(final DataProvider<BaseEvent> dataProvider, final SequenceBarrier sequenceBarrier,
                           final EventHandler<BaseEvent> eventHandler) {
        this.dataProvider = dataProvider;
        this.sequenceBarrier = sequenceBarrier;
        this.eventHandler = eventHandler;
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void halt() {
        running.set(HALTED);
        sequenceBarrier.alert();
    }

    @Override
    public boolean isRunning() {
        return running.get() != IDLE;
    }


    /**
     * It is ok to have another thread rerun this method after a halt().
     *
     * @throws IllegalStateException if this object instance is already running in a thread
     */
    @Override
    public void run() {
        if (running.compareAndSet(IDLE, RUNNING)) {
            sequenceBarrier.clearAlert();
            notifyStart();
            try {
                if (running.get() == RUNNING) {
//                    try (AffinityLock cpuLock = AffinityLock.acquireLock()) {
                        processEvents();
//                    }
                }
            } finally {
            	
            	notifyShutdown();
                running.set(IDLE);
            }
        }
    }

    private void processEvents() {
    	BaseEvent event = null;
        long nextSequence = sequence.get() + 1L;
        
        // wait until balanceProcessor starts
        while (!balanceProcessor.isRunning()) {
            Thread.yield();
        }

        // main event loop
        while (true) {
            try {
            	// busy spinning waiting for next sequence available on the ring buffer
                long availableSequence = sequenceBarrier.waitFor(nextSequence);
                
                if (availableSequence >= nextSequence) {
                    while (nextSequence <= availableSequence) {
                        event = dataProvider.get(nextSequence);
                        
                        eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence);
                        nextSequence++;
                    }
                    sequence.set(availableSequence);
                    
                    // busy spinning handling the balance controller, take control of the thread
                    balanceProcessor.busyProcessing(nextSequence);
                }

            } catch (final Exception ex) {
                sequence.set(nextSequence);
                nextSequence++;
            }


        }
    }

    private void notifyStart()
    {
        if (eventHandler instanceof LifecycleAware)
        {

                ((LifecycleAware) eventHandler).onStart();

        }
    }

    private void notifyShutdown()
    {
        if (eventHandler instanceof LifecycleAware)
        {

                ((LifecycleAware) eventHandler).onShutdown();

        }
    }
}