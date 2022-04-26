package org.ssi.util;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public class WaitStrategyFactory {
	
	public static WaitStrategy createWaitStrategy(String strategy) {
		switch (strategy) {
		case "busyspinwait":
			return new BusySpinWaitStrategy();
		case "yieldingwait":
			return new YieldingWaitStrategy();
		case "sleepingwait":
			return new SleepingWaitStrategy();
		case "blockingwait":
			return new BlockingWaitStrategy();
		default:
			return null;
		}
	}
}
