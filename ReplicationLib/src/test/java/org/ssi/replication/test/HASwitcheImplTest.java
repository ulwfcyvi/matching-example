package org.ssi.replication.test;

import org.ssi.replication.process.HASwitcher;
import org.ssi.replication.process.ReplicationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HASwitcheImplTest implements HASwitcher{
	private static final Logger LOG = LoggerFactory.getLogger(ReplicationProcessor.class);
	@Override
	public int processToBeMaster(boolean isForce) {
		LOG.info("Start switch to be master");
		return 1;
	}

	@Override
	public int processToBeSlave() {
		LOG.info("Start switch to be slave");
		return 1;
	}

}
