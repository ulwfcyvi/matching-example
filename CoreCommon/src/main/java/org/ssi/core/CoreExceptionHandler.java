package org.ssi.core;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lmax.disruptor.ExceptionHandler;

public class CoreExceptionHandler<T> implements ExceptionHandler<T> {

	private static final Logger log = LogManager.getLogger(CoreExceptionHandler.class);
	
    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        log.error("Disruptor exception caught: {}", ex);
        log.debug("Sequence: {}, Event: {}", sequence, event);
        ex.printStackTrace();
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("Disruptor expcetion on startup: {}", ex);
        ex.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
    	log.error("Disruptor expcetion on shutdown: {}", ex);
    	ex.printStackTrace();
    }
}
