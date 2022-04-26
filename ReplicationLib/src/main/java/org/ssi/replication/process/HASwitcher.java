package org.ssi.replication.process;

public interface HASwitcher {
	// process to master
	public int processToBeMaster(boolean isForce);

	// process to slave
	public int processToBeSlave();
}
