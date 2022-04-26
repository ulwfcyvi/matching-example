package org.ssi.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.ssi.model.BaseEvent;

import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.TimeoutException;

public final class BalanceProcessor implements EventProcessor {
    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private final AtomicInteger running = new AtomicInteger(IDLE);
    private final DataProvider<BaseEvent> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final EventHandler<? super BaseEvent> eventHandler;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private long nextSequence = 0;

    public BalanceProcessor(DataProvider<BaseEvent> dataProvider, SequenceBarrier sequenceBarrier, EventHandler<? super BaseEvent> eventHandler) {
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
        }
        else
        {
            // This is a little bit of guess work.  The running state could of changed to HALTED by
            // this point.  However, Java does not have compareAndExchange which is the only way
            // to get it exactly correct.
            if (running.get() == RUNNING)
            {
                throw new IllegalStateException("Thread is already running");
            }
        }
    }
    
    public void busyProcessing(long processUpToSequence) {
        while (true) {
            try {
                long availableSequence = sequenceBarrier.waitFor(nextSequence);
                // process the batch
                while (nextSequence <= availableSequence && nextSequence < processUpToSequence) {
                    eventHandler.onEvent(dataProvider.get(nextSequence), nextSequence, nextSequence == availableSequence);
                    nextSequence++;
                }

                // release the thread when all events get processed
                if (nextSequence == processUpToSequence) {
                    sequence.set(nextSequence - 1);
                    return;
                }

                sequence.set(availableSequence);

            } catch (final Exception ex) {
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }


}