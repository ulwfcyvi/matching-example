package org.ssi.replication.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.chronicle.core.Jvm;


public class ReplicatingEventThread implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(ReplicatingEventThread.class);
	private boolean isRun = true;
	private static final String NAME = "Replicated Thread";
	private static final ReplicatingEventThread replicateTask = new ReplicatingEventThread();
	private AffinityLock al;
	
	@Override
	public void run() {
		try (AffinityLock al2 = al.acquireLock(AffinityStrategies.SAME_SOCKET, AffinityStrategies.ANY)) {
			LOG.info("Start thread replicate {} \n{}",Thread.currentThread(),AffinityLock.dumpLocks());
			while (isRun) {
				if (ReplicationProcessor.canReplicateNow()) {
					ReplicationProcessor.replicateToSlave();
				} else {
					LOG.debug("Wait new event because hasEvent {}", ReplicationProcessor.hasEvent());
					Jvm.pause(1);
//				LockSupport.parkNanos(50000);
				}
			}
			al2.release();
		}

	}

	public boolean isRun() {
		return isRun;
	}

	public void setRun(boolean isRun) {
		LOG.info("setRun replicate {}", isRun);
		this.isRun = isRun;
	}
	
	public void setAffinityLock(AffinityLock lock) {
		al = lock;
	}

	public static void start() {
		try (AffinityLock al = AffinityLock.acquireLock()) {
			replicateTask.setAffinityLock(al);
			Thread t = new Thread(replicateTask, NAME);
			t.start();
		}
	}

	public static void stop() {
		replicateTask.setRun(false);		
		replicateTask.releaseCpuLock();
	}

	private void releaseCpuLock() {
		if(al!= null) {
			al.release();
		}
	}

}
